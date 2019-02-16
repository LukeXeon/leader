package org.kexie.android.mapper;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class FragmentIntent implements Parcelable
{
    final Uri uri;
    final Bundle arguments;

    private FragmentIntent(Parcel in)
    {
        uri = in.readParcelable(Uri.class.getClassLoader());
        arguments = in.readBundle(getClass().getClassLoader());
    }

    private FragmentIntent(Builder builder)
    {
        uri = builder.uri;
        arguments = builder.arguments;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(uri, flags);
        dest.writeBundle(arguments);;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<FragmentIntent> CREATOR = new Creator<FragmentIntent>()
    {
        @Override
        public FragmentIntent createFromParcel(Parcel in)
        {
            return new FragmentIntent(in);
        }

        @Override
        public FragmentIntent[] newArray(int size)
        {
            return new FragmentIntent[size];
        }
    };

    public static final class Builder
    {
        private Uri uri;
        private Bundle arguments;

        public Builder()
        {
        }

        public Builder uri(Uri val)
        {
            uri = val;
            return this;
        }

        public Builder arguments(Bundle val)
        {
            arguments = val;
            return this;
        }

        public FragmentIntent build()
        {
            return new FragmentIntent(this);
        }
    }
}
