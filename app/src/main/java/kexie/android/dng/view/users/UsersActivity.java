package kexie.android.dng.view.users;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import kexie.android.dng.R;
import kexie.android.dng.databinding.ActivityUsersBinding;
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
        binding.setHandler(this);

        viewModel = ViewModelProviders.of(this).get(UsersViewModel.class);


    }

    public static void startOf(Context activity)
    {
        activity.startActivity(new Intent(activity, UsersActivity.class));
    }
}
