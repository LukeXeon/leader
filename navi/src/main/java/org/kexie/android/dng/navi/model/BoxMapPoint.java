package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;

public class BoxMapPoint
        extends Point
        implements Parcelable
{
    private final LatLng latLng;

    BoxMapPoint(LatLng latLng)
    {
        this.latLng = latLng;
    }

    private BoxMapPoint(Parcel in)
    {
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(latLng, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<BoxMapPoint> CREATOR = new Creator<BoxMapPoint>()
    {
        @Override
        public BoxMapPoint createFromParcel(Parcel in)
        {
            return new BoxMapPoint(in);
        }

        @Override
        public BoxMapPoint[] newArray(int size)
        {
            return new BoxMapPoint[size];
        }
    };

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
