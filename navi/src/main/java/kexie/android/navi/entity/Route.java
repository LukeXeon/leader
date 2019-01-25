package kexie.android.navi.entity;

import android.os.Parcelable;

import java.util.List;

public interface Route extends Parcelable
{
    Point getFrom();

    Point getTo();

    List<Point> getPoints();
}
