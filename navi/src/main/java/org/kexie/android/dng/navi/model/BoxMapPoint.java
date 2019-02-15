package org.kexie.android.dng.navi.model;

import com.amap.api.maps.model.LatLng;

public class BoxMapPoint extends Point
{
    private final LatLng latLng;

    BoxMapPoint(LatLng latLng)
    {
        this.latLng = latLng;
    }

    @Override
    public double getLatitude()
    {
        return latLng.latitude;
    }

    @Override
    public double getLongitude()
    {
        return latLng.longitude;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unBox(Class<T> type)
    {
        if (LatLng.class.equals(type))
        {
            return (T)latLng;
        }
        return super.unBox(type);
    }
}
