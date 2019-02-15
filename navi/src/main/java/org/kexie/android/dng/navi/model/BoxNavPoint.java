package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.navi.model.NaviLatLng;

public class BoxNavPoint
        extends Point
        implements Parcelable
{
    private final NaviLatLng naviLatLng;

    BoxNavPoint(NaviLatLng naviLatLng)
    {
        this.naviLatLng = naviLatLng;
    }

    protected BoxNavPoint(Parcel in)
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

    public static final Creator<BoxNavPoint> CREATOR = new Creator<BoxNavPoint>()
    {
        @Override
        public BoxNavPoint createFromParcel(Parcel in)
        {
            return new BoxNavPoint(in);
        }

        @Override
        public BoxNavPoint[] newArray(int size)
        {
            return new BoxNavPoint[size];
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
