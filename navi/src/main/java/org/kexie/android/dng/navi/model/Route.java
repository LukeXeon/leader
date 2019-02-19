package org.kexie.android.dng.navi.model;

import android.os.Parcelable;

import java.util.List;

public abstract class Route implements Parcelable
{
    public abstract Point getFrom();

    public abstract Point getTo();

    public abstract List<? extends Point> getWays();

}