package org.kexie.android.dng.navi.widget;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class X extends FragmentStateAdapter
{
    public X(@NonNull FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        return null;
    }

    @Override
    public int getItemCount()
    {
        return 0;
    }
}
