package org.kexie.android.dng.media.viewmodel.beans;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Resource implements Parcelable {
    public final String title;
    public final String uri;
    public final int type;

    public Resource(String title, String uri, int type) {
        this.title = title;
        this.uri = uri;
        this.type = type;
    }

    private Resource(Parcel in) {
        title = in.readString();
        uri = in.readString();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(uri);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Resource> CREATOR = new Creator<Resource>() {
        @Override
        public Resource createFromParcel(Parcel in) {
            return new Resource(in);
        }

        @Override
        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return uri;
    }
}
