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
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;

import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.ai.databinding.ViewWaveform2Binding;

import java.util.Objects;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

@SuppressLint("ViewConstructor")
public final class WaveformView2
        extends FrameLayout
{

    public enum Provider {
        /**
         * Jvm级别单例
         */
        INSTANCE;

        private final WaveformView2 mView;

        Provider() {
            mView = new WaveformView2(Initializer.createInner());
        }

        public WaveformView2 attachTo(FrameLayout viewGroup) {
            viewGroup.addView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            Context context = viewGroup.getContext();
            LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(mView.mObserver);
            contextWrapper.setBaseContext(context);
            Glide.with(mView).load(R.drawable.yy)
                    .into(mView.mBinding.icon);
            return mView;
        }

        public void detach() {
            FrameLayout parent = (FrameLayout) mView.getParent();
            parent.removeView(mView);
            MutableContextWrapper contextWrapper = (MutableContextWrapper) mView.getContext();
            contextWrapper.setBaseContext(Initializer.mApplication);
            mView.mBinding.icon.setImageDrawable(null);
        }
    }

    private final ViewWaveform2Binding mBinding;

    private final LifecycleObserver mObserver;

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled"})
    private WaveformView2(ViewWaveform2Binding binding) {
        super(binding.getRoot().getContext());
        mBinding = binding;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        WebSettings settings = mBinding.webView.getSettings();
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
            mBinding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            mBinding.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mBinding.webView.setWebChromeClient(new WebChromeClient());
        mBinding.webView.setBackgroundColor(Color.TRANSPARENT);
        mBinding.webView.setVerticalScrollBarEnabled(false);
        mBinding.webView.setHorizontalScrollBarEnabled(false);
        mBinding.webView.loadUrl("file:///android_asset/voicewave.html");
        mObserver = (LifecycleEventObserver) (source, event) -> {
            switch (event) {
                case ON_PAUSE: {
                    mBinding.webView.onPause();
                }
                break;
                case ON_RESUME: {
                    mBinding.webView.onResume();
                }
                break;
            }
        };
        addView(mBinding.getRoot());
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("ObsoleteSdkInt")
    public void stop()
    {
        mBinding.webView.evaluateJavascript("javascript:SW9.stop(\"\")",
                value -> mBinding.icon.setVisibility(VISIBLE));
        mBinding.webView.removeAllViews();
        mBinding.webView.clearHistory();
        //clearCache(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            mBinding.webView.clearView();
        } else
        {
            mBinding.webView.loadUrl("about:blank");
        }
        mBinding.webView.freeMemory();
        mBinding.webView.pauseTimers();
        mBinding.webView.loadUrl("file:///android_asset/voicewave.html");
    }

    public void prepare()
    {
        DisplayMetrics displayMetrics = getContext()
                .getResources()
                .getDisplayMetrics();
        mBinding.webView.evaluateJavascript("javascript:SW9.setWidth(\""
                        + displayMetrics.widthPixels * 92 / 100
                        + "\");"
                        + "javascript:SW9.start(\"\");",
                null);
    }

    public void setAmplitude(@FloatRange(from = 0.1f, to = 1f) float value)
    {
        mBinding.icon.setVisibility(INVISIBLE);
        mBinding.webView.evaluateJavascript("javascript:SW9.setAmplitude(\""
                        + Math.min(Math.max(value, 0.1f), 1f)
                        + "\")",
                null);
    }

    public static final class Initializer extends ContentProvider
    {
        private static Application mApplication;

        private static ViewWaveform2Binding createInner() {
            LayoutInflater inflater = LayoutInflater.from(mApplication)
                    .cloneInContext(new MutableContextWrapper(mApplication));
            return DataBindingUtil.inflate(inflater, R.layout.view_waveform2,
                    new FrameLayout(mApplication),
                    false);
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
