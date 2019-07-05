package org.kexie.android.dng.media.model.beans;

import android.graphics.drawable.Drawable;

public class Music {
    public final Drawable drawable;
    public final String singer;
    public final String title;
    public final String path;

    public Music(Drawable drawable,
                 String title,
                 String singer,
                 String path) {
        this.drawable = drawable;
        this.singer = singer;
        this.title = title;
        this.path = path;
    }
}
