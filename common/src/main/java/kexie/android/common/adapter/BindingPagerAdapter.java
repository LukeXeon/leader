package kexie.android.common.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class BindingPagerAdapter<T>
        extends PagerAdapter
{
    private final String setterName;

    private final int layoutRes;

    private final SparseArray<ViewDataBinding> using;

    private final List<T> data;

    private final Stack<ViewDataBinding> bindingCache;

    private Method setter;

    public BindingPagerAdapter(String variableName, @LayoutRes int layoutRes)
    {
        if (TextUtils.isEmpty(variableName))
        {
            throw new IllegalArgumentException();
        }
        this.setterName = "set" + String.valueOf(variableName.charAt(0)
                + variableName.substring(1));
        this.layoutRes = layoutRes;
        this.using = new SparseArray<>();
        this.data = new ArrayList<>();
        this.bindingCache = new Stack<>();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container,
                                  int position)
    {
        ViewDataBinding binding = using.get(position);
        if (binding == null)
        {
            binding = newBinding(container);
            using.put(position, binding);
        }
        setDataToBinding(binding, data.get(position));
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

    private void setDataToBinding(ViewDataBinding binding, T data)
    {
        try
        {
            getSetter(binding, data).invoke(binding, data);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Method getSetter(ViewDataBinding binding,T data)
    {
        if (setter == null)
        {
            for (Method method : binding.getClass().getMethods())
            {
                Class<?>[] parameters = method.getParameterTypes();
                if (method.getName().equals(setterName)
                        && parameters.length == 1
                        && parameters[0].isInstance(data))
                {
                    setter = method;
                    return setter;
                }
            }
            throw new RuntimeException();
        }
        else
        {
            return setter;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull Object object)
    {
        View view = (View) object;
        container.removeView(view);
        using.remove(position);
        bindingCache.push(DataBindingUtil.bind(view));
    }

    public List<T> getData()
    {
        return data;
    }

    public void setNewData(Collection<T> data)
    {
        this.data.clear();
        this.data.addAll(data);
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
