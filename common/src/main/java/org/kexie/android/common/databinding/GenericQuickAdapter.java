package org.kexie.android.common.databinding;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Objects;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

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
        Objects.requireNonNull(binding).setVariable(mName,item);
    }
}