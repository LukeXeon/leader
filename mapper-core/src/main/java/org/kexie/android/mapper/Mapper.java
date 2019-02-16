package org.kexie.android.mapper;

import android.net.Uri;
import android.os.Bundle;

import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public final class Mapper
{
    private Mapper()
    {
        throw new AssertionError();
    }

    private static final String MAPPER_REQUEST_CODE_KEY
            = UUID.randomUUID().toString();

    public static int getRequestCode(Fragment fragment)
    {
        return Objects.requireNonNull(fragment.getArguments())
                .getInt(MAPPER_REQUEST_CODE_KEY);
    }

    public static Fragment mapping(FragmentIntent intent)
    {
        return mapping(intent, 0);
    }

    @NonNull
    public static Fragment mapping(FragmentIntent intent, int requestCode)
    {
        long hashCode = ((long) intent.uri.hashCode()) & 0xffffffffL;
        while (true)
        {
            try
            {
                Class entryType = Class.forName(
                        "org.kexie.android.mapper.internal.Entry_"
                                + hashCode);
                Entry entry = (Entry) entryType.newInstance();
                if (intent.uri.equals(Uri.parse(entry.getKey())))
                {
                    Fragment fragment = (Fragment) entry.getValue()
                            .newInstance();
                    Bundle bundle = intent.arguments == null
                            ? new Bundle()
                            : intent.arguments;
                    bundle.putInt(MAPPER_REQUEST_CODE_KEY, requestCode);
                    fragment.setArguments(bundle);
                    return fragment;
                }
                hashCode++;
            } catch (ClassNotFoundException e)
            {
                throw new IllegalArgumentException(String.format("%s no found", intent.uri));
            } catch (IllegalAccessException | InstantiationException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
