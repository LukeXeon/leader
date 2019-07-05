package org.kexie.android.dng.media.model.beans;

import android.os.Parcel;
import android.os.Parcelable;

import org.kexie.android.dng.media.model.MimeType;

import java.util.List;

public class Graph implements Parcelable {
    //        String title;
    //        long dateAdded; // 时间戳
    //        long dateModified; // 时间戳
    //        String description;
    //        String picasaId;
    //        String isPrivate;
    //        String latitude;
    //        String longitude;
    //        String dateTaken;
    //        int orientation;
    //        String miniThumbMagic;
    //        String bucketId;
    //        long size;
    //        int width;
    //        int height;
    public final String id;
    public final String data; //file path
    public final String displayName; // xxx.jpg
    public final String mineType; // image/jpeg
    public final String bucketDisplayName;

    public Graph(String id,
                 String data,
                 String displayName,
                 String mineType,
                 String bucketDisplayName) {
        this.id = id;
        this.data = data;
        this.displayName = displayName;
        this.mineType = mineType;
        this.bucketDisplayName = bucketDisplayName;
    }

    protected Graph(Parcel in) {
        id = in.readString();
        data = in.readString();
        displayName = in.readString();
        mineType = in.readString();
        bucketDisplayName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(data);
        dest.writeString(displayName);
        dest.writeString(mineType);
        dest.writeString(bucketDisplayName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Graph> CREATOR = new Creator<Graph>() {
        @Override
        public Graph createFromParcel(Parcel in) {
            return new Graph(in);
        }

        @Override
        public Graph[] newArray(int size) {
            return new Graph[size];
        }
    };

    public boolean isVideo() {
        return mineType != null && mineType.contains("video");
    }

    public boolean isGif() {
        return mineType != null && mineType.equals(MimeType.GIF);
    }

    public static class Album {
        public final String name;
        //封面
        public final List<Graph> resources;

        public Album(String name,
                     List<Graph> resources) {
            this.name = name;
            this.resources = resources;
        }


    }
}