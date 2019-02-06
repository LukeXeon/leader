package kexie.android.dng.view.userinfo;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import kexie.android.dng.R;
import kexie.android.dng.databinding.FragmentUserInfoBinding;
import kexie.android.dng.entity.users.SimpleUser;
import kexie.android.dng.viewmodel.userinfo.UsersViewModel;

public class UserInfoFragment extends Fragment
{
    private FragmentUserInfoBinding binding;
    private UsersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_info, container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
        binding.setActions(getActions());

    }

    public Map<String, View.OnClickListener> getActions()
    {
        return new HashMap<String, View.OnClickListener>()
        {
            {
                put("返回", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Activity activity = (Activity) v.getContext();
                        activity.finish();
                    }
                });
                put("刷新", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                    }
                });
            }
        };
    }
}
