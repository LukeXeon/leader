
package org.kexie.android.dng.media.model.beans;

public abstract class MediaInfo
{
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_MUSIC = 3;
    public int type;
    public String title;
    public String uri;
    public MediaInfo(String title, String uri, int type)
    {
        this.title = title;
        this.uri = uri;
        this.type = type;
    }
}
