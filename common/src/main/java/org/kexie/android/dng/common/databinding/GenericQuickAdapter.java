package org.kexie.android.dng.common.databinding;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

//ViewModel拥有数据的控制权
public class GenericQuickAdapter<X>
        extends BaseQuickAdapter<X, BaseViewHolder>
{

    private final int mName;

    @SuppressWarnings("WeakerAccess")
    public GenericQuickAdapter(int layoutId, int name)
    {
        super(layoutId);
        openLoadAnimation();
        this.mName = name;
    }

    @Override
    protected void convert(BaseViewHolder helper, X item)
    {
        ViewDataBinding binding = DataBindingUtil.bind(helper.itemView);
        Objects.requireNonNull(binding).setVariable(mName, item);
    }

    public interface OnItemClickListener<X>
    {
        void onItemClick(GenericQuickAdapter<X> adapter, View view, int position);
    }

    public static final class RxOnItemClick<X>
            implements BaseQuickAdapter.OnItemClickListener
    {

        private final PublishSubject<Object[]> subject = PublishSubject.create();

        @SuppressWarnings("unchecked")
        public RxOnItemClick(LifecycleOwner lifecycleOwner, OnItemClickListener<X> listener)
        {
            subject.throttleFirst(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(autoDisposable(from(lifecycleOwner, Lifecycle.Event.ON_DESTROY)))
                    .subscribe((pack) -> listener.onItemClick(
                            (GenericQuickAdapter<X>) pack[0],
                            (View) pack[1],
                            (int) pack[2])
                    );
        }

        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position)
        {
            subject.onNext(new Object[]{adapter, view, position});
        }
    }
}