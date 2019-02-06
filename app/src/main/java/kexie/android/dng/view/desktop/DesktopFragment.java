package kexie.android.dng.view.desktop;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kexie.android.dng.R;
import kexie.android.dng.databinding.FragmentDesktopBinding;
import kexie.android.dng.entity.desktop.Function;
import kexie.android.dng.entity.desktop.User;
import kexie.android.dng.viewmodel.desktop.DesktopViewModel;

public class DesktopFragment extends Fragment
{

    public static DesktopFragment newInstance()
    {
        Bundle args = new Bundle();

        DesktopFragment fragment = new DesktopFragment();
        fragment.setArguments(args);
        return fragment;
    }


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
                new Observer<User>()
                {
                    @Override
                    public void onChanged(@Nullable User userInfo)
                    {
                        binding.setUser(userInfo);
                    }
                });
        viewModel.getTime().observe(this,
                new Observer<String>()
                {
                    @Override
                    public void onChanged(@Nullable String s)
                    {
                        binding.setTime(s);
                    }
                });
        viewModel.getFunctions().observe(this,
                new Observer<List<Function>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Function> desktopFunctions)
                    {
                        binding.setFunctions(desktopFunctions);
                    }
                });
        binding.setActions(getActions());
    }

    public Map<String, View.OnClickListener> getActions()
    {
        return new HashMap<String, View.OnClickListener>()
        {
            {
                put("个人信息", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //UserInfoFragment.startOf(v.getContext());
                    }
                });
                put("导航", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //RouteQueryFragment.startOf(v.getContext());
                    }
                });
            }
        };
    }
}
