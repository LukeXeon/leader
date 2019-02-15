package org.kexie.android.dng.navi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;

import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class BoxRoute
        extends Route
        implements Parcelable
{
    private Point from;
    private Point to;
    private DrivePath path;

    public BoxRoute(Point from, Point to, DrivePath path)
    {
        this.from = from;
        this.to = to;
        this.path = path;
    }

    protected BoxRoute(Parcel in)
    {
        from = in.readParcelable(Point.class.getClassLoader());
        to = in.readParcelable(Point.class.getClassLoader());
        path = in.readParcelable(DrivePath.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(from, flags);
        dest.writeParcelable(to, flags);
        dest.writeParcelable(path, flags);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<BoxRoute> CREATOR = new Creator<BoxRoute>()
    {
        @Override
        public BoxRoute createFromParcel(Parcel in)
        {
            return new BoxRoute(in);
        }

        @Override
        public BoxRoute[] newArray(int size)
        {
            return new BoxRoute[size];
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
        return StreamSupport.stream(path.getSteps())
                .map(DriveStep::getPolyline)
                .map(l -> l.get(0))
                .map(Point::box)
                .collect(Collectors.toList());
    }
}
