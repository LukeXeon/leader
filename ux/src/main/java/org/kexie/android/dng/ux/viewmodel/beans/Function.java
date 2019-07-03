package org.kexie.android.dng.ux.viewmodel.beans;

import android.graphics.drawable.Drawable;

public class Function
{
    public final Drawable icon;
    public final String name;
    public final String uri;

    public Function(String name,
                    Drawable icon,
                    String uri)
    {
        this.name = name;
        this.icon = icon;
        this.uri = uri;
    }
}
