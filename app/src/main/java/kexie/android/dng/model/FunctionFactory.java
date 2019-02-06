package kexie.android.dng.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kexie.android.common.util.ZoomTransformation;
import kexie.android.dng.R;
import kexie.android.dng.entity.desktop.Function;

public final class FunctionFactory
{
    private final Context context;
    private final ZoomTransformation transformation;

    public FunctionFactory(Context context, int borderSize)
    {
        this.context = context;
        transformation = new ZoomTransformation(borderSize);
    }

    public List<Function> getDefault()
            throws ExecutionException, InterruptedException
    {
        List<FunctionFactory.LoadTask> targets
                = new LinkedList<LoadTask>()
        {
            {
                add(create("天气",
                        R.mipmap.image_weather,
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {

                            }
                        }));
                add(create("多媒体",
                        R.mipmap.image_media,
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {

                            }
                        }));
                add(create("APPS",
                        R.mipmap.image_apps,
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {

                            }
                        }));
            }
        };
        List<Function> functions = new ArrayList<>(targets.size());
        for (FunctionFactory.LoadTask task : targets)
        {
            functions.add(task.get());
        }
        return functions;
    }

    private LoadTask create(String name,
                           int mipmap,
                           final View.OnClickListener action)
    {
        return new LoadTask(name, mipmap, action);
    }

    private final class LoadTask
            implements RequestListener<Drawable>
    {
        private final FutureTarget<Drawable> innerTask;
        private Function function;
        private final String name;
        private final int mipmap;
        private final View.OnClickListener action;

        private LoadTask(String name,
                         int mipmap,
                         View.OnClickListener action)
        {
            this.name = name;
            this.mipmap = mipmap;
            this.action = action;
            this.innerTask = Glide.with(context)
                    .load(mipmap)
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .listener(this).submit();
        }

        private Function get()
                throws ExecutionException, InterruptedException
        {
            innerTask.get();
            return function;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e,
                                    Object model,
                                    Target<Drawable> target,
                                    boolean isFirstResource)
        {
            Resources resources = context.getResources();
            LoadTask.this.function = new Function.Builder()
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
            this.function = new Function.Builder()
                    .action(action)
                    .icon(resource)
                    .name(name)
                    .build();
            return true;
        }
    }
}
