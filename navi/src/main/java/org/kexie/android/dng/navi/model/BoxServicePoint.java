package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.services.core.LatLonPoint;

public class BoxServicePoint
        extends Point
        implements Parcelable
{
    private final LatLonPoint latLonPoint;

    BoxServicePoint(LatLonPoint latLonPoint)
    {
        this.latLonPoint = latLonPoint;
    }

    protected BoxServicePoint(Parcel in)
    {
        latLonPoint = in.readParcelable(LatLonPoint.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(latLonPoint, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<BoxServicePoint> CREATOR = new Creator<BoxServicePoint>()
    {
        @Override
        public BoxServicePoint createFromParcel(Parcel in)
        {
            return new BoxServicePoint(in);
        }

        @Override
        public BoxServicePoint[] newArray(int size)
        {
            return new BoxServicePoint[size];
        }
    };

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
