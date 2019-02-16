package org.kexie.android.mapper;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface Entry
{
    String getKey();

    Class<?> getValue();
}
