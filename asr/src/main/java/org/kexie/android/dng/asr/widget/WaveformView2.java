package org.kexie.android.dng.asr.widget;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.FrameLayout;

import com.dnkilic.waveform.WaveView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public final class WaveformView2
        extends WaveView
        implements LifecycleEventObserver
{

    public enum Provider
    {
        /**
         * Jvm级别单例
         */
        INSTANCE;

        private final WaveformView2 mView;

        Provider()
        {
            Log.d(WaveformView2.class.getSimpleName(), "Provider: init");
            mView = Initializer.createView();
        }

        public WaveformView2 setTo(FrameLayout viewGroup)
        {
            viewGroup.addView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            Context context = viewGroup.getContext();
            LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(mView);
            contextWrapper.setBaseContext(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            mView.setLayoutParams(layoutParams);
            return mView;
        }

        public void release()
        {
            FrameLayout parent = (FrameLayout) mView.getParent();
            parent.removeView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            contextWrapper.setBaseContext(Initializer.mApplication);
        }
    }

    private WaveformView2(Context context)
    {
        super(context, null);
        WebSettings settings = getSettings();
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        //开启DOM缓存，关闭的话H5自身的一些操作是无效的
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setBlockNetworkImage(true);
        //这个是国外网站Stack Overflow推荐提升加载速度的方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // chromium, enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else
        {
            // older android version, disable hardware acceleration
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void speechPrepare()
    {
        initialize(getContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event)
    {
        switch (event)
        {
            case ON_PAUSE:
            {
                onPause();
            }
            break;
            case ON_RESUME:
            {
                onResume();
            }
            break;
        }
    }

    public static final class Initializer extends ContentProvider
    {
        private static Application mApplication;

        private static WaveformView2 createView()
        {
            return new WaveformView2(new MutableContextWrapper(mApplication));
        }

        @Override
        public boolean onCreate()
        {
            mApplication = (Application) Objects.requireNonNull(getContext())
                    .getApplicationContext();
            return Provider.values().length == 1;
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri,
                            @Nullable String[] projection,
                            @Nullable String selection,
                            @Nullable String[] selectionArgs,
                            @Nullable String sortOrder)
        {
            return null;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri)
        {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri,
                          @Nullable ContentValues values)
        {
            return null;
        }

        @Override
        public int delete(@NonNull Uri uri,
                          @Nullable String selection,
                          @Nullable String[] selectionArgs)
        {
            return 0;
        }

        @Override
        public int update(@NonNull Uri uri,
                          @Nullable ContentValues values,
                          @Nullable String selection,
                          @Nullable String[] selectionArgs)
        {
            return 0;
        }
    }
}
