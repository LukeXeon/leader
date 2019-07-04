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
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.util.Objects;

import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public final class WaveformView
        extends WebView
{

    public enum Provider {
        /**
         * Jvm级别单例
         */
        INSTANCE;

        private final WaveformView mView;

        Provider() {
            mView = Initializer.createView();
        }

        @MainThread
        public WaveformView attachTo(FrameLayout viewGroup) {
            viewGroup.addView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            Context context = viewGroup.getContext();
            LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(mView.mObserver);
            contextWrapper.setBaseContext(context);
            return mView;
        }

        @MainThread
        public void detach() {
            mView.stop();
            FrameLayout parent = (FrameLayout) mView.getParent();
            parent.removeView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            contextWrapper.setBaseContext(Initializer.mApplication);
        }
    }
    private final LifecycleObserver mObserver;

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled"})
    private WaveformView(Context context) {
        super(context);
        WebView mWebView = this;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        WebSettings settings = mWebView.getSettings();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.loadUrl("file:///android_asset/voicewave.html");
        mObserver = (LifecycleEventObserver) (source, event) -> {
            switch (event) {
                case ON_PAUSE: {
                    mWebView.onPause();
                }
                break;
                case ON_RESUME: {
                    mWebView.onResume();
                }
                break;
            }
        };
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("ObsoleteSdkInt")
    public void stop() {
        WebView mWebView = this;
        mWebView.evaluateJavascript("javascript:SW9.stop(\"\")", null);
        mWebView.removeAllViews();
        mWebView.clearHistory();
        //clearCache(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWebView.clearView();
        } else {
            mWebView.loadUrl("about:blank");
        }
        mWebView.freeMemory();
        mWebView.pauseTimers();
        mWebView.loadUrl("file:///android_asset/voicewave.html");
    }

    public void prepare()
    {
        WebView mWebView = this;
        DisplayMetrics displayMetrics = getContext()
                .getResources()
                .getDisplayMetrics();
        mWebView.evaluateJavascript("javascript:SW9.setWidth(\""
                        + displayMetrics.widthPixels * 92 / 100
                        + "\");"
                        + "javascript:SW9.start(\"\");",
                null);
    }

    public void setAmplitude(@FloatRange(from = 0.1f, to = 1f) float value)
    {
        WebView mWebView = this;
        mWebView.evaluateJavascript("javascript:SW9.setAmplitude(\""
                        + Math.min(Math.max(value, 0.1f), 1f)
                        + "\")",
                null);
    }

    public static final class Initializer extends ContentProvider
    {
        private static Application mApplication;

        private static WaveformView createView() {
            return new WaveformView(new MutableContextWrapper(mApplication));
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
