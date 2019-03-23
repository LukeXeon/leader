package org.kexie.android.dng.ai.widget;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.kexie.android.dng.ai.R;

import java.util.Objects;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public final class WaveformView2
        extends FrameLayout
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
            mView = Initializer.createView();
        }

        public WaveformView2 attachTo(FrameLayout viewGroup)
        {
            viewGroup.addView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            Context context = viewGroup.getContext();
            LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(mView.mObserver);
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

    private final WebView mWebView;
    private final AppCompatImageView mImageView;
    private final LifecycleObserver mObserver;

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled"})
    private WaveformView2(Context context)
    {
        super(context);
        mWebView = new WebView(context);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // chromium, enable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else
        {
            // older android version, disable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.loadUrl("file:///android_asset/voicewave.html");
        LayoutParams layoutParams1 = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams1.gravity = Gravity.CENTER;
        mWebView.setLayoutParams(layoutParams1);
        mImageView = new AppCompatImageView(context);
        LayoutParams layoutParams2 = new LayoutParams(0, 0);
        layoutParams2.gravity = Gravity.CENTER;
        mImageView.setLayoutParams(layoutParams2);
        mImageView.setScaleType(AppCompatImageView.ScaleType.CENTER_INSIDE);
        Glide.with(this)
                .load(R.drawable.yy)
                .listener(new RequestListener<Drawable>()
                {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource)
                    {
                        Bitmap bitmap = BitmapFactory
                                .decodeResource(getResources(), R.drawable.yy);
                        post(() -> mImageView.setImageBitmap(bitmap));
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource)
                    {
                        post(() -> mImageView.setImageDrawable(resource));
                        return true;
                    }
                })
                .submit();
        addView(mImageView);
        addView(mWebView);
        mObserver = (LifecycleEventObserver) (source, event) -> {
            switch (event)
            {
                case ON_PAUSE:
                {
                    mWebView.onPause();
                }
                break;
                case ON_RESUME:
                {
                    mWebView.onResume();
                }
                break;
            }
        };
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LayoutParams layoutParams = (LayoutParams) mImageView
                .getLayoutParams();
        layoutParams.width = getMeasuredHeight();
        layoutParams.height = getMeasuredHeight();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("ObsoleteSdkInt")
    public void stop()
    {
        mWebView.evaluateJavascript("javascript:SW9.stop(\"\")",
                value -> mImageView.setVisibility(VISIBLE));
        mWebView.removeAllViews();
        mWebView.clearHistory();
        //clearCache(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            mWebView.clearView();
        } else
        {
            mWebView.loadUrl("about:blank");
        }
        mWebView.freeMemory();
        mWebView.pauseTimers();
        mWebView.loadUrl("file:///android_asset/voicewave.html");
    }

    public void prepare()
    {
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
        mImageView.setVisibility(INVISIBLE);
        mWebView.evaluateJavascript("javascript:SW9.setAmplitude(\""
                        + Math.min(Math.max(value, 0.1f), 1f)
                        + "\")",
                null);
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
