package org.kexie.android.dng.common.widget;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.Lifecycle;
import io.reactivex.android.schedulers.AndroidSchedulers;

public final class RxUtils {
    private RxUtils() {
        throw new AssertionError();
    }

    public static <X> X debounce(Class<X> type, Lifecycle lifecycle, X event) {
        return RxOnClick.Builder.form(type)
                .onEvent(event)
                .apply(Transfer.debounce(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()))
                .lifecycle(lifecycle, Lifecycle.Event.ON_DESTROY)
                .build()
                .get();
    }
}
