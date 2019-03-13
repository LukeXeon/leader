package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentInfoBinding;
import org.kexie.android.dng.ux.viewmodel.InfoViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.ux.content_info)
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


    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        binding.setLifecycleOwner(this);

        viewModel = ViewModelProviders.of(requireActivity())
                .get(InfoViewModel.class);
        viewModel.user.observe(this, binding::setUser);
    }
}
