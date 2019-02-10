
package org.kexie.android.dng.media.viewmodel.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaInfo implements Parcelable
{
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_VIDEO = 2;

    private int type;
    private String title;
    private String path;

    public MediaInfo(String title, String path, int type)
    {
        this.title = title;
        this.path = path;
        this.type = type;
    }

    protected MediaInfo(Parcel in)
    {
        title = in.readString();
        path = in.readString();
        type = in.readInt();
    }

    public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>()
    {
        @Override
        public MediaInfo createFromParcel(Parcel in)
        {
            return new MediaInfo(in);
        }

        @Override
        public MediaInfo[] newArray(int size)
        {
            return new MediaInfo[size];
        }
    };

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String content)
    {
        this.path = content;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(title);
        dest.writeString(path);
        dest.writeInt(type);
    }
}
