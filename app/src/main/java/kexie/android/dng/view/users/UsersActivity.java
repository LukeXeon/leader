package kexie.android.dng.view.users;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Map;

import kexie.android.dng.R;
import kexie.android.dng.databinding.ActivityUsersBinding;
import kexie.android.dng.entity.users.SimpleUser;
import kexie.android.dng.viewmodel.users.UsersViewModel;

public class UsersActivity extends AppCompatActivity
{
    private ActivityUsersBinding binding;
    private UsersViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_users);

        binding.setLifecycleOwner(this);

        viewModel = ViewModelProviders.of(this).get(UsersViewModel.class);

        binding.setActions(binding.getActions());

        viewModel.getHeadImage().observe(this,
                new Observer<Drawable>()
        {
            @Override
            public void onChanged(@Nullable Drawable drawable)
            {
                binding.setHeadImage(drawable);
            }
        });

        viewModel.getSimpleUser().observe(this,
                new Observer<SimpleUser>()
                {
                    @Override
                    public void onChanged(@Nullable SimpleUser simpleUser)
                    {
                        binding.setUser(simpleUser);
                    }
                });


    }

    public static void startOf(Context activity)
    {
        activity.startActivity(new Intent(activity, UsersActivity.class));
    }
}
