package org.kexie.android.dng.common.widget;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("WeakerAccess")
public class GenericQuickAdapter<X>
        extends BaseQuickAdapter<X, GenericQuickAdapter.GenericViewHolder> {

    protected final int name;

    public GenericQuickAdapter(int layoutResId, int name) {
        super(layoutResId);
        this.name = name;
        openLoadAnimation();
    }

    @Override
    protected void convert(GenericViewHolder helper, X item) {
        if (item != null) {
            helper.getBinding().setVariable(name, item);
        }
    }

    public static class GenericViewHolder extends BaseViewHolder {
        private ViewDataBinding binding;

        public GenericViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
        }

        @SuppressWarnings("unchecked")
        public <T extends ViewDataBinding> T getBinding() {
            return (T) binding;
        }
    }

    public void observe(LifecycleOwner owner, Observer<Collection<X>> observer) {
        Lifecycle lifecycle = owner.getLifecycle();
        if (!Lifecycle.State.DESTROYED.equals(lifecycle.getCurrentState())) {
            ObserverWrapper wrapper = new ObserverWrapper(observer);
            owner.getLifecycle().addObserver(wrapper);
            registerAdapterDataObserver(wrapper);
        }
    }

    private final class ObserverWrapper
            extends RecyclerView.AdapterDataObserver
            implements LifecycleEventObserver {

        private final Observer<Collection<X>> observer;

        private ObserverWrapper(Observer<Collection<X>> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged() {
            observer.onChanged(getData());
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                source.getLifecycle().removeObserver(this);
                unregisterAdapterDataObserver(this);
            }
        }
    }
}
