
package kexie.android.media.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaInfo implements Parcelable
{

    private String title;
    private String path;

    public MediaInfo(String title, String path)
    {
        this.title = title;
        this.path = path;
    }

    protected MediaInfo(Parcel in)
    {
        title = in.readString();
        path = in.readString();
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
    }
}
