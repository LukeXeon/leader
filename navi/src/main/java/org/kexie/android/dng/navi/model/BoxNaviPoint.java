package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.navi.model.NaviLatLng;

public class BoxNaviPoint
        extends Point
        implements Parcelable
{
    private final NaviLatLng naviLatLng;

    BoxNaviPoint(NaviLatLng naviLatLng)
    {
        this.naviLatLng = naviLatLng;
    }

    protected BoxNaviPoint(Parcel in)
    {
        naviLatLng = in.readParcelable(NaviLatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(naviLatLng, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<BoxNaviPoint> CREATOR = new Creator<BoxNaviPoint>()
    {
        @Override
        public BoxNaviPoint createFromParcel(Parcel in)
        {
            return new BoxNaviPoint(in);
        }

        @Override
        public BoxNaviPoint[] newArray(int size)
        {
            return new BoxNaviPoint[size];
        }
    };

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
