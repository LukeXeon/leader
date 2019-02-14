
package org.kexie.android.dng.media.model.entity;

public class MediaInfo
{
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;

    public final int type;
    public final String title;
    public final String path;

    public MediaInfo(String title, String path, int type)
    {
        this.title = title;
        this.path = path;
        this.type = type;
    }

}
