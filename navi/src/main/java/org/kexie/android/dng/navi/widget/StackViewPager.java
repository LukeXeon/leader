package org.kexie.android.dng.navi.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.bartoszlipinski.flippablestackview.FlippableStackView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class StackViewPager extends FlippableStackView
{
    private final static PagerAdapter EMPTY_ADAPTER = new PagerAdapter()
    {
        @Override
        public int getCount()
        {
            return 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
        {
            return false;
        }
    };

    public StackViewPager(Context context)
    {
        super(context);
    }

    public StackViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter)
    {
        if (adapter == null)
        {
            super.setAdapter(EMPTY_ADAPTER);
        } else
        {
            super.setAdapter(adapter);
        }
    }
}
