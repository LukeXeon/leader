package org.kexie.android.dng.navi.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class LiteStep
{
    public final String action;
    public final Drawable icon;

    public LiteStep(String action, Drawable icon)
    {
        this.action = action;
        this.icon = icon;
    }
}
