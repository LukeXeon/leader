package org.kexie.android.databinding;

import androidx.databinding.BindingAdapter;
import androidx.annotation.LayoutRes;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public final class GenericItemViewBindingAdapter
{
    private GenericItemViewBindingAdapter()
    {
        throw new AssertionError();
    }

    private interface ViewPagerCompat
    {
        <A extends PagerAdapter> A getAdapter();

        <A extends PagerAdapter> void setAdapter(A adapter);
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter({"item_name",
            "item_layout",
            "item_source"})
    public static void setItems(RecyclerView view,
                                String itemName,
                                @LayoutRes int itemLayout,
                                List<?> dataSource)
    {
        GenericItemRecyclerAdapter adapter
                = (GenericItemRecyclerAdapter) view.getAdapter();
        if (adapter == null
                || !adapter.setterName.equals(itemName)
                || adapter.layoutRes != itemLayout)
        {
            adapter = new GenericItemRecyclerAdapter(itemName, itemLayout);
            view.setAdapter(adapter);
        }
        adapter.setNewData(dataSource);
    }


    @BindingAdapter({"item_name",
            "item_layout",
            "item_source"})
    public static void setItems(final ViewPager view,
                                String itemName,
                                @LayoutRes int itemLayout,
                                List<?> dataSource)
    {
        setItems(new ViewPagerCompat()
        {
            @Override
            public <A extends PagerAdapter> void setAdapter(A adapter)
            {
                view.setAdapter(adapter);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <A extends PagerAdapter> A getAdapter()
            {
                return (A) view.getAdapter();
            }
        }, itemName, itemLayout, dataSource);
    }

    @BindingAdapter({"item_name",
            "item_layout",
            "item_source"})
    public static void setItems(final VerticalViewPager view,
                                String itemName,
                                @LayoutRes int itemLayout,
                                List<?> dataSource)
    {
        setItems(new ViewPagerCompat()
        {
            @Override
            public <A extends PagerAdapter> void setAdapter(A adapter)
            {
                view.setAdapter(adapter);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <A extends PagerAdapter> A getAdapter()
            {
                return (A) view.getAdapter();
            }
        }, itemName, itemLayout, dataSource);
    }

    @SuppressWarnings("unchecked")
    private static void setItems(ViewPagerCompat view,
                                 String itemName,
                                 @LayoutRes int itemLayout,
                                 List<?> dataSource)
    {
        GenericItemPagerAdapter adapter = view.getAdapter();
        if (adapter == null
                || !adapter.setterName.equals(itemName)
                || adapter.layoutRes != itemLayout)
        {
            adapter = new GenericItemPagerAdapter(itemName, itemLayout);
            view.setAdapter(adapter);
        }
        adapter.setNewData(dataSource);
    }
}
