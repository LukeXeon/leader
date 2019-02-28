package org.kexie.android.dng.media.viewmodel.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable
{
    public final String title;
    public final String uri;
    public final int type;

    public Media(String title, String uri, int type)
    {
        this.title = title;
        this.uri = uri;
        this.type = type;
    }

    protected Media(Parcel in)
    {
        title = in.readString();
        uri = in.readString();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(title);
        dest.writeString(uri);
        dest.writeInt(type);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>()
    {
        @Override
        public Media createFromParcel(Parcel in)
        {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size)
        {
            return new Media[size];
        }
    };
}
