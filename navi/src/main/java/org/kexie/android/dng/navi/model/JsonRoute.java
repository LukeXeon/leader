package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class JsonRoute
        extends Route
        implements Parcelable
{
    private JsonPoint from;
    private JsonPoint to;
    private List<JsonPoint> ways;

    protected JsonRoute(Parcel in)
    {
        from = in.readParcelable(JsonPoint.class.getClassLoader());
        to = in.readParcelable(JsonPoint.class.getClassLoader());
        ways = in.createTypedArrayList(JsonPoint.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(from, flags);
        dest.writeParcelable(to, flags);
        dest.writeTypedList(ways);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<JsonRoute> CREATOR = new Creator<JsonRoute>()
    {
        @Override
        public JsonRoute createFromParcel(Parcel in)
        {
            return new JsonRoute(in);
        }

        @Override
        public JsonRoute[] newArray(int size)
        {
            return new JsonRoute[size];
        }
    };

    @Override
    public Point getFrom()
    {
        return from;
    }

    @Override
    public Point getTo()
    {
        return to;
    }

    @Override
    public List<? extends Point> getWays()
    {
        return ways;
    }
}
