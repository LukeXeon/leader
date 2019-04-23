package org.kexie.android.dng.common.databinding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.kexie.android.dng.common.R;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

public final class LoadAsyncBindingAdapter
{
    private LoadAsyncBindingAdapter()
    {
        throw new AssertionError();
    }

    private final static Map<String, Typeface> sTypefaceCache
            = new WeakHashMap<>();

    private static final class TypefaceLoadTask
            extends AsyncTask<Void, Void, Typeface>
    {
        private final String name;

        private final WeakReference<TextView> textView;

        @SuppressLint("StaticFieldLeak")
        private final Context context;

        private TypefaceLoadTask(TextView textView, String name)
        {
            super();
            this.name = name;
            this.textView = new WeakReference<>(textView);
            this.context = textView.getContext().getApplicationContext();
        }

        @Override
        protected Typeface doInBackground(Void... voids)
        {
            TextView target = textView.get();
            if (target == null)
            {
                return null;
            }
            return Typeface.createFromAsset(context.getAssets(), name);
        }

        @Override
        protected void onPostExecute(Typeface typeface)
        {
            TextView target = textView.get();
            if (target != null && typeface != null)
            {
                target.setTypeface(typeface);
                sTypefaceCache.put(name, typeface);
            }
        }
    }


    @BindingAdapter({"async_src"})
    public static void loadAsyncToSrc(ImageView view, String name)
    {
        Context context = view.getContext()
                .getApplicationContext();
        int res = context.getResources()
                .getIdentifier(name,
                        "mipmap",
                        context.getPackageName());
        if (res == 0)
        {
            res = context.getResources()
                    .getIdentifier(name,
                            "drawable",
                            context.getPackageName());
        }
        if (res != 0)
        {
            loadAsync(Glide.with(view).load(res), view);
        } else
        {
            loadAsync(Glide.with(view).load(name), view);
        }
    }


    @BindingAdapter({"async_src"})
    public static void loadAsyncToSrc(ImageView view, int id)
    {
        loadAsync(Glide.with(view).load(id), view);
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

    @BindingAdapter({"async_font"})
    public static void loadAsyncToFont(TextView view, String name)
    {
        String path = "font/" + name;
        Typeface typeface = sTypefaceCache.get(path);
        if (typeface != null)
        {
            view.setTypeface(typeface);
        } else
        {
            new TypefaceLoadTask(view, path);
        }
    }
}