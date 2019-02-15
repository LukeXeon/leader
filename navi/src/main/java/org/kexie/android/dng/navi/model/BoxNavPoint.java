package org.kexie.android.dng.navi.model;

import com.amap.api.navi.model.NaviLatLng;

public class BoxNavPoint extends Point
{
    private final NaviLatLng naviLatLng;

    BoxNavPoint(NaviLatLng naviLatLng)
    {
        this.naviLatLng = naviLatLng;
    }

    @Override
    public double getLatitude()
    {
        return naviLatLng.getLatitude();
    }

    @Override
    public double getLongitude()
    {
        return naviLatLng.getLongitude();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unBox(Class<T> type)
    {
        if (NaviLatLng.class.equals(type))
        {
            return (T)naviLatLng;
        }
        return super.unBox(type);
    }
}
