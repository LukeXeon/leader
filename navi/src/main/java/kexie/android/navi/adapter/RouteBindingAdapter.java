package kexie.android.navi.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import kexie.android.common.adapter.BindingPagerAdapter;
import kexie.android.common.adapter.BindingRecyclerAdapter;
import kexie.android.common.util.DataBindingCompat;
import kexie.android.navi.R;
import kexie.android.navi.entity.Route;
import kexie.android.navi.entity.Step;

public class RouteBindingAdapter extends BindingPagerAdapter<Route>
{
    public RouteBindingAdapter()
    {
        super("route", R.layout.item_route);
    }

    @NonNull
    @Override
    public View instantiateItem(@NonNull ViewGroup container, int position)
    {
        View view = super.instantiateItem(container, position);
        ViewDataBinding viewDataBinding = DataBindingUtil.bind(view);
        BindingRecyclerAdapter<Step> adapter
                = new BindingRecyclerAdapter<>("step", R.layout.item_step);
        adapter.setNewData(getData().get(position).steps);
        DataBindingCompat.setVariable(viewDataBinding, "adapter", adapter);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull View view)
    {
        ViewDataBinding viewDataBinding = DataBindingUtil.bind(view);
        DataBindingCompat.setVariable(viewDataBinding, "adapter", null);
        super.destroyItem(container, position, view);
    }
}
