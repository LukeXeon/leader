package kexie.android.dng.adapter;

import kexie.android.common.adapter.BindingRecyclerAdapter;
import kexie.android.dng.R;
import kexie.android.dng.entity.desktop.Function;

public class DesktopFunctionAdapter
        extends BindingRecyclerAdapter<Function>
{
    public DesktopFunctionAdapter()
    {
        super("function", R.layout.item_desktop_function);
    }
}
