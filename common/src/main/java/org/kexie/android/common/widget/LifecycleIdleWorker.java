package org.kexie.android.common.widget;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;

public class LifecycleIdleWorker implements LifecycleOwner
{
    private static final Lifecycle.State IDlE = Lifecycle.State.STARTED;

    private static final Lifecycle.State WORKING = Lifecycle.State.CREATED;

    private static final Lifecycle.State CLOSE = Lifecycle.State.DESTROYED;

    private static final Handler sMain = new Handler(Looper.getMainLooper());

    private final HandlerThread mWorkerThread = new HandlerThread(toString());

    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    private final Handler mWorker;

    public LifecycleIdleWorker()
    {
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

    @NonNull
    public <T> Observer<T> makeObserver(@NonNull Observer<T> inner)
    {
        return data -> {
            if (isIdle())
            {
                mLifecycleRegistry.setCurrentState(WORKING);
                mWorker.post(() -> {
                    inner.onChanged(data);
                    sMain.post(() -> mLifecycleRegistry.setCurrentState(IDlE));
                });
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isIdle()
    {
        return mLifecycleRegistry.getCurrentState().equals(IDlE);
    }

}
