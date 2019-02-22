package org.kexie.android.dng.navi.model;

import com.amap.api.col.n3.ik;
import com.amap.api.col.n3.ip;
import com.amap.api.col.n3.ir;
import com.amap.api.navi.AMapNavi;

import java.lang.reflect.Field;

public final class NaviCompat
{
    private final static Field sInnerNavi;
    private final static Field sNaviImpl;
    private final static Field sNaviPath;
    private final static Field sNaviPathManager;
    private final static Field sAllNaviPath;

    static
    {
        try
        {
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
            throw new RuntimeException("compat load failed", e);
        }
    }

    private NaviCompat()
    {
        throw new AssertionError();
    }

}
