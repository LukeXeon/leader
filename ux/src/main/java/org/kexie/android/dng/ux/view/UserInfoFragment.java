package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentUserInfoBinding;
import org.kexie.android.dng.ux.viewmodel.UsersViewModel;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;

public class UserInfoFragment extends Fragment
{
    private FragmentUserInfoBinding binding;
    private UsersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_info, container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        viewModel = ViewModelProviders.of(this)
                .get(UsersViewModel.class);
        //dataBinding
        binding.blurView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(
                        getActivity().getWindow()
                                .getDecorView()
                                .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
        binding.setActions(binding.getActions());
        binding.setActions(getActions());
        //liveData
        viewModel.getHeadImage().observe(this, binding::setHeadImage);
        viewModel.getSimpleUser().observe(this, binding::setUser);
    }

    private Map<String, View.OnClickListener> getActions()
    {
        return new ArrayMap<String, View.OnClickListener>()
        {
            {
                put("返回", v -> getFragmentManager().popBackStack());
            }
        };
    }
}
