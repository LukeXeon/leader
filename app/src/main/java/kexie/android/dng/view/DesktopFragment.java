package kexie.android.dng.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import kexie.android.dng.R;
import kexie.android.dng.databinding.FragmentDesktopBinding;
import kexie.android.dng.viewmodel.DesktopViewModel;
import kexie.android.navi.view.RouteQueryFragment;

public class DesktopFragment extends Fragment
{
    private FragmentDesktopBinding binding;
    private DesktopViewModel viewModel;

    @Nullable
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
        FunctionFactory.getDefault(getContext(), (result) -> {
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
                    UserInfoFragment userInfo = new UserInfoFragment();
                    getFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(R.id.fragment_container, userInfo)
                            .show(userInfo)
                            .addToBackStack(null)
                            .commit();
                });
                put("导航", v -> {
                    RouteQueryFragment query = new RouteQueryFragment();
                    getFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(R.id.fragment_container, query)
                            .show(query)
                            .addToBackStack(null)
                            .commit();
                });
            }
        };
    }
}
