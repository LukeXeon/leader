
package org.kexie.android.dng.media.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaInfo implements Parcelable
{
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;

    public final int type;
    public final String title;
    public final String uri;

    public MediaInfo(String title, String uri, int type)
    {
        this.title = title;
        this.uri = uri;
        this.type = type;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

    }
}
