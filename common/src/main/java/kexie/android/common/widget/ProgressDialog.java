package kexie.android.common.widget;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import kexie.android.common.R;

/**
 * Created by zhangyc on 2018/6/7.
 */

public class ProgressDialog
        extends DialogFragment
{
    private ProgressBar mProgress;
    private TextView mProgressNumber, mProgressPercent, mProgressMessage, mPercentTv;
    private RoundCornerImageView mProgressIv;
    private ImageView mBotIv;
    private String msg;
    private Handler mainThread = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mProgress = view.findViewById(R.id.progress);
        mProgressNumber = view.findViewById(R.id.progress_number);
        mProgressPercent = view.findViewById(R.id.progress_percent);
        mProgressMessage = view.findViewById(R.id.progress_message);
        //新增进度条
        mProgressIv = view.findViewById(R.id.p_cover_iv);
        mBotIv = view.findViewById(R.id.p_bot_iv);
        mPercentTv = view.findViewById(R.id.percent_tv);
        mProgressMessage.setText(msg);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.view_progress, container, false);
    }

    private void updatePercent(int percent)
    {
//        mPercent = percent;
        Log.i("mPercent", "mPercent=" + percent);
        mPercentTv.setText(String.format(Locale.CHINA, "%2d%%", percent));//
        float percentFloat = percent / 100.0f;//除以100，得到百分比
        final int ivWidth = mBotIv.getWidth();//获取总长度
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mProgressIv.getLayoutParams();
        int marginEnd = (int) ((1 - percentFloat) * ivWidth); //获取剩下的长度
        lp.width = ivWidth - marginEnd;
        mProgressIv.setLayoutParams(lp);
        mProgressIv.postInvalidate();
    }

    public void setMessage(String message)
    {
        this.msg = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Window window = getDialog().getWindow();
        setCancelable(false);
        // 一定要设置Background，如果不设置，window属性设置无效
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
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

    @Override
    public void dismiss()
    {
        mainThread.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mainThread.removeCallbacksAndMessages(null);
                ProgressDialog.super.dismiss();
            }
        }, 200);
    }
}
