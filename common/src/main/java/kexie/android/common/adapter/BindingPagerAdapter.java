package kexie.android.common.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import kexie.android.common.util.DataBindingCompat;

public class BindingPagerAdapter<T>
        extends PagerAdapter
        implements BindingViewAdapter<T>
{
    private final String setterName;

    private final int layoutRes;

    private final SparseArray<ViewDataBinding> using;

    private final List<T> data;

    private final Stack<ViewDataBinding> bindingCache;

    public BindingPagerAdapter(String variableName, @LayoutRes int layoutRes)
    {
        this.setterName= variableName;
        this.layoutRes = layoutRes;
        this.using = new SparseArray<>();
        this.data = new ArrayList<>();
        this.bindingCache = new Stack<>();
    }

    @NonNull
    @Override
    public View instantiateItem(@NonNull ViewGroup container,
                                  int position)
    {
        ViewDataBinding binding = using.get(position);
        if (binding == null)
        {
            binding = newBinding(container);
            using.put(position, binding);
        }
        DataBindingCompat.setVariable(binding, setterName, data.get(position));
        View view = binding.getRoot();
        container.addView(view);
        return view;
    }

    private ViewDataBinding newBinding(ViewGroup root)
    {
        if (!bindingCache.isEmpty())
        {
            return bindingCache.pop();
        } else
        {
            return DataBindingUtil.inflate(
                    LayoutInflater.from(root.getContext()),
                    layoutRes,
                    root,
                    false);
        }
    }

    @Override
    public final void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull Object object)
    {
        destroyItem(container, position, (View) object);
    }

    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull View view)
    {
        container.removeView(view);
        using.remove(position);
        bindingCache.push(DataBindingUtil.bind(view));
    }

    public List<T> getData()
    {
        return data;
    }

    public void setNewData(@Nullable List<T> data)
    {
        this.data.clear();
        if (data != null)
        {
            this.data.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
    {
        return view == object;
    }
}
