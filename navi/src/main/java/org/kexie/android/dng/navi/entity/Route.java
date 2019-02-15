package org.kexie.android.dng.navi.entity;

import android.os.Parcelable;

import org.kexie.android.dng.navi.model.JsonPoint;

import java.util.List;

public interface Route extends Parcelable
{
    JsonPoint getFrom();

    JsonPoint getTo();

    List<JsonPoint> getPoints();
}
