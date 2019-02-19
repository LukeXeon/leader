package org.kexie.android.common.databinding;

import androidx.databinding.ObservableList;

public abstract class OnListChangedCallback<T extends ObservableList>
        extends ObservableList.OnListChangedCallback<T>
{

    @Override
    public void onChanged(T sender)
    {

    }

    @Override
    public void onItemRangeChanged(T sender, int positionStart, int itemCount)
    {

    }

    @Override
    public void onItemRangeInserted(T sender, int positionStart, int itemCount)
    {

    }

    @Override
    public void onItemRangeMoved(T sender, int fromPosition, int toPosition, int itemCount)
    {

    }

    @Override
    public void onItemRangeRemoved(T sender, int positionStart, int itemCount)
    {

    }
}
