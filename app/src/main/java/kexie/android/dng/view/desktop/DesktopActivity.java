package kexie.android.dng.view.desktop;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import kexie.android.dng.R;
import kexie.android.dng.databinding.ActivityDesktopBinding;
import kexie.android.dng.entity.desktop.Function;
import kexie.android.dng.entity.desktop.User;
import kexie.android.dng.viewmodel.desktop.DesktopViewModel;

public class DesktopActivity extends AppCompatActivity
{
    private ActivityDesktopBinding binding;
    private DesktopViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_desktop);

        binding.setLifecycleOwner(this);

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
        viewModel.getListFunctions().observe(this,
                new Observer<List<Function>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Function> desktopFunctions)
                    {
                        binding.setFunctions(desktopFunctions);
                    }
                });
        binding.setActions(viewModel.getActions());
    }
}
