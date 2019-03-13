package org.kexie.android.dng.common.databinding;

import android.view.View;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.subjects.PublishSubject;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

public final class RxOnClick implements View.OnClickListener
{
    private final PublishSubject<View> subject = PublishSubject.create();

    public RxOnClick(LifecycleOwner lifecycleOwner, View.OnClickListener inner)
    {
        subject.throttleFirst(500, TimeUnit.MILLISECONDS)
                .as(autoDisposable(from(lifecycleOwner, Lifecycle.Event.ON_DESTROY)))
                .subscribe(inner::onClick);
    }

    @Override
    public void onClick(View v)
    {
        subject.onNext(v);
    }
}
