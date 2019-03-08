package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Luke on 2018/12/27.
 */

public class Query implements Parcelable
{
    public final Point to;
    public final List<? extends Point> ways;
    public final int mode;

    private Query(Builder builder)
    {
        to = builder.to;
        ways = builder.ways;
        mode = builder.mode;
    }

    public static final class Builder
    {
        private Point to;
        private List<Point> ways;
        private int mode;

        public Builder()
        {
        }

        public Builder to(Point val)
        {
            to = val;
            return this;
        }

        public Builder ways(List<Point> val)
        {
            ways = val;
            return this;
        }


        public Builder mode(int val)
        {
            mode = val;
            return this;
        }

        public Query build()
        {
            return new Query(this);
        }
    }

    private Query(Parcel in)
    {
        to = in.readParcelable(Point.class.getClassLoader());
        ways = in.createTypedArrayList(JsonPoint.CREATOR);
        mode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(to, flags);
        dest.writeTypedList(ways);
        dest.writeInt(mode);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<Query> CREATOR = new Creator<Query>()
    {
        @Override
        public Query createFromParcel(Parcel in)
        {
            return new Query(in);
        }

        @Override
        public Query[] newArray(int size)
        {
            return new Query[size];
        }
    };
}
