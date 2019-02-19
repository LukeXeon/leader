package org.kexie.android.common.databinding;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import org.kexie.android.common.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableList;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import java8.util.stream.IntStreams;

//ViewModel拥有数据的控制权
public class GenericBindingAdapter<X>
        extends BaseQuickAdapter<X, BaseViewHolder>
{

    private ObservableList<X> mObservableData;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final SyncCallback mSyncCallback = new SyncCallback();

    private String mName;

    @SuppressWarnings("WeakerAccess")
    public GenericBindingAdapter()
    {
        super(0, new ArrayList<>());
    }

    @NonNull
    @Override
    public ObservableList<X> getData()
    {
        return mObservableData;
    }

    public void setName(String mName)
    {
        this.mName = mName;
    }

    @Override
    public void addData(@NonNull X data)
    {
        mObservableData.add(data);
    }

    @Override
    public void addData(@NonNull Collection<? extends X> newData)
    {
        mObservableData.addAll(newData);
    }

    @Override
    public void addData(int position, @NonNull X data)
    {
        mObservableData.add(position, data);
    }

    @Override
    public void addData(int position, @NonNull Collection<? extends X> newData)
    {
        mObservableData.addAll(position, newData);
    }

    @Override
    public void setData(int index, @NonNull X data)
    {
        mObservableData.add(index, data);
    }

    @Override
    public void replaceData(@NonNull Collection<? extends X> data)
    {
        if (data instanceof ObservableList)
        {
            if (mObservableData != data)
            {
                mObservableData.clear();
                mObservableData.addAll(data);
            }
        } else
        {
            throw new IllegalStateException();
        }
    }

    @Override
    public void setNewData(@Nullable List<X> data)
    {
        if (data instanceof ObservableList)
        {
            if (mObservableData != null)
            {
                mObservableData.removeOnListChangedCallback(mSyncCallback);
            }
            mObservableData = (ObservableList<X>) data;
            mObservableData.addOnListChangedCallback(mSyncCallback);
        } else
        {
            throw new IllegalStateException();
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, X item)
    {
        ViewDataBinding binding = DataBindingUtil.bind(helper.itemView);
        DataBindingReflectionUtil.setVariable(binding, mName, item);
    }

    private final class SyncCallback
            extends OnListChangedCallback<ObservableList<X>>
    {

        @Override
        public void onChanged(ObservableList<X> sender)
        {
            runOnMainThread(() -> onChangedSync(sender));
        }

        @Override
        public void onItemRangeChanged(ObservableList<X> sender,
                                       int positionStart,
                                       int itemCount)
        {
            runOnMainThread(() -> onItemRangeChangedSync(
                    sender,
                    positionStart,
                    itemCount
            ));
        }

        @Override
        public void onItemRangeInserted(ObservableList<X> sender,
                                        int positionStart,
                                        int itemCount)
        {
            runOnMainThread(() -> onItemRangeInsertedSync(
                    sender,
                    positionStart,
                    itemCount
            ));
        }

        @Override
        public void onItemRangeRemoved(ObservableList<X> sender,
                                       int positionStart,
                                       int itemCount)
        {
            runOnMainThread(() -> onItemRangeRemovedSync(
                    sender,
                    positionStart,
                    itemCount
            ));
        }
    }

    private void runOnMainThread(Runnable run)
    {
        if (Looper.getMainLooper().equals(Looper.myLooper()))
        {
            run.run();
        } else
        {
            mHandler.post(run);
        }
    }

    private void onChangedSync(ObservableList<X> sender)
    {
        super.replaceData(sender);
    }

    private void onItemRangeChangedSync(ObservableList<X> sender,
                                        int positionStart,
                                        int itemCount)
    {
        if (itemCount == 1)
        {
            super.setData(positionStart, sender.get(positionStart));
        } else
        {
            IntStreams.iterate(positionStart,
                    index -> index < itemCount,
                    index -> index + 1)
                    .boxed()
                    .forEach(index -> super.setData(index, sender.get(index)));
        }
    }

    private void onItemRangeInsertedSync(ObservableList<X> sender,
                                         int positionStart,
                                         int itemCount)
    {
        if (itemCount == 1)
        {
            super.addData(positionStart,
                    sender.get(positionStart));
            Logger.d(sender.get(positionStart) + " " + positionStart + " " + itemCount);
        } else
        {
            List<X> newData = sender.subList(positionStart,
                    positionStart + itemCount - 1);
            Logger.d(newData + " " + positionStart + " " + itemCount);
            super.addData(positionStart, newData);
        }
    }


    private void onItemRangeRemovedSync(ObservableList<X> sender,
                                        int positionStart,
                                        int itemCount)
    {
        if (itemCount == 1)
        {
            super.remove(positionStart);
        } else
        {
            List<X> data = super.getData();
            Logger.d(positionStart + " " + itemCount + " " + data);
            int internalPositionStart = positionStart
                    + super.getHeaderLayoutCount();
            data.removeAll(data.subList(positionStart,
                    positionStart + itemCount - 1));
            super.notifyItemRangeChanged(internalPositionStart,
                    data.size() - internalPositionStart);
        }
    }

    private static void setAdapter(View view,
                                   GenericBindingAdapter adapter)
    {
        try
        {
            view.getClass().getMethod("setAdapter",
                    RecyclerView.Adapter.class)
                    .invoke(view, adapter);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static GenericBindingAdapter with(View view)
    {
        try
        {
            GenericBindingAdapter adapter = (GenericBindingAdapter)
                    view.getTag(R.id.generic_binding_adapter);
            if (adapter == null)
            {
                adapter = new GenericBindingAdapter();
                view.setTag(R.id.generic_binding_adapter, adapter);
            }
            return adapter;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter(value = {"item_name", "item_layout", "item_source"})
    public static void setAdapter(RecyclerView view,
                                  String itemName,
                                  int itemLayout,
                                  ObservableList data)
    {
        GenericBindingAdapter adapter = with(view);
        adapter.mName = Objects.requireNonNull(itemName);
        adapter.mLayoutResId = itemLayout;
        adapter.setNewData(data);
        setAdapter(view, adapter);
    }

    @BindingAdapter(value = {"empty_layout"})
    public static void setEmptyView(RecyclerView view, int layout)
    {
        with(view).setEmptyView(layout, view);
    }

    @BindingAdapter(value = {"onItemClick"})
    public static void setOnItemClickListener(RecyclerView view,
                                              OnItemClickListener listener)
    {
        with(view).setOnItemClickListener(listener);
    }

    @BindingAdapter(value = {"onItemLongClick"})
    public static void
    setOnItemLongClickListener(RecyclerView view,
                               OnItemLongClickListener listener)
    {
        with(view).setOnItemLongClickListener(listener);
    }

    @BindingAdapter(value = {"onItemChildClick"})
    public static void
    setOnItemChildClickListener(RecyclerView view,
                                OnItemChildClickListener listener)
    {
        with(view).setOnItemChildClickListener(listener);
    }

    @BindingAdapter(value = {"onItemChildLongClick"})
    public static void
    setOnItemChildLongClickListener(RecyclerView view,
                                    OnItemChildLongClickListener listener)
    {
        with(view).setOnItemChildLongClickListener(listener);
    }
}
