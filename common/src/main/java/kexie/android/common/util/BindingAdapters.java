package kexie.android.common.util;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public final class BindingAdapters
{

    private BindingAdapters()
    {
    }

    private static final LruCache<String,Typeface> TYPEFACE_LRU_CACHE
            = new LruCache<String,Typeface>(32)
    {
        @Override
        protected int sizeOf(String key, Typeface value)
        {
            return 1;
        }
    };

    @BindingAdapter({"fontAsset"})
    public static void setFont(final TextView view, String name)
    {
        TYPEFACE_LRU_CACHE.get(name);

        final Context context = view.getContext()
                .getApplicationContext();
        Observable.just(name)
                .observeOn(Schedulers.io())
                .map(new Function<String, Typeface>()
                {
                    @Override
                    public Typeface apply(String s) throws Exception
                    {
                        String path = "fonts/" + s;
                        Typeface typeface = TYPEFACE_LRU_CACHE.get(path);
                        if (typeface == null)
                        {
                            typeface = Typeface.createFromAsset(
                                    context.getAssets(),
                                    path);
                            TYPEFACE_LRU_CACHE.put(path, typeface);
                        }
                        return typeface;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Typeface>()
                {
                    @Override
                    public void accept(Typeface typeface) throws Exception
                    {
                        view.setTypeface(typeface);
                    }
                });
    }

    @BindingAdapter({"mipmap"})
    public static void setMipmap(ImageView view, String name)
    {
        Context context = view.getContext()
                .getApplicationContext();
        Glide.with(context).load(context
                .getResources()
                .getIdentifier(name, "mipmap", context.getPackageName()))
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .into(view);
    }

    @BindingAdapter({"mipmapBackground"})
    public static void setMipmapBackground(final View view, String name)
    {
        Context context = view.getContext()
                .getApplicationContext();
        final Resources resources = context.getResources();
        final int res = resources
                .getIdentifier(name, "mipmap", context.getPackageName());
        Glide.with(context).load(res)
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .listener(new RequestListener<Drawable>()
                {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource)
                    {
                        view.setBackground(new BitmapDrawable(BitmapFactory
                                .decodeResource(resources, res)));
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource)
                    {
                        view.setBackground(resource);
                        return true;
                    }
                }).submit();
    }
}
