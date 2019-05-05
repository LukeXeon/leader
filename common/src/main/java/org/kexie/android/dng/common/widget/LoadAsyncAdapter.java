package org.kexie.android.dng.common.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.kexie.android.dng.common.R;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

public final class LoadAsyncAdapter
{
    private LoadAsyncAdapter()
    {
        throw new AssertionError();
    }

    @BindingAdapter({"async_src"})
    public static void loadAsyncToSrc(ImageView view, String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        Context context = view.getContext()
                .getApplicationContext();
        int res = context.getResources()
                .getIdentifier(name,
                        "mipmap",
                        context.getPackageName());
        if (res == 0) {
            res = context.getResources()
                    .getIdentifier(name,
                            "drawable",
                            context.getPackageName());
        }
        if (res != 0) {
            loadAsync(Glide.with(view).load(res), view);
        } else {
            loadAsync(Glide.with(view).load(name), view);
        }
    }


    @BindingAdapter({"async_src"})
    public static void loadAsyncToSrc(ImageView view, int id)
    {
        loadAsync(Glide.with(view).load(id), view);
    }

    @BindingAdapter({"async_src"})
    public static void loadAsyncToSrc(ImageView view, Drawable drawable) {
        Glide.with(view).load(drawable).into(view);
    }

    @BindingAdapter({"async_background"})
    public static void loadAsyncToBackground(final View view, String name)
    {
        Context context = view.getContext()
                .getApplicationContext();
        final Resources resources = context.getResources();
        final int res = resources
                .getIdentifier(name, "mipmap", context.getPackageName());
        Glide.with(view).load(res)
                .error(Glide.with(view).load(R.mipmap.image_loading))
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .listener(new RequestListener<Drawable>()
                {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource)
                    {
                        view.post(() -> view.setBackground(new BitmapDrawable(
                                resources,
                                BitmapFactory.decodeResource(resources, res))));
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource)
                    {
                        view.post(() -> view.setBackground(resource));
                        return true;
                    }
                }).submit();
    }

    private static void loadAsync(RequestBuilder<Drawable> builder, ImageView view)
    {
        builder.error(Glide.with(view)
                .load(R.mipmap.image_loading))
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .into(view);
    }
}