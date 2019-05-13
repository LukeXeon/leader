package org.kexie.android.dng.common.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.kexie.android.dng.common.R;
import org.kexie.android.dng.common.databinding.ViewProgressBinding;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public final class ProgressFragment
        extends Fragment {
    private ViewProgressBinding binding;
    private RoundCornerImageView mProgressIv;
    private ImageView mBotIv;
    private TextView mProgressMessage;
    private Handler mHandler;
    private int mValue = 0;
    private Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            if (mValue < 100) {
                updatePercent(++mValue);
                setMessage("加载中" + mValue + "%");
            } else {
                mValue = 0;
            }
            mHandler.postDelayed(this,50);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.view_progress,
                container,
                false);

        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.getRoot().setOnTouchListener((x, y) -> true);

        mProgressMessage = view.findViewById(R.id.progress_message);
        //新增进度条
        mProgressIv = view.findViewById(R.id.p_cover_iv);
        mBotIv = view.findViewById(R.id.p_bot_iv);
        mHandler.post(mUpdater);
    }


    private void updatePercent(int percent) {
        float percentFloat = percent / 100.0f;//除以100，得到百分比
        final int ivWidth = mBotIv.getWidth();//获取总长度
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
                mProgressIv.getLayoutParams();
        int marginEnd = (int) ((1 - percentFloat) * ivWidth); //获取剩下的长度
        lp.width = ivWidth - marginEnd;
        mProgressIv.setLayoutParams(lp);
        mProgressIv.postInvalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mUpdater);
        binding = null;
        mBotIv = null;
        mProgressIv = null;
    }

    private void setMessage(String message) {
        if (mProgressMessage != null) {
            mProgressMessage.setText(message);
        }
    }

    public static void observeWith(LiveData<Boolean> liveData, Fragment root) {
        liveData.observe(root, new ObserverHandler(root));
    }

    private static final class ObserverHandler
            extends Handler
            implements Observer<Boolean>,
            OnBackPressedCallback,
            Runnable {

        private final FragmentManager manager;
        private final Fragment target;
        private boolean isAdding = false;

        private ObserverHandler(Fragment fragment) {
            super(Looper.getMainLooper());
            target = fragment;
            manager = fragment.requireFragmentManager();
        }

        @Override
        public void run() {
            removeCallbacks(this);
            if (isAdding) {
                postDelayed(this,200);
                return;
            }
            if (!manager.isDestroyed()) {
                ProgressFragment fragment = (ProgressFragment) manager
                        .findFragmentByTag(ProgressFragment.class.getName());
                if (fragment != null) {
                    manager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .remove(fragment)
                            .commitAllowingStateLoss();
                }
            }
        }

        @Override
        public void onChanged(Boolean show) {
            if (!manager.isDestroyed()) {
                ProgressFragment fragment = (ProgressFragment) manager
                        .findFragmentByTag(ProgressFragment.class.getName());
                if (fragment == null && !isAdding && show != null && show) {
                    fragment = new ProgressFragment();
                    fragment.mHandler = this;
                    isAdding = true;
                    fragment.setMessage("加载中");
                    manager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(target.getId(), fragment, ProgressFragment.class.getName())
                            .runOnCommit(() -> isAdding = false)
                            .commitAllowingStateLoss();
                    target.requireActivity().addOnBackPressedCallback(fragment,
                            this);
                } else {
                    postDelayed(this, 200);
                }
            }
        }

        @Override
        public boolean handleOnBackPressed() {
            return true;
        }
    }
}
