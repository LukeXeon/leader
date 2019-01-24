package kexie.android.navi.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kexie.android.navi.databinding.ItemRouteBinding;

public class RouteBindingAdapter extends PagerAdapter
{
    private List<ItemRouteBinding> bindings;


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container,
                                  int position)
    {
        View view = bindings.get(position).getRoot();
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull Object object)
    {
        container.removeView((View) object);
    }

    public void setBindings(List<ItemRouteBinding> bindings)
    {
        this.bindings = bindings;
    }

    public List<ItemRouteBinding> getBindings()
    {
        return bindings;
    }

    @Override
    public int getCount()
    {
        return bindings.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
    {
        return view == object;
    }
}
