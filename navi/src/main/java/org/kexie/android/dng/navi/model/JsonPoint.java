package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Luke on 2018/12/27.
 */

public class JsonPoint
        extends Point
        implements Parcelable
{
    private double latitude;
    private double longitude;

    private JsonPoint()
    {

    }

    JsonPoint(double longitude, double latitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected JsonPoint(Parcel in)
    {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<JsonPoint> CREATOR = new Creator<JsonPoint>()
    {
        @Override
        public JsonPoint createFromParcel(Parcel in)
        {
            return new JsonPoint(in);
        }

        @Override
        public JsonPoint[] newArray(int size)
        {
            return new JsonPoint[size];
        }
    };


    @Override
    public double getLatitude()
    {
        return latitude;
    }

    @Override
    public double getLongitude()
    {
        return longitude;
    }
}