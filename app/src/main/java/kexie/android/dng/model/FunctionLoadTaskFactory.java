package kexie.android.dng.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import kexie.android.common.util.ZoomTransformation;
import kexie.android.dng.entity.desktop.Function;

public final class FunctionLoadTaskFactory
{
    private final Context context;
    private final ZoomTransformation transformation;

    private FunctionLoadTaskFactory(Context context,int borderSize)
    {
        this.context = context;
        transformation = new ZoomTransformation(borderSize);
    }

    public static FunctionLoadTaskFactory form(Context context,int borderSize)
    {
        return new FunctionLoadTaskFactory(context.getApplicationContext(), borderSize);
    }

    public FunctionLoadTask create(String name,
                                   int mipmap,
                                   final View.OnClickListener action)
    {
        return new FunctionLoadTask(name, mipmap, action);
    }

    public final class FunctionLoadTask
    {
        private final FutureTarget<Drawable> innerTask;
        private Function function;

        private FunctionLoadTask(final String name,
                                 final int mipmap,
                                 final View.OnClickListener action)
        {
            this.innerTask = Glide.with(context)
                    .load(mipmap)
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .listener(new RequestListener<Drawable>()
                    {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e,
                                                    Object model,
                                                    Target<Drawable> target,
                                                    boolean isFirstResource)
                        {
                            Resources resources = context.getResources();
                            FunctionLoadTask.this.function = new Function.Builder()
                                    .action(action)
                                    .icon(new BitmapDrawable(resources,
                                            BitmapFactory.decodeResource(resources, mipmap)))
                                    .name(name)
                                    .build();
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource,
                                                       Object model,
                                                       Target<Drawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource)
                        {
                            FunctionLoadTask.this.function = new Function.Builder()
                                    .action(action)
                                    .icon(resource)
                                    .name(name)
                                    .build();
                            return true;
                        }
                    }).submit();
        }

        public Function get() throws Exception
        {
            innerTask.get();
            return function;
        }
    }
}
