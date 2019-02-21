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
        return new ArrayList<Point>()
        {
            {
                add(route.getFrom());
                addAll(route.getWays());
                add(route.getTo());
            }
        };
    }
}
