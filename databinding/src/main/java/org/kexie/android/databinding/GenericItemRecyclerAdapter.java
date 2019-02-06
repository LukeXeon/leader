package org.kexie.android.databinding;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

public class GenericItemRecyclerAdapter<T>
        extends BaseQuickAdapter<T,BaseViewHolder>
{
    final String setterName;

    final int layoutRes;

    @SuppressWarnings("WeakerAccess")
    public GenericItemRecyclerAdapter(String variableName,
                                      @LayoutRes int layoutResId)
    {
        super(layoutResId);
        this.layoutRes = layoutResId;
        this.setterName = variableName;
    }

    @Override
    protected void convert(BaseViewHolder helper, T data)
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
}