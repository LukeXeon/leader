package org.kexie.android.dng.ai.widget;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.util.Objects;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public final class WaveformView2
        extends WebView
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

        public WaveformView2 attachTo(FrameLayout viewGroup)
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

        public void detach()
        {
            FrameLayout parent = (FrameLayout) mView.getParent();
            parent.removeView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            contextWrapper.setBaseContext(Initializer.mApplication);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private WaveformView2(Context context)
    {
        super(context);
        WebSettings settings = getSettings();
        //开启DOM缓存，关闭的话H5自身的一些操作是无效的
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setBlockNetworkImage(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // chromium, enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else
        {
            // older android version, disable hardware acceleration
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        setWebChromeClient(new WebChromeClient());
        setBackgroundColor(Color.TRANSPARENT);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        loadUrl("file:///android_asset/voicewave.html");
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("ObsoleteSdkInt")
    public void stop()
    {
        evaluateJavascript("javascript:SW9.stop(\"\")", null);
        removeAllViews();
        clearHistory();
        //clearCache(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            clearView();
        } else {
            loadUrl("about:blank");
        }
        freeMemory();
        pauseTimers();
        loadUrl("file:///android_asset/voicewave.html");
    }

    public void prepare()
    {
        DisplayMetrics displayMetrics = getContext()
                .getResources()
                .getDisplayMetrics();
        evaluateJavascript("javascript:SW9.setWidth(\""
                        + displayMetrics.widthPixels * 92 / 100
                        + "\");"
                        + "javascript:SW9.start(\"\");",
                null);
    }

    public void setAmplitude(@FloatRange(from = 0.1f, to = 1f) float value)
    {
        evaluateJavascript("javascript:SW9.setAmplitude(\""
                        + Math.min(Math.max(value, 0.1f), 1f)
                        + "\")",
                null);
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
