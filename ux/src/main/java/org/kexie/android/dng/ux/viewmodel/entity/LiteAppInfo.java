package org.kexie.android.dng.ux.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class LiteAppInfo
{
    public final String name;
    public final Drawable icon;

    public LiteAppInfo(String name,
                       Drawable icon)
    {
        this.name = name;
        this.icon = icon;
    }
}
