package org.kexie.android.dng.common.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.kexie.android.dng.common.R;
import org.kexie.android.dng.common.databinding.ViewProgressBinding;

import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public final class ProgressFragment
        extends Fragment
{
    private final static int UPDATE = 1;
    private final static int REMOVE = 2;
    private ViewProgressBinding binding;
    private RoundCornerImageView mProgressIv;
    private ImageView mBotIv;
    private TextView mProgressMessage;
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

        mProgressMessage = view.findViewById(R.id.progress_message);
        //新增进度条
        mProgressIv = view.findViewById(R.id.p_cover_iv);
        mBotIv = view.findViewById(R.id.p_bot_iv);
        Message message = mHandler.obtainMessage();
        message.what = UPDATE;
        message.obj = new Runnable()
        {
            private int value = 0;

            @Override
            public void run()
            {
                if (value < 100)
                {
                    updatePercent(++value);
                    setMessage("加载中" + value + "%");
                } else
                {
                    value = 0;
                }
                Message newMessage = mHandler.obtainMessage();
                newMessage.what = UPDATE;
                newMessage.obj = this;
                mHandler.sendMessageDelayed(newMessage, 50);
            }
        };
        message.sendToTarget();
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
        mHandler.removeMessages(UPDATE);
        binding = null;
        mBotIv = null;
        mProgressIv = null;
    }

    private void setMessage(String message)
    {
        if (mProgressMessage != null)
        {
            mProgressMessage.setText(message);
        }
    }

    public static void observeWith(LiveData<Boolean> liveData, Fragment root)
    {
        liveData.observe(root, new ObserverImpl(root));
    }

    private static final class ObserverImpl
            extends Handler
            implements Observer<Boolean>,
            OnBackPressedCallback
    {
        private final ProgressFragment progressFragment = new ProgressFragment();

        private ObserverImpl(Fragment fragment)
        {
            super(Looper.getMainLooper());
            progressFragment.setTargetFragment(fragment, 0);
            progressFragment.mHandler = this;
            progressFragment.setMessage("加载中");
        }

        @Override
        public void onChanged(Boolean aBoolean)
        {
            Fragment target = Objects.requireNonNull(progressFragment.getTargetFragment());
            if (aBoolean != null && aBoolean)
            {
                if (!progressFragment.isAdded())
                {
                    target.requireFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(target.getId(), progressFragment)
                            .show(progressFragment)
                            .commit();
                    target.requireActivity()
                            .addOnBackPressedCallback(this);
                }
            } else
            {
                Message message = Message.obtain();
                message.obj = (Runnable) () -> {
                    this.removeMessages(REMOVE);
                    if (progressFragment.isAdded())
                    {
                        progressFragment.requireFragmentManager()
                                .beginTransaction()
                                .remove(progressFragment)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                                .commit();
                        progressFragment.requireActivity()
                                .removeOnBackPressedCallback(this);
                    }
                };
                message.what = REMOVE;
                sendMessageDelayed(message, 200);
            }
        }

        @Override
        public void handleMessage(Message msg)
        {
            Object run = msg.obj;
            if (run instanceof Runnable)
            {
                ((Runnable) run).run();
            }
        }

        @Override
        public boolean handleOnBackPressed()
        {
            return true;
        }

    }
}
