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
        extends Fragment
        implements Runnable {
    private ViewProgressBinding binding;
    private RoundImageView progressImage;
    private ImageView botImage;
    private TextView message;
    private Handler handler;
    private int value = 0;

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

        message = view.findViewById(R.id.progress_message);
        //新增进度条
        progressImage = view.findViewById(R.id.p_cover_iv);
        botImage = view.findViewById(R.id.p_bot_iv);
        handler.post(this);
    }


    private void updatePercent(int percent) {
        //除以100，得到百分比
        float percentFloat = percent / 100.0f;
        //获取总长度
        final int ivWidth = botImage.getWidth();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
                progressImage.getLayoutParams();
        //获取剩下的长度
        int marginEnd = (int) ((1 - percentFloat) * ivWidth);
        lp.width = ivWidth - marginEnd;
        progressImage.setLayoutParams(lp);
        progressImage.postInvalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(this);
        binding = null;
        botImage = null;
        progressImage = null;
    }

    private void setMessage(String message) {
        if (this.message != null) {
            this.message.setText(message);
        }
    }


    public static void observeWith(LiveData<Boolean> liveData, Fragment root) {
        liveData.observe(root, new EventHandler(root));
    }

    @Override
    public void run() {
        if (value < 100) {
            updatePercent(++value);
            setMessage("加载中" + value + "%");
        } else {
            value = 0;
        }
        handler.postDelayed(this, 50);
    }


    private static final class EventHandler
            extends Handler
            implements Observer<Boolean>,
            Runnable {

        private final OnBackPressedCallback onBackPressed
                = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
            }
        };
        private final FragmentManager manager;
        private final Fragment target;
        private boolean isAdding = false;

        private EventHandler(Fragment fragment) {
            super(Looper.getMainLooper());
            target = fragment;
            manager = fragment.requireFragmentManager();
            fragment.requireActivity()
                    .getOnBackPressedDispatcher()
                    .addCallback(fragment, onBackPressed);
        }

        @Override
        public void run() {
            removeCallbacks(this);
            if (isAdding) {
                postDelayed(this, 200);
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
            onBackPressed.setEnabled(false);
        }

        @Override
        public void onChanged(Boolean show) {
            if (!manager.isDestroyed()) {
                ProgressFragment fragment = (ProgressFragment) manager
                        .findFragmentByTag(ProgressFragment.class.getName());
                if (fragment == null && !isAdding && show != null && show) {
                    fragment = new ProgressFragment();
                    fragment.handler = this;
                    isAdding = true;
                    fragment.setMessage("加载中");
                    manager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(target.getId(), fragment, ProgressFragment.class.getName())
                            .runOnCommit(() -> isAdding = false)
                            .commitAllowingStateLoss();
                    onBackPressed.setEnabled(true);
                } else {

                    postDelayed(this, 200);
                }
            }
        }
    }
}