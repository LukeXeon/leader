package org.kexie.android.dng.navi.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.amap.api.col.n3.ck;
import com.amap.api.col.n3.ht;
import com.amap.api.col.n3.on;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.TextureSupportMapFragment;
import com.autonavi.amap.mapcore.interfaces.IAMap;
import com.autonavi.amap.mapcore.interfaces.IMapFragmentDelegate;

import org.kexie.android.dng.navi.R;

import java.lang.reflect.Constructor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public final class AMapCompatFragment extends Fragment
        implements ViewTreeObserver.OnGlobalLayoutListener
{
    private AMap map;
    private IMapFragmentDelegate mapFragmentDelegate;

    public AMapCompatFragment()
    {
    }

    public static TextureSupportMapFragment newInstance()
    {
        return newInstance(new AMapOptions());
    }

    @SuppressWarnings("WeakerAccess")
    public static TextureSupportMapFragment newInstance(AMapOptions var0)
    {
        TextureSupportMapFragment var1 = new TextureSupportMapFragment();
        Bundle var2 = new Bundle();

        Parcel var3;
        try
        {
            var3 = Parcel.obtain();
            var0.writeToParcel(var3, 0);
            var2.putByteArray("MAP_OPTIONS", var3.marshall());
        } catch (Throwable var4)
        {
            var4.printStackTrace();
        }

        var1.setArguments(var2);
        return var1;
    }

    private IMapFragmentDelegate getMapFragmentDelegate(Context var1)
    {
        if (this.mapFragmentDelegate == null)
        {
            try
            {
                this.mapFragmentDelegate = on.a(var1, ht.f(), var1.getString(R.string.amap_inner_class_name),
                        ck.class, new Class[]{Integer.TYPE}, new Object[]{1});
            } catch (Throwable ignored)
            {

            }
            if (this.mapFragmentDelegate == null)
            {
                this.mapFragmentDelegate = new ck(1);
                this.mapFragmentDelegate.setContext(var1);
            }
        }
        return this.mapFragmentDelegate;
    }

    private IMapFragmentDelegate a()
    {
        return this.getMapFragmentDelegate(this.getActivity());
    }

    public AMap getMap()
    {
        IMapFragmentDelegate var1;
        if ((var1 = this.a()) == null)
        {
            return null;
        } else
        {
            IAMap var3;
            try
            {
                var3 = var1.getMap();
            } catch (Throwable var2)
            {
                return null;
            }

            if (var3 == null)
            {
                return null;
            } else
            {
                if (this.map == null)
                {
                    try
                    {
                        Constructor<AMap> constructor = AMap.class
                                .getDeclaredConstructor(IAMap.class);
                        constructor.setAccessible(true);
                        this.map = constructor.newInstance(var3);
                    } catch (Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
                return this.map;
            }
        }
    }

    @Override
    public void onInflate(@NonNull Context var1, @NonNull AttributeSet var2, Bundle var3)
    {
        super.onInflate(var1, var2, var3);
        try
        {
            this.getMapFragmentDelegate(var1).onInflate((Activity) var1, new AMapOptions(), var3);
        } catch (Throwable var4)
        {
            var4.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater var1, ViewGroup var2, Bundle var3)
    {
        try
        {
            if (var3 == null)
            {
                var3 = this.getArguments();
            }
            return this.a().onCreateView(var1, var2, var3);
        } catch (Throwable var4)
        {
            var4.printStackTrace();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        try
        {
            this.a().onResume();
        } catch (Throwable var1)
        {
            var1.printStackTrace();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            this.a().onPause();
        } catch (Throwable var1)
        {
            var1.printStackTrace();
        }
    }

    @Override
    public void onDestroyView()
    {
        try
        {
            this.requireView().getViewTreeObserver()
                    .removeOnGlobalLayoutListener(this);
            this.a().onDestroyView();
        } catch (Throwable var1)
        {
            var1.printStackTrace();
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        try
        {
            this.a().onDestroy();
            this.map = null;
        } catch (Throwable var1)
        {
            var1.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        try
        {
            this.a().onLowMemory();
        } catch (Throwable var1)
        {
            var1.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle var1)
    {
        try
        {
            this.a().onSaveInstanceState(var1);
        } catch (Throwable var2)
        {
            var2.printStackTrace();
        }

        super.onSaveInstanceState(var1);
    }

    @Override
    public void setArguments(Bundle var1)
    {
        try
        {
            super.setArguments(var1);
        } catch (Throwable var2)
        {
            var2.printStackTrace();
        }
    }

    @Override
    public void setUserVisibleHint(boolean var1)
    {
        super.setUserVisibleHint(var1);
        if (var1)
        {
            this.a().setVisibility(0);
        } else
        {
            this.a().setVisibility(8);
        }
    }

    @Override
    public void onGlobalLayout()
    {
        View view = getView();
        if (view instanceof ViewGroup)
        {
            View logo = ((ViewGroup) view).getChildAt(2);
            if (logo != null)
            {
                logo.setVisibility(View.GONE);
            }
        }
    }
}
