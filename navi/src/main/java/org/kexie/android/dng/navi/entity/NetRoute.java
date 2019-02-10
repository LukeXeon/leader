package org.kexie.android.dng.navi.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NetRoute implements Route,Parcelable
{
    @SerializedName("from")
    private Point from;
    @SerializedName("to")
    private Point to;
    @SerializedName("points")
    private List<Point> points;

    public NetRoute()
    {
        points = Collections.emptyList();
    }

    protected NetRoute(Parcel in)
    {
        from = in.readParcelable(Point.class.getClassLoader());
        to = in.readParcelable(Point.class.getClassLoader());
        points = in.createTypedArrayList(Point.CREATOR);
    }

    public static final Creator<NetRoute> CREATOR = new Creator<NetRoute>()
    {
        @Override
        public NetRoute createFromParcel(Parcel in)
        {
            return new NetRoute(in);
        }

        @Override
        public NetRoute[] newArray(int size)
        {
            return new NetRoute[size];
        }
    };

    public void setFrom(Point from)
    {
        this.from = from;
    }

    public void setTo(Point to)
    {
        this.to = to;
    }

    public void setPoints(List<Point> points)
    {
        this.points = Objects.requireNonNull(points);
    }

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
    public List<Point> getPoints()
    {
        return Collections.unmodifiableList(points);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(from, flags);
        dest.writeParcelable(to, flags);
        dest.writeTypedList(points);
    }
}
