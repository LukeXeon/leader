package org.kexie.android.dng.navi.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.kexie.android.dng.navi.model.JsonPoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NetRoute implements Route,Parcelable
{
    @SerializedName("from")
    private JsonPoint from;
    @SerializedName("to")
    private JsonPoint to;
    @SerializedName("points")
    private List<JsonPoint> points;

    public NetRoute()
    {
        points = Collections.emptyList();
    }

    protected NetRoute(Parcel in)
    {
        from = in.readParcelable(JsonPoint.class.getClassLoader());
        to = in.readParcelable(JsonPoint.class.getClassLoader());
        points = in.createTypedArrayList(JsonPoint.CREATOR);
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

    public void setFrom(JsonPoint from)
    {
        this.from = from;
    }

    public void setTo(JsonPoint to)
    {
        this.to = to;
    }

    public void setPoints(List<JsonPoint> points)
    {
        this.points = Objects.requireNonNull(points);
    }

    @Override
    public JsonPoint getFrom()
    {
        return from;
    }

    @Override
    public JsonPoint getTo()
    {
        return to;
    }

    @Override
    public List<JsonPoint> getPoints()
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
