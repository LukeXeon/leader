package org.kexie.android.common.widget;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;

public class LifecycleIdleWorker<T> implements LifecycleOwner, Observer<T>
{
    private static final Lifecycle.State IDlE = Lifecycle.State.STARTED;

    private static final Lifecycle.State WORKING = Lifecycle.State.CREATED;

    private static final Lifecycle.State CLOSE = Lifecycle.State.DESTROYED;

    private static final Handler sMain = new Handler(Looper.getMainLooper());

    private final HandlerThread mWorkerThread = new HandlerThread(toString());

    private final Handler mWorker;

    private final Observer<T> mInner;

    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    public LifecycleIdleWorker(@NonNull Observer<T> inner)
    {
        mInner = Objects.requireNonNull(inner);
        mWorkerThread.start();
        mWorker = new Handler(mWorkerThread.getLooper());
        mLifecycleRegistry.setCurrentState(IDlE);
    }

    public void close()
    {
        mLifecycleRegistry.setCurrentState(CLOSE);
        mWorkerThread.quitSafely();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle()
    {
        return mLifecycleRegistry;
    }

    @Override
    public void onChanged(T t)
    {
        if (mLifecycleRegistry.getCurrentState().equals(IDlE))
        {
            mLifecycleRegistry.setCurrentState(WORKING);
            mWorker.post(() -> {
                mInner.onChanged(t);
                sMain.post(() -> mLifecycleRegistry.setCurrentState(IDlE));
            });
        }
    }
}
