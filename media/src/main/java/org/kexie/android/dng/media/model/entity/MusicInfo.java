package org.kexie.android.dng.media.model.entity;

import android.graphics.drawable.Drawable;

public class MusicInfo extends MediaInfo {
    public final Drawable drawable;
    public final String singer;
    public MusicInfo(String title,
                     String uri,
                     Drawable drawable,
                     String singer) {
        super(title, uri, TYPE_MUSIC);
        this.drawable = drawable;
        this.singer = singer;
    }
}
