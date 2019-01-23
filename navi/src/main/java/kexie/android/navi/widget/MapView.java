package kexie.android.navi.widget;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.util.AttributeSet;

public class MapView
        extends com.amap.api.maps.MapView
        implements LifecycleObserver
{
    public MapView(Context context)
    {
        super(context);
        init();
    }

    public MapView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init();
    }

    public MapView(Context context, AttributeSet attributeSet, int i)
    {
        super(context, attributeSet, i);
        init();
    }

    private void init()
    {
        LifecycleOwner owner = (LifecycleOwner) getContext();
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    final void onCreate0()
    {
        this.onCreate(null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    final void onResume0()
    {
        this.onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    final void onPause0()
    {
        this.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    final void onDestroy0()
    {
        this.onDestroy();
    }
}
