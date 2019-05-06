
package org.kexie.android.dng.media.model.entity;

public abstract class MediaInfo
{
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_MUSIC = 3;
    public final int type;
    public final String title;
    public final String uri;
    public MediaInfo(String title, String uri, int type)
    {
        this.title = title;
        this.uri = uri;
        this.type = type;
    }
}
