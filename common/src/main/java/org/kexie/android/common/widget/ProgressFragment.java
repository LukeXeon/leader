package org.kexie.android.common.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.kexie.android.common.R;
import org.kexie.android.common.databinding.ViewProgressBinding;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import eightbitlab.com.blurview.RenderScriptBlur;
import io.reactivex.functions.Consumer;

public final class ProgressFragment
        extends Fragment
{
    private ViewProgressBinding binding;
    private RoundCornerImageView mProgressIv;
    private ImageView mBotIv;
    private String msg;
    private Handler mHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState)
    {
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
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.getRoot().setOnTouchListener((x, y) -> true);
        binding.rootView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(
                        getActivity().getWindow()
                                .getDecorView()
                                .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
        TextView mProgressMessage = view.findViewById(R.id.progress_message);
        //新增进度条
        mProgressIv = view.findViewById(R.id.p_cover_iv);
        mBotIv = view.findViewById(R.id.p_bot_iv);
        mProgressMessage.setText(msg);
        mHandler.post(new Runnable()
        {
            private int value = 0;

            @Override
            public void run()
            {
                if (value < 100)
                {
                    updatePercent(++value);
                    mHandler.postDelayed(this, 10);
                }
            }
        });
    }


    private void updatePercent(int percent)
    {
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
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
        mBotIv = null;
        msg = null;
        mProgressIv = null;
    }

    private void setMessage(String message)
    {
        this.msg = message;
    }

    public static Consumer<String> makeObserver(Fragment root)
    {
        return new Observer(root);
    }

    private static final class Observer
            extends Handler
            implements Consumer<String>,
            OnBackPressedCallback,
            Runnable
    {
        private final ProgressFragment progressFragment = new ProgressFragment();

        private Observer(Fragment fragment)
        {
            super(Looper.getMainLooper());
            progressFragment.setTargetFragment(fragment, 0);
            progressFragment.mHandler = this;
        }

        @Override
        public void accept(String s)
        {
            Fragment target = progressFragment.getTargetFragment();
            if (!TextUtils.isEmpty(s))
            {
                progressFragment.setMessage(s);
                if (!progressFragment.isAdded())
                {
                    target.getFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(target.getId(), progressFragment)
                            .show(progressFragment)
                            .commit();
                    target.getActivity().addOnBackPressedCallback(this);
                }
            } else
            {
                postDelayed(this, 200);
            }
        }

        @Override
        public boolean handleOnBackPressed()
        {
            return true;
        }

        @Override
        public void run()
        {
            this.removeCallbacksAndMessages(null);
            progressFragment.getFragmentManager()
                    .beginTransaction()
                    .remove(progressFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
            progressFragment.getActivity().removeOnBackPressedCallback(this);
        }
    }
}
