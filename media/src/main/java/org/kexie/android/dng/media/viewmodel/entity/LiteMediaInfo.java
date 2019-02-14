package org.kexie.android.dng.media.viewmodel.entity;

import android.os.Parcel;
import android.os.Parcelable;

public final class LiteMediaInfo implements Parcelable
{
    public final String title;
    public final String uri;
    public final int type;

    public LiteMediaInfo(String title, String uri, int type)
    {
        this.title = title;
        this.uri = uri;
        this.type = type;
    }

    protected LiteMediaInfo(Parcel in)
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

    public static final Creator<LiteMediaInfo> CREATOR = new Creator<LiteMediaInfo>()
    {
        @Override
        public LiteMediaInfo createFromParcel(Parcel in)
        {
            return new LiteMediaInfo(in);
        }

        @Override
        public LiteMediaInfo[] newArray(int size)
        {
            return new LiteMediaInfo[size];
        }
    };
}
