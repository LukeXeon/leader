package org.kexie.android.dng.ux.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class App
{
    public final String name;
    public final Drawable icon;
    public final String packageName;

    public App(String name,
               Drawable icon, String packageName)
    {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
    }
}
