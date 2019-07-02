package org.kexie.android.dng.navi.viewmodel.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class TipText implements Parcelable {
    public final String id;
    public final String text;

    public TipText(String id, String text) {
        this.id = id;
        this.text = text;
    }

    private TipText(Parcel in) {
        id = in.readString();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TipText> CREATOR = new Creator<TipText>() {
        @Override
        public TipText createFromParcel(Parcel in) {
            return new TipText(in);
        }

        @Override
        public TipText[] newArray(int size) {
            return new TipText[size];
        }
    };
}
