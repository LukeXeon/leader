package kexie.android.dng.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import kexie.android.dng.R;
import kexie.android.dng.databinding.FragmentUserInfoBinding;
import kexie.android.dng.viewmodel.UsersViewModel;

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

        binding.setActions(binding.getActions());

        viewModel.getHeadImage().observe(this,
                drawable -> binding.setHeadImage(drawable));

        viewModel.getSimpleUser().observe(this,
                simpleUser -> binding.setUser(simpleUser));
        binding.setActions(getActions());
    }

    public Map<String, View.OnClickListener> getActions()
    {
        return new HashMap<String, View.OnClickListener>()
        {
            {
                put("返回", v -> {
                    getFragmentManager().popBackStack();
                });
                put("刷新", v -> {

                });
            }
        };
    }
}
