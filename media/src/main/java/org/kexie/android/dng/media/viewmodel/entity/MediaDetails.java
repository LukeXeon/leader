package org.kexie.android.dng.media.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class MediaDetails {
    public final Drawable icon;
    public final String songName;
    public final String singerName;
    public MediaDetails(Drawable icon, String songName, String singerName) {
        this.icon = icon;
        this.songName = songName;
        this.singerName = singerName;
    }
}
