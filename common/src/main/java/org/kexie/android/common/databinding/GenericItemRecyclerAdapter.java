package org.kexie.android.common.databinding;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class GenericItemRecyclerAdapter<T>
        extends BaseQuickAdapter<T,BaseViewHolder>
{
    private final String setterName;

    @SuppressWarnings("WeakerAccess")
    public GenericItemRecyclerAdapter(String variableName,
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
        if (getAdapter(view) == null)
        {
            view.setAdapter(new GenericItemRecyclerAdapter(itemName, itemLayout));
        }
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter({"item_source"})
    public static void setItemSource(RecyclerView view,
                                     List dataSource)
    {
        getAdapter(view).setNewData(dataSource);
    }

    @BindingAdapter(value = {"empty_layout"})
    public static void setEmptyView(RecyclerView view, int layout)
    {
        getAdapter(view).setEmptyView(layout, view);
    }

    @BindingAdapter(value = {"onItemClick"})
    public static void setOnItemClickListener(RecyclerView view,
                                              OnItemClickListener listener)
    {
        getAdapter(view).setOnItemClickListener(listener);
    }

    @BindingAdapter(value = {"onItemLongClick"})
    public static void
    setOnItemLongClickListener(RecyclerView view,
                               OnItemLongClickListener listener)
    {
        getAdapter(view).setOnItemLongClickListener(listener);
    }

    @BindingAdapter(value = {"onItemChildClick"})
    public static void
    setOnItemChildClickListener(RecyclerView view,
                                OnItemChildClickListener listener)
    {
        getAdapter(view).setOnItemChildClickListener(listener);
    }

    @BindingAdapter(value = {"onItemChildLongClick"})
    public static void
    setOnItemChildLongClickListener(RecyclerView view,
                                    OnItemChildLongClickListener listener)
    {
        getAdapter(view).setOnItemChildLongClickListener(listener);
    }

    private static GenericItemRecyclerAdapter getAdapter(RecyclerView view)
    {
        return (GenericItemRecyclerAdapter) view.getAdapter();
    }

}