package kexie.android.navi.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Luke on 2018/12/27.
 */

public final class Point
        implements PointCompat,
        Parcelable
{
    public static final Creator<Point> CREATOR = new Creator<Point>()
    {
        @Override
        public Point createFromParcel(Parcel in)
        {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size)
        {
            return new Point[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeDouble(getLatitude());
        dest.writeDouble(getLongitude());
    }

    private static class Json implements PointCompat
    {
        public final double latitude;
        public final double longitude;

        private Json(double latitude, double longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

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

        @Override
        public LatLng toLatLng()
        {
            return new LatLng(latitude, longitude);
        }

        @Override
        public LatLonPoint toLatLonPoint()
        {
            return new LatLonPoint(latitude, longitude);
        }

        @Override
        public NaviLatLng toNaviLatLng()
        {
            return new NaviLatLng(latitude, longitude);
        }
    }

    private static class LatLngImpl implements PointCompat
    {
        private final LatLng latLng;

        private LatLngImpl(LatLng latLng)
        {
            this.latLng = latLng;
        }

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

        @Override
        public LatLng toLatLng()
        {
            return latLng;
        }

        @Override
        public LatLonPoint toLatLonPoint()
        {
            return new LatLonPoint(latLng.latitude, latLng.longitude);
        }

        @Override
        public NaviLatLng toNaviLatLng()
        {
            return new NaviLatLng(latLng.latitude, latLng.longitude);
        }
    }

    private static class LatLonPointImpl implements PointCompat
    {
        private final LatLonPoint latLonPoint;

        private LatLonPointImpl(LatLonPoint latLonPoint)
        {
            this.latLonPoint = latLonPoint;
        }

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

        @Override
        public LatLng toLatLng()
        {
            return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
        }

        @Override
        public LatLonPoint toLatLonPoint()
        {
            return latLonPoint;
        }

        @Override
        public NaviLatLng toNaviLatLng()
        {
            return new NaviLatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
        }
    }

    private static class NaviLatLngImpl implements PointCompat
    {
        private final NaviLatLng naviLatLng;

        private NaviLatLngImpl(NaviLatLng naviLatLng)
        {
            this.naviLatLng = naviLatLng;
        }


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

        @Override
        public LatLng toLatLng()
        {
            return new LatLng(naviLatLng.getLatitude(), naviLatLng.getLongitude());
        }

        @Override
        public LatLonPoint toLatLonPoint()
        {
            return new LatLonPoint(naviLatLng.getLatitude(), naviLatLng.getLongitude());
        }

        @Override
        public NaviLatLng toNaviLatLng()
        {
            return naviLatLng;
        }
    }

    private final PointCompat impl;

    protected Point(Parcel in)
    {
        impl = new Json(in.readDouble(), in.readDouble());
    }

    public Point(Json json)
    {
        this(json.latitude, json.longitude);
    }

    public Point(double latitude, double longitude)
    {
        impl = new Json(latitude, longitude);
    }

    public Point(LatLng latLng)
    {
        impl = new LatLngImpl(latLng);
    }

    public Point(LatLonPoint latLonPoint)
    {
        impl = new LatLonPointImpl(latLonPoint);
    }

    public Point(NaviLatLng naviLatLng)
    {
        impl = new NaviLatLngImpl(naviLatLng);
    }

    @Override
    public double getLatitude()
    {
        return impl.getLatitude();
    }

    @Override
    public double getLongitude()
    {
        return impl.getLongitude();
    }

    @Override
    public LatLng toLatLng()
    {
        return impl.toLatLng();
    }

    @Override
    public LatLonPoint toLatLonPoint()
    {
        return impl.toLatLonPoint();
    }

    @Override
    public NaviLatLng toNaviLatLng()
    {
        return impl.toNaviLatLng();
    }

    public final static Point NO_LOCATION = new Point(Double.NaN, Double.NaN);

}