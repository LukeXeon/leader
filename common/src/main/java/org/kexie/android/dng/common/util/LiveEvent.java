package org.kexie.android.dng.common.util;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

public class LiveEvent<T> {
    private static final Looper mainLooper = Looper.getMainLooper();
    private static final Handler mainThread = new Handler(mainLooper);

    private LinkedList<Observer<T>> observers = new LinkedList<>();

    @MainThread
    public void observe(LifecycleOwner owner, Observer<T> observer) {
        Lifecycle lifecycle = owner.getLifecycle();
        if (lifecycle.getCurrentState() != Lifecycle.State.DESTROYED) {
            observers.add(observer);
            lifecycle.addObserver((LifecycleEventObserver) (source, event) -> {
                if (Lifecycle.Event.ON_DESTROY == event) {
                    observers.remove(observer);
                }
            });
        }
    }

    @AnyThread
    public void post(T value) {
        if (mainLooper.getThread().equals(Thread.currentThread())) {
            for (Observer<T> observer : observers) {
                observer.onChanged(value);
            }
        } else {
            mainThread.post(() -> {
                for (Observer<T> observer : observers) {
                    observer.onChanged(value);
                }
            });
        }
    }
}
