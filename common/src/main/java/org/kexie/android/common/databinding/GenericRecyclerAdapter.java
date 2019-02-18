package org.kexie.android.common.databinding;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.kexie.android.common.R;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

public class GenericRecyclerAdapter<T>
        extends BaseQuickAdapter<T,BaseViewHolder>
{
    private final String setterName;

    @SuppressWarnings("WeakerAccess")
    public GenericRecyclerAdapter(String variableName,
                                  @LayoutRes int layoutResId)
    {
        super(layoutResId);
        this.setterName = variableName;
        openLoadAnimation();
    }

    @Override
    protected void convert(BaseViewHolder helper, Object data)
    {
        try
        {
            ViewDataBinding binding = DataBindingUtil.bind(helper.itemView);
            assert binding != null;
            DataBindingReflectionUtil.setVariable(binding, setterName, data);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @BindingAdapter(value = {"item_name", "item_layout"})
    public static void setItemAdapter(RecyclerView view,
                                      String itemName,
                                      @LayoutRes int itemLayout)
    {
        GenericRecyclerAdapter adapter
                = new GenericRecyclerAdapter(itemName, itemLayout);
        view.setAdapter(adapter);
        getLiveAdapter(view).setValue(adapter);
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter({"item_source"})
    public static void setItemSource(RecyclerView view,
                                     List dataSource)
    {
        observe(view, adapter -> adapter.setNewData(dataSource));
    }

    @BindingAdapter(value = {"empty_layout"})
    public static void setEmptyView(RecyclerView view, int layout)
    {
        observe(view, adapter -> adapter.setEmptyView(layout, view));
    }

    @BindingAdapter(value = {"onItemClick"})
    public static void setOnItemClickListener(RecyclerView view,
                                              OnItemClickListener listener)
    {
        observe(view, adapter -> adapter.setOnItemClickListener(listener));
    }

    @BindingAdapter(value = {"onItemLongClick"})
    public static void
    setOnItemLongClickListener(RecyclerView view,
                               OnItemLongClickListener listener)
    {
        observe(view, adapter -> adapter.setOnItemLongClickListener(listener));
    }

    @BindingAdapter(value = {"onItemChildClick"})
    public static void
    setOnItemChildClickListener(RecyclerView view,
                                OnItemChildClickListener listener)
    {
        observe(view, adapter -> adapter.setOnItemChildClickListener(listener));
    }

    @BindingAdapter(value = {"onItemChildLongClick"})
    public static void
    setOnItemChildLongClickListener(RecyclerView view,
                                    OnItemChildLongClickListener listener)
    {
        observe(view, adapter -> adapter.setOnItemChildLongClickListener(listener));
    }

    private static void observe(RecyclerView view,
                                Observer<GenericRecyclerAdapter> consumer)
    {
        MutableLiveData<GenericRecyclerAdapter> liveData = getLiveAdapter(view);
        liveData.observeForever(new Observer<GenericRecyclerAdapter>()
        {
            @Override
            public void onChanged(GenericRecyclerAdapter adapter)
            {
                if (consumer != null)
                {
                    consumer.onChanged(adapter);
                }
                liveData.removeObserver(this);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static MutableLiveData<GenericRecyclerAdapter>
    getLiveAdapter(RecyclerView view)
    {
        MutableLiveData<GenericRecyclerAdapter> liveData
                = (MutableLiveData<GenericRecyclerAdapter>)
                view.getTag(R.id.live_adapter);
        if (liveData == null)
        {
            liveData = new MutableLiveData<>();
            view.setTag(R.id.live_adapter, liveData);
        }
        return liveData;
    }
}