
package org.kexie.android.dng.media.model.entity;

public class MediaInfo
{
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
