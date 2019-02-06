package kexie.android.navi.util;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kexie.android.navi.entity.Point;

/**
 * Created by Luke on 2018/12/27.
 */

public final class Points
{
    private Points()
    {

    }

    public static Gson getJsonConverter()
    {
        return new GsonBuilder()
                .registerTypeAdapter(Point.class, new JsonDeserializer<Point>()
                {
                    @Override
                    public Point deserialize(JsonElement json, Type typeOfT
                            , JsonDeserializationContext context) throws JsonParseException
                    {
                        JsonObject object = (JsonObject) json;
                        return new Point(object.get("latitude").getAsDouble(),
                                object.get("longitude").getAsDouble());
                    }
                }).registerTypeAdapter(Point.class, new JsonSerializer<Point>()
                {
                    @Override
                    public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context)
                    {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("latitude", src.getLatitude());
                        jsonObject.addProperty("longitude", src.getLongitude());
                        return jsonObject;
                    }
                }).create();
    }

    public static List<LatLng> toLatLngs(List<Point> points)
    {
        if (points == null)
        {
            return null;
        }
        List<LatLng> latLngs = new ArrayList<>(points.size());
        for (Point point : points)
        {
            latLngs.add(point.toLatLng());
        }
        return latLngs;
    }

    public static List<LatLonPoint> toLatLatLonPoints(List<Point> points)
    {
        if (points == null)
        {
            return null;
        }
        List<LatLonPoint> latLngs = new ArrayList<>(points.size());
        for (Point point : points)
        {
            latLngs.add(point.toLatLonPoint());
        }
        return latLngs;
    }

    public static List<NaviLatLng> toNaviLatLngs(List<Point> points)
    {
        if (points == null)
        {
            return null;
        }
        List<NaviLatLng> latLngs = new ArrayList<>(points.size());
        for (Point point : points)
        {
            latLngs.add(point.toNaviLatLng());
        }
        return latLngs;
    }

    public static List<Point> getCircleBy(Point point, double radius)
    {
        List<Point> latLonPoints = new ArrayList<>();
        for (int i = 0; i < 12; i++)
        {
            double arc = Math.PI * i * 30.0 / 180.0;
            double addx = radius * Math.sin(arc);
            double addy = radius * Math.cos(arc);
            double x = point.getLatitude() + addy;
            double y = point.getLongitude() + addx;
            latLonPoints.add(new Point(x, y));
        }
        return latLonPoints;
    }

    // 功能：判断点是否在多边形内
    // 方法：求解通过该点的水平线与多边形各边的交点
    // 结论：单边交点为奇数，成立!
    //参数：
    // POINT p   指定的某个点
    // LPPOINT ptPolygon 多边形的各个顶点坐标（首末点可以不一致）
    public static boolean isInPolygon(Point point, List<Point> APoints)
    {
        int nCross = 0;
        for (int i = 0; i < APoints.size(); i++)
        {
            Point p1 = APoints.get(i);
            Point p2 = APoints.get((i + 1) % APoints.size());
            // 求解 y=p.y 与 p1p2 的交点
            if (p1.getLongitude() == p2.getLongitude())      // p1p2 与 y=p0.y平行
                continue;
            if (point.getLongitude() < Math.min(p1.getLongitude(), p2.getLongitude()))   // 交点在p1p2延长线上
                continue;
            if (point.getLongitude() >= Math.max(p1.getLongitude(), p2.getLongitude()))   // 交点在p1p2延长线上
                continue;
            // 求交点的 X 坐标 --------------------------------------------------------------
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
}
