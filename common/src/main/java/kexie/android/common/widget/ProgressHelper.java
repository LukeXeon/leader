package kexie.android.common.widget;

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

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import kexie.android.common.R;


public final class ProgressHelper
        extends Fragment
{
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
        View view = inflater.inflate(R.layout.view_progress, container,
                false);
        view.setOnTouchListener((x, y) -> true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
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

    public static void observe(LiveData<String> liveData,
                               FragmentManager fragmentManager,
                               @IdRes int position)
    {
        liveData.observeForever(new WidgetObserver(fragmentManager, position));
    }

    private static class WidgetObserver
            implements Observer<String>
    {
        private static final String TAG = "wait";
        private final FragmentManager fragmentManager;
        private final ProgressHelper widget = new ProgressHelper();
        private final int position;

        private WidgetObserver(FragmentManager fragmentManager,
                               int position)
        {
            this.fragmentManager = fragmentManager;
            this.position = position;
        }

        @Override
        public void onChanged(@Nullable String message)
        {
            if (message != null)
            {
                widget.setMessage(TextUtils.isEmpty(message) ? "加载中" : message);
                fragmentManager.beginTransaction()
                        .add(position, widget)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            } else
            {
                widget.dismiss();
            }
        }
    }
}
