package org.kexie.android.dng.navi.widget;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class ScaleTransformer implements ViewPager.PageTransformer
{
    private static final float MIN_SCALE = 0.7f;

    @Override
    public void transformPage(@NonNull View page, float position)
    {
        if (position < -1 || position > 1)
        {
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
        } else
        {
            if (position < 0)
            {
                float scale = 1 + 0.3f * position;
                page.setScaleX(scale);
                page.setScaleY(scale);
            } else
            {
                float scale = 1 - 0.3f * position;
                page.setScaleX(scale);
                page.setScaleY(scale);
            }
        }
    }
}