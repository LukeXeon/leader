package org.kexie.android.dng.navi.model;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public abstract class Route implements Parcelable
{
    public abstract Point getFrom();

    public abstract Point getTo();

    public abstract List<? extends Point> getWays();

    public static List<Point> getAllPoint(Route route)
    {
        List<Point> points = new ArrayList<>();
        Point from = route.getFrom();
        Point to = route.getTo();
        if (from != null)
        {
            points.add(route.getFrom());
        }
        points.addAll(route.getWays());
        if (to != null)
        {
            points.add(route.getTo());
        }
        return points;
    }
}
