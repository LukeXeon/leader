package org.kexie.android.dng.navi.model.beans;

import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;

import org.kexie.android.dng.common.contract.LBS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import java8.util.function.BiFunction;

/**
 * Created by Luke on 2018/12/27.
 */

public abstract class Point
        implements Parcelable,
        LBS.IPoint {

    private static final Map<Class<?>, BiFunction<Double, Double, Object>>
            POINT_FACTORIES
            = new ArrayMap<Class<?>, BiFunction<Double, Double, Object>>() {
        {
            put(NaviLatLng.class, NaviLatLng::new);
            put(LatLng.class, LatLng::new);
            put(LatLonPoint.class, LatLonPoint::new);
            put(JsonPoint.class, JsonPoint::new);
        }
    };

    public final static Point NO_LOCATION = form(Double.NaN, Double.NaN);

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj
                || (obj instanceof Point
                && ((Point) obj).getLatitude() == getLatitude()
                && ((Point) obj).getLongitude() == getLongitude());
    }

    public Point add(Point point) {
        return form(getLongitude() + point.getLongitude(), getLatitude() + point.getLatitude());
    }

    //Y
    public abstract double getLatitude();

    //x
    public abstract double getLongitude();

    @SuppressWarnings("unchecked")
    public <T> T unBox(Class<T> type) {
        return (T) Objects.requireNonNull(POINT_FACTORIES.get(type))
                .apply(getLatitude(), getLongitude());
    }

    public static Point box(NaviLatLng naviLatLng) {
        return new BoxNaviPoint(naviLatLng);
    }

    public static Point box(LatLonPoint latLonPoint) {
        return new BoxServicePoint(latLonPoint);
    }

    public static Point box(LatLng latLng) {
        return new BoxMapPoint(latLng);
    }

    public static Point form(double x, double y) {
        return new JsonPoint(x, y);
    }

    // 功能：判断点是否在多边形内
    // 方法：求解通过该点的水平线与多边形各边的交点
    // 结论：单边交点为奇数，成立!
    //参数：
    // POINT p   指定的某个点
    // LPPOINT ptPolygon 多边形的各个顶点坐标（首末点可以不一致）
    public static boolean isInPolygon(Point point, List<Point> APoints) {
        int nCross = 0;
        for (int i = 0; i < APoints.size(); i++) {
            Point p1 = APoints.get(i);
            Point p2 = APoints.get((i + 1) % APoints.size());
            // 求解 y=p.y 与 p1p2 的交点
            if (p1.getLongitude() == p2.getLongitude())      // p1p2 与 y=p0.y平行
                continue;
            if (point.getLongitude() < Math.min(p1.getLongitude(), p2.getLongitude()))   // 交点在p1p2延长线上
                continue;
            if (point.getLongitude() >= Math.max(p1.getLongitude(), p2.getLongitude()))   // 交点在p1p2延长线上
                continue;
            // 求交点的 SimpleApplyAdapter 坐标 --------------------------------------------------------------
            double x = (point.getLongitude() - p1.getLongitude())
                    * (p2.getLatitude() - p1.getLatitude())
                    / (p2.getLongitude() - p1.getLongitude())
                    + p1.getLatitude();
            if (x > point.getLatitude())
                nCross++; // 只统计单边交点
        }
        // 单边交点为偶数，点在多边形之外 ---
        return (nCross % 2 == 1);
    }

    public static List<Point> getCircleBy(Point point, double radius) {
        List<Point> latLonPoints = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            double arc = Math.PI * i * 30.0 / 180.0;
            double addx = radius * Math.sin(arc);
            double addy = radius * Math.cos(arc);
            double x = point.getLatitude() + addy;
            double y = point.getLongitude() + addx;
            latLonPoints.add(form(x, y));
        }
        return latLonPoints;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("longitude(x)=%s,latitude(y)=%s", getLongitude(), getLatitude());
    }
}
