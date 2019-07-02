package org.kexie.android.dng.common.util;

public interface Callback<T> {
    void handle(T value);
}