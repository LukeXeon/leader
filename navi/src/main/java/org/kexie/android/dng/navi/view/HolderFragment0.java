package org.kexie.android.dng.navi.view;

import android.os.Bundle;

import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class HolderFragment0 extends Fragment
{
    private NaviViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(NaviViewModel.class);
    }
}
