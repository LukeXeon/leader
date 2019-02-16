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
    private Handler mainThread = new Handler(Looper.getMainLooper());

    @SuppressLint("ClickableViewAccessibility")
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
        binding.getRoot().setOnTouchListener((x, y) -> true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.rootView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(
                        getActivity().getWindow()
                                .getDecorView()
                                .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);


        view.setVisibility(View.VISIBLE);
        TextView mProgressMessage = view.findViewById(R.id.progress_message);
        //新增进度条
        mProgressIv = view.findViewById(R.id.p_cover_iv);
        mBotIv = view.findViewById(R.id.p_bot_iv);
        mProgressMessage.setText(msg);
        mainThread.post(new Runnable()
        {
            private int value = 0;

            @Override
            public void run()
            {
                if (value < 100)
                {
                    updatePercent(++value);
                    mainThread.postDelayed(this, 10);
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
        mBotIv = null;
        msg = null;
        mProgressIv = null;
    }

    private void setMessage(String message)
    {
        this.msg = message;
    }

    private void dismiss()
    {
        mainThread.postDelayed(() -> {
            mainThread.removeCallbacksAndMessages(null);
            getFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }, 200);
    }

    public static Consumer<String> makeObserver(Fragment root)
    {
        return new Consumer<String>()
        {
            private final ProgressFragment fragment = new ProgressFragment();

            @Override
            public void accept(String s)
            {
                s = "".equals(s) ? "加载中..." : s;
                if (TextUtils.isEmpty(s))
                {
                    fragment.setMessage(s);
                    if (!fragment.isAdded())
                    {
                        root.getFragmentManager()
                                .beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .add(root.getId(), fragment)
                                .show(fragment)
                                .commit();
                    }
                } else
                {
                    if (fragment.isAdded())
                    {
                        fragment.dismiss();
                    }
                }
            }
        };
    }
}
