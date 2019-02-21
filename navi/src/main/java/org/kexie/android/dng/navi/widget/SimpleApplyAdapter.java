package org.kexie.android.dng.navi.widget;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SimpleApplyAdapter
        extends FragmentPagerAdapter
{
    private final List<Fragment> fragments;

    public SimpleApplyAdapter(@NonNull FragmentManager fragmentManager,
                              List<Fragment> fragments)
    {
        super(fragmentManager);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        return fragments.get(position);
    }

    @Override
    public int getCount()
    {
        return fragments.size();
    }
}
