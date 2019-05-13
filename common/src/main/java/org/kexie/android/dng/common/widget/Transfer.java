package org.kexie.android.dng.common.widget;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public abstract class Transfer {

    protected abstract Observable<Event> transform(Method method, Observable<Event> observable);

    public Transfer connectWith(Transfer transfer) {
        return connect(this, transfer);
    }

    public static Transfer debounce(long timeout, TimeUnit unit, Scheduler scheduler) {
        return new Transfer() {
            @Override
            protected Observable<Event> transform(Method method, Observable<Event> observable) {
                return observable.debounce(timeout, unit, scheduler);
            }
        };
    }

    public static Transfer connect(Transfer tf1, Transfer tf2) {
        return new Transfer() {
            @Override
            protected Observable<Event> transform(Method method, Observable<Event> observable) {
                return tf2.transform(method, tf1.transform(method, observable));
            }
        };
    }
}
