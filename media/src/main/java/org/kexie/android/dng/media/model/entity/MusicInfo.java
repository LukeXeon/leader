package org.kexie.android.dng.media.model.entity;

import android.graphics.drawable.Drawable;

public class MusicInfo extends MediaInfo {
    public Drawable drawable;
    public String singer;
    public MusicInfo(String title,
                     String uri,
                     String singer) {
        super(title, uri, TYPE_MUSIC);
        this.singer = singer;
    }
}
