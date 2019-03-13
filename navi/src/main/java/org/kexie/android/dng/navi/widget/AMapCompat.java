package org.kexie.android.dng.navi.widget;

import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.amap.api.col.n3.ik;
import com.amap.api.col.n3.ip;
import com.amap.api.col.n3.ir;
import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviPath;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import androidx.fragment.app.Fragment;

public final class AMapCompat
{
    private AMapCompat()
    {
        throw new AssertionError();
    }

    private final static Field sInnerNavi;
    private final static Field sNaviImpl;
    private final static Field sNaviPath;
    private final static Field sNaviPathManager;
    private final static Field sAllNaviPath;

    static
    {
        try
        {
            if (!isCompat())
            {
                throw new Exception("no compat this version");
            }
            //get inner -> get ir
            sInnerNavi = AMapNavi.class.getDeclaredField("mINavi");
            //get impl -> get ik
            sNaviImpl = ir.class.getDeclaredField("m");
            //get path -> get NaviPath
            sNaviPath = ik.class.getDeclaredField("c");
            //get allPathManager -> get ip
            sNaviPathManager = ik.class.getDeclaredField("b");
            //get allPath -> get Map<int,NaviPath>
            sAllNaviPath = ip.class.getDeclaredField("h");

            sInnerNavi.setAccessible(true);
            sNaviImpl.setAccessible(true);
            sNaviPath.setAccessible(true);
            sNaviPathManager.setAccessible(true);
            sAllNaviPath.setAccessible(true);
        } catch (Exception e)
        {
            throw new IllegalStateException("must is navi version = 6.5.0 and map version = 6.6.0", e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isCompat()
    {
        //noinspection deprecation
        return "6.5.0".equals(AMapNavi.getVersion()) && "6.6.0".equals(AMap.getVersion());
    }

    public static DynamicMarker addMarker(DynamicMarkerOptions options)
    {
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static Map<Integer, NaviPath> getAllNaviPath(AMapNavi navi)
    {
        try
        {
            Object inner = sInnerNavi.get(navi);
            Object impl = sNaviImpl.get(inner);
            Object manager = sNaviPathManager.get(impl);
            //noinspection unchecked
            return (Map<Integer, NaviPath>) sAllNaviPath.get(manager);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void hideLogo(TextureSupportMapFragment mapFragment)
    {
        Fragment fragment = Objects.requireNonNull(Fragment.class.cast(mapFragment));
        ViewGroup mapLayout = Objects.requireNonNull((ViewGroup) fragment.getView());
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                mapLayout.removeView(mapLayout.getChildAt(2));
                mapLayout.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
            }
        });
    }

}

