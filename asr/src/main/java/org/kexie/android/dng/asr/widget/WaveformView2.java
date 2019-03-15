package org.kexie.android.dng.asr.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;

import com.dnkilic.waveform.WaveView;

public class WaveformView2 extends WaveView
{

    public WaveformView2(Context context)
    {
        this(context, null);
    }

    public WaveformView2(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        WebSettings settings = getSettings();
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);//开启DOM缓存，关闭的话H5自身的一些操作是无效的
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

    public void initialize()
    {
        initialize(getContext().getResources().getDisplayMetrics());
    }
}
