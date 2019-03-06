package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentInfoBinding;
import org.kexie.android.dng.ux.viewmodel.InfoViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapping;

@Mapping("dng/ux/info")
public class InfoFragment extends Fragment
{
    private FragmentInfoBinding binding;
    private InfoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_info, container,
                false);

        return binding.getRoot();
    }

    @SuppressWarnings("All")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(false);

        viewModel = ViewModelProviders.of(requireParentFragment().getTargetFragment())
                .get(InfoViewModel.class);

        binding.setLifecycleOwner(this);

        //liveData
        viewModel.user.observe(this, binding::setUser);
    }
}
