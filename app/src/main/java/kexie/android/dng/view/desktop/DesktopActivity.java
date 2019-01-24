package kexie.android.dng.view.desktop;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

import kexie.android.dng.R;
import kexie.android.dng.adapter.DesktopFunctionAdapter;
import kexie.android.dng.databinding.ActivityDesktopBinding;
import kexie.android.dng.entity.desktop.Function;
import kexie.android.dng.entity.desktop.User;
import kexie.android.dng.view.users.UsersActivity;
import kexie.android.dng.viewmodel.desktop.DesktopViewModel;
import kexie.android.navi.view.RouteQueryActivity;

public class DesktopActivity extends AppCompatActivity
{
    private ActivityDesktopBinding binding;
    private DesktopViewModel viewModel;
    private DesktopFunctionAdapter functionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_desktop);

        binding.setLifecycleOwner(this);

        binding.setHandler(this);

        functionAdapter = new DesktopFunctionAdapter();

        binding.setAdapter(functionAdapter);

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
                        functionAdapter.setNewData(desktopFunctions);
                    }
                });
        functionAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position)
            {
                jumpTo(functionAdapter.getData().get(position).name);
            }
        });
    }

    public void jumpTo(String text)
    {
        switch (text)
        {
            case "APPS":
            {

            }
            break;
            case "导航":
            {
                RouteQueryActivity.startOf(this);
            }
            break;
            case "个人信息":
            {
                this.startActivity(new Intent(this,
                                UsersActivity.class),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                                binding.cvCardView,
                                "card").toBundle());
            }
            break;
            case "多媒体":
            {

            }
            break;
        }
    }


}
