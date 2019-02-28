package org.kexie.android.common.databinding;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.ObservableSubscribeProxy;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.ObservableConverter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public final class RxEvent
{
    private RxEvent()
    {
        throw new AssertionError();
    }

    public static <T> ObservableConverter<T, ObservableSubscribeProxy<T>>
    bind(LifecycleOwner owner)
    {
        return x -> x.observeOn(AndroidSchedulers.mainThread())
                .filter(x2 -> owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
                        .from(owner, Lifecycle.Event.ON_DESTROY)));
    }
}
