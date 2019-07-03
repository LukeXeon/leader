package org.kexie.android.dng.ux.viewmodel.beans;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class App implements Comparable<App> {
    public final String name;
    public final Drawable icon;
    public final String packageName;
    public final boolean isSystem;

    public App(String name,
               Drawable icon,
               String packageName,
               boolean isSystem) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.isSystem = isSystem;
    }

    @Override
    public int compareTo(@NonNull App o) {
        return packageName.compareTo(o.packageName);
    }
}
