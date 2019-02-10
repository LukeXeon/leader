package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.navi.view.RouteQueryFragment;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopBinding;
import org.kexie.android.dng.ux.viewmodel.DesktopViewModel;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public final class DesktopFragment extends Fragment
{
    private FragmentDesktopBinding binding;
    private DesktopViewModel viewModel;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_desktop, container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this)
                .get(DesktopViewModel.class);
        getLifecycle().addObserver(viewModel);
        viewModel.getUserInfo().observe(this,
                userInfo -> binding.setUser(userInfo));
        viewModel.getTime().observe(this,
                s -> binding.setTime(s));
        FunctionFactory.getDefault(this, (result) -> {
            binding.setFunctions(result);
        });
        binding.setActions(getActions());
    }

    public Map<String, View.OnClickListener> getActions()
    {
        return new ArrayMap<String, View.OnClickListener>()
        {
            {

                put("个人信息", v -> {
                    getFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(getId(), new UserInfoFragment())
                            .addToBackStack(null)
                            .commit();
                });
                put("导航", v -> {
                    getFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(getId(), new RouteQueryFragment())
                            .addToBackStack(null)
                            .commit();
                });
            }
        };
    }
}
