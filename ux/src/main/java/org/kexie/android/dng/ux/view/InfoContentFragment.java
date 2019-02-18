package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentInfoContentBinding;
import org.kexie.android.dng.ux.viewmodel.InfoViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;


public class InfoContentFragment extends Fragment
{
    private FragmentInfoContentBinding binding;
    private InfoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_info_content, container,
                false);

        return binding.getRoot();
    }

    @SuppressWarnings("All")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag("dng/ux/main"))
                .get(InfoViewModel.class);
        binding.setLifecycleOwner(this);
        //dataBinding

        //liveData
        viewModel.getUser().observe(this, binding::setUser);
    }
}
