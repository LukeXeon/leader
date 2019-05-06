package org.kexie.android.dng.media.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class MusicDetails {
    public final String path;
    public final Drawable icon;
    public final String songName;
    public final String singerName;
    public MusicDetails(String path,
                        Drawable icon,
                        String songName,
                        String singerName) {
        this.path = path;
        this.icon = icon;
        this.songName = songName;
        this.singerName = singerName;
    }
}
