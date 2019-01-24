package kexie.android.common.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;


import kexie.android.common.util.DataBindingCompat;

public class BindingRecyclerAdapter<T>
        extends BaseQuickAdapter<T,BaseViewHolder>
{

    private final String setterName;

    public BindingRecyclerAdapter(String variableName, @LayoutRes int layoutResId)
    {
        super(layoutResId);
        this.setterName = variableName;
    }

    @Override
    protected void convert(BaseViewHolder helper, T data)
    {
        try
        {
            ViewDataBinding binding = DataBindingUtil.bind(helper.itemView);
            assert binding != null;
            DataBindingCompat.setVariable(binding, setterName, data);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
