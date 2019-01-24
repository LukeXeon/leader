package kexie.android.common.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;


import kexie.android.common.util.DataBindingCompat;

public class BindingRecyclerAdapter<T>
        extends BaseQuickAdapter<T,BaseViewHolder>
        implements BindingViewAdapter<T>
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
            DataBindingCompat.setVariable(DataBindingUtil.bind(helper.itemView),
                    setterName, data);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
