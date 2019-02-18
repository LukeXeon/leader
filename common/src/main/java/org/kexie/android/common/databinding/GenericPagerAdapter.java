package org.kexie.android.common.databinding;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class GenericPagerAdapter<T>
        extends PagerAdapter
{
    private final String setterName;

    private final int layoutRes;

    private final SparseArray<ViewDataBinding> using;

    private final List<T> data;

    private final List<ViewDataBinding> cache;

    private float mPageWidth = 1.f;

    @SuppressWarnings("WeakerAccess")
    public GenericPagerAdapter(String variableName,
                               @LayoutRes int layoutRes)
    {
        this.setterName = variableName;
        this.layoutRes = layoutRes;
        this.using = new SparseArray<>();
        this.data = new ArrayList<>();
        this.cache = new ArrayList<>();
    }

    @NonNull
    @Override
    public View instantiateItem(@NonNull ViewGroup container,
                                int position)
    {
        ViewDataBinding binding = using.get(position);
        if (binding == null)
        {
            binding = getBinding(container);
            using.put(position, binding);
        }
        DataBindingReflectionUtil.setVariable(binding, setterName, data.get(position));
        View view = binding.getRoot();
        container.addView(view);
        return view;
    }

    private ViewDataBinding getBinding(ViewGroup root)
    {
        if (!cache.isEmpty())
        {
            return cache.remove(cache.size() - 1);
        } else
        {
            return DataBindingUtil.inflate(
                    LayoutInflater.from(root.getContext()),
                    layoutRes,
                    root,
                    false
            );
        }
    }

    @Override
    public final void destroyItem(@NonNull ViewGroup container,
                                  int position,
                                  @NonNull Object object)
    {
        destroyItem(container, position, (View) object);
    }

    @SuppressWarnings({"WeakerAccess"})
    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull View view)
    {
        container.removeView(view);
        using.remove(position);
        cache.add(DataBindingUtil.bind(view));
    }

    public List<T> getData()
    {
        return data;
    }

    @SuppressWarnings({"WeakerAccess"})
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
    public boolean isViewFromObject(@NonNull View view,
                                    @NonNull Object object)
    {
        return view == object;
    }

    @Override
    public float getPageWidth(int position)
    {
        return mPageWidth;
    }

    @BindingAdapter(value = {"item_name", "item_layout"})
    public static void setItemAdapter(ViewPager view,
                                      String itemName,
                                      @LayoutRes int itemLayout)
    {
        setItemAdapter((View) view, itemName, itemLayout);
    }

    @BindingAdapter(value = {"item_name", "item_layout"})
    public static void setItemAdapter(VerticalViewPager view,
                                      String itemName,
                                      @LayoutRes int itemLayout)
    {
        setItemAdapter((View) view, itemName, itemLayout);
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter({"item_source"})
    public static void setItemSource(ViewPager view,
                                     List dataSource)
    {
        observe(view, adapter -> adapter.setNewData(dataSource));
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter({"item_source"})
    public static void setItemSource(VerticalViewPager view,
                                     List dataSource)
    {
        observe(view, adapter -> adapter.setNewData(dataSource));
    }

    private static void setItemAdapter(View view,
                                      String itemName,
                                      @LayoutRes int itemLayout)
    {
        GenericPagerAdapter adapter
                = new GenericPagerAdapter<>(itemName, itemLayout);
        setAdapter(view, adapter);
        getLiveAdapter(view).setValue(adapter);
    }

    private static void setAdapter(View view, GenericPagerAdapter adapter)
    {
        try
        {
            Method method = view.getClass()
                    .getMethod("setAdapter", PagerAdapter.class);
            method.invoke(view, adapter);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void observe(View view,
                                Observer<GenericPagerAdapter> consumer)
    {
        MutableLiveData<GenericPagerAdapter> liveData = getLiveAdapter(view);
        liveData.observeForever(new Observer<GenericPagerAdapter>()
        {
            @Override
            public void onChanged(GenericPagerAdapter adapter)
            {
                if (consumer != null)
                {
                    consumer.onChanged(adapter);
                }
                liveData.removeObserver(this);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static MutableLiveData<GenericPagerAdapter>
    getLiveAdapter(View view)
    {
        MutableLiveData<GenericPagerAdapter> liveData
                = (MutableLiveData<GenericPagerAdapter>)
                view.getTag(R.id.live_adapter);
        if (liveData == null)
        {
            liveData = new MutableLiveData<>();
            view.setTag(R.id.live_adapter, liveData);
        }
        return liveData;
    }
}