package kexie.android.common.adapter;

import android.support.annotation.Nullable;

import java.util.List;

public interface BindingViewAdapter<T>
{
    void setNewData(@Nullable List<T> data);

    List<T> getData();
}
