package org.kexie.android.dng.navi.model;

import com.amap.api.services.core.LatLonPoint;

public class BoxServicePoint extends Point
{
    private final LatLonPoint latLonPoint;

    BoxServicePoint(LatLonPoint latLonPoint)
    {
        this.latLonPoint = latLonPoint;
    }

    @Override
    public double getLatitude()
    {
        return latLonPoint.getLatitude();
    }

    @Override
    public double getLongitude()
    {
        return latLonPoint.getLongitude();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unBox(Class<T> type)
    {
        if (LatLonPoint.class.equals(type))
        {
            return (T) latLonPoint;
        }
        return super.unBox(type);
    }
}
