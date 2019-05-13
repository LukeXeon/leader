package org.kexie.android.dng.common.widget;


import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import androidx.lifecycle.Lifecycle;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.PublishSubject;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

public final class RxOnClick<X> implements Disposable {
    private final X mProxy;
    private final Disposable mDisposable;

    private RxOnClick(X proxy, Disposable disposable) {
        this.mProxy = proxy;
        this.mDisposable = disposable;
    }

    public X get() {
        return mProxy;
    }

    @Override
    public boolean isDisposed() {
        return mDisposable.isDisposed();
    }

    @Override
    public void dispose() {
        mDisposable.dispose();
    }

    public static final class Builder<X> {

        private final Class<X> mInterface;

        private Transfer mTransfer;

        private X mOnEvent;

        private Lifecycle mLifecycle;

        private Lifecycle.Event mFinishEvent;

        private Consumer<? super Throwable> mOnError = Functions.ON_ERROR_MISSING;

        public static <X> Builder<X> form(Class<X> type) {
            if (type == null || !type.isInterface()) {
                throw new IllegalArgumentException();
            }
            return new Builder<>(type);
        }

        private Builder(Class<X> type) {
            mInterface = type;
        }

        public Builder<X> apply(Transfer transfer) {
            this.mTransfer = transfer;
            return this;
        }

        public Builder<X> onEvent(X inner) {
            if (inner == null) {
                throw new IllegalArgumentException();
            }
            mOnEvent = inner;
            return this;
        }

        public Builder<X> lifecycle(Lifecycle lifecycle, Lifecycle.Event event) {
            if (lifecycle == null || event == null) {
                throw new IllegalArgumentException();
            }
            mLifecycle = lifecycle;
            mFinishEvent = event;
            return this;
        }

        public Builder<X> onError(Consumer<? super Throwable> error) {
            this.mOnError = error;
            return this;
        }

        public RxOnClick<X> build() {
            return build0(
                    mInterface,
                    mOnEvent,
                    mOnError,
                    mTransfer,
                    mLifecycle,
                    mFinishEvent
            );
        }

        @SuppressWarnings("unchecked")
        private static <X> RxOnClick<X>
        build0(Class<X> type,
               X onEvent,
               Consumer<? super Throwable> onError,
               Transfer transfer,
               Lifecycle lifecycle,
               Lifecycle.Event finish) {
            Method[] methods = type.getMethods();
            PublishSubject<Event> subject = PublishSubject.create();
            Observable<Event>[] observables = new Observable[methods.length];
            for (int i = 0; i < methods.length; ++i) {
                Method method = methods[i];
                Observable<Event> observable = subject
                        .filter(event -> method.equals(event.mTarget));
                if (transfer != null) {
                    Observable<Event> out = transfer.transform(method, observable);
                    if (out != null) {
                        observable = out;
                    }
                }
                observables[i] = observable;
            }
            Observable<Event> observable = Observable.mergeArray(observables);
            Consumer<Event> handle = event -> {
                event.mTarget.invoke(onEvent, event.mArgs);
                event.recycle();
            };
            Disposable disposable = lifecycle == null
                    ? observable.subscribe(handle, onError)
                    : observable.as(autoDisposable(from(lifecycle, finish))).subscribe(handle, onError);
            X trueProxy = (X) Proxy.newProxyInstance(
                    type.getClassLoader(), new Class[]{type},
                    (proxy, method, args) -> {
                        if (Object.class.equals(method.getDeclaringClass())) {
                            return method.invoke(proxy, args);
                        }
                        subject.onNext(Event.obtain(method, args));
                        return null;
                    });
            return new RxOnClick<>(trueProxy, disposable);
        }
    }
}