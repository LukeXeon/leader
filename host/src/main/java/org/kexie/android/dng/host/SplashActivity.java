package org.kexie.android.dng.host;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;

import org.kexie.android.common.util.SystemUtil;
import org.kexie.android.dng.host.databinding.ActivitySplashBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public final class SplashActivity extends AppCompatActivity
{

    private static final String IS_FIRST = "is_first";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        SystemUtil.hideSystemUi(window);
        ActivitySplashBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_splash);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean isFirst = preferences.getBoolean(IS_FIRST, true);
        Runnable jump = () -> {
            ARouter.getInstance()
                    .build("/host/host")
                    .navigation(this);
            finish();
        };
        binding.setIsFirst(isFirst || BuildConfig.DEBUG);
        if (isFirst || BuildConfig.DEBUG)
        {
            SplashAdapter adapter = new SplashAdapter(R.mipmap.image_background, R.mipmap.image_background);
            binding.setAdapter(adapter);
            binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
            {
                @Override
                public void onPageSelected(int position)
                {
                    if (position == adapter.getCount() - 1)
                    {
                        jump.run();
                        preferences.edit()
                                .putBoolean(IS_FIRST, false)
                                .apply();
                    }
                }
            });
        } else
        {
            jump.run();
        }
    }

    private static final class SplashAdapter extends PagerAdapter
    {
        private final int[] imageIds;
        private final AppCompatImageView[] imageViews;
        private AppCompatImageView last;


        private SplashAdapter(int... images)
        {
            this.imageIds = images;
            this.imageViews = new AppCompatImageView[images.length];
        }

        @Override
        public int getCount()
        {
            return imageIds.length + 1;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position)
        {
            Context context = container.getContext();
            View view;
            if (last == null)
            {
                last = new AppCompatImageView(context);
                last.setScaleType(AppCompatImageView.ScaleType.CENTER_CROP);
                Glide.with(last).load(R.mipmap.image_background)
                        .into(last);
            }
            if (imageIds.length == position)
            {
                view = last;
            } else
            {
                AppCompatImageView imageView = imageViews[position];
                if (imageView == null)
                {
                    imageView = new AppCompatImageView(context);
                    imageView.setScaleType(AppCompatImageView.ScaleType.CENTER_CROP);
                    Glide.with(imageView).load(imageIds[position])
                            .into(imageView);
                    imageViews[position] = imageView;
                }
                view = imageView;
            }
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

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
        {
            return view == object;
        }
    }
}
