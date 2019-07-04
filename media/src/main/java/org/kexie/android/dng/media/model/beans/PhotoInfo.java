package org.kexie.android.dng.media.model.beans;

import java.util.Date;

public class PhotoInfo extends MediaInfo {
    public final Date date;
    public PhotoInfo(String title, String uri, Date date) {
        super(title, uri, TYPE_PHOTO);
        this.date = date;
    }
}
