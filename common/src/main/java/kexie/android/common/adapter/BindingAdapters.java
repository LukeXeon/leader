package kexie.android.common.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
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
import com.orhanobut.logger.Logger;

import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;
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
            = new LruCache<>(32);


    @BindingAdapter(value = {"adapter_item","adapter_layout"})
    public static void setAdapter(RecyclerView view,
                                  String name,
                                  int layoutRes)
    {
        view.setAdapter(new BindingRecyclerAdapter<>(name, layoutRes));
    }

    @BindingAdapter(value = {"adapter_item","adapter_layout"})
    public static void setAdapter(ViewPager view,
                                  String name,
                                  int layoutRes)
    {
        view.setAdapter(new BindingPagerAdapter<>(name, layoutRes));
    }

    @BindingAdapter(value = {"adapter_item","adapter_layout"})
    public static void setAdapter(VerticalViewPager view,
                                  String name,
                                  int layoutRes)
    {
        view.setAdapter(new BindingPagerAdapter<>(name, layoutRes));
    }

    @BindingAdapter("adapter_data")
    public static void setAdapterData(RecyclerView view, List list)
    {
        setAdapterData(view,(BindingViewAdapter) view.getAdapter(),list);
    }

    @BindingAdapter("adapter_data")
    public static void setAdapterData(VerticalViewPager view, List list)
    {
        setAdapterData(view,(BindingViewAdapter) view.getAdapter(),list);
    }

    @BindingAdapter("adapter_data")
    public static void setAdapterData(ViewPager view, List list)
    {
        setAdapterData(view,(BindingViewAdapter) view.getAdapter(),list);
    }

    @SuppressWarnings("unchecked")
    private static void setAdapterData(View view,BindingViewAdapter adapter, List list)
    {
        if (adapter != null)
        {
            adapter.setNewData(list);
        } else
        {
            Logger.e(view + "no has adapter to bind " + list);
        }
    }

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

    @BindingAdapter({"mipmap"})
    public static void setMipmapId(ImageView view, int id)
    {
        Context context = view.getContext()
                .getApplicationContext();
        Glide.with(context).load(id)
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
