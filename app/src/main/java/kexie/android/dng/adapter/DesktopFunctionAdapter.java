package kexie.android.dng.adapter;

import android.databinding.DataBindingUtil;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import kexie.android.dng.R;
import kexie.android.dng.databinding.ItemDesktopFunctionBinding;
import kexie.android.dng.entity.desktop.Function;

public class DesktopFunctionAdapter
        extends BaseQuickAdapter<Function,BaseViewHolder>
{
    public DesktopFunctionAdapter()
    {
        super(R.layout.item_desktop_function);
    }

    @Override
    protected void convert(BaseViewHolder helper, Function item)
    {
        ItemDesktopFunctionBinding binding = DataBindingUtil.bind(helper.itemView);
        assert binding != null;
        binding.setFunction(item);
    }
}
