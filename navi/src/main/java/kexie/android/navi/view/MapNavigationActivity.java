package kexie.android.navi.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;
import kexie.android.common.widget.ProgressWidget;
import kexie.android.navi.R;
import kexie.android.navi.databinding.ActivityNavigationBinding;
import kexie.android.navi.entity.Route;
import kexie.android.navi.viewmodel.MapNavigationViewModel;

public class MapNavigationActivity extends AppCompatActivity
{
    private static final String WAIT_TAG ="wait";
    private static final String ARG = "route";
    private MapNavigationViewModel viewModel;
    private ActivityNavigationBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Route route = intent.getParcelableExtra(ARG);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_navigation);
        viewModel = ViewModelProviders.of(this)
                .get(MapNavigationViewModel.class);
        viewModel.calculate(route);
        viewModel.getCalculateResult().observe(this,
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean)
                    {
                        if (aBoolean != null && aBoolean)
                        {
                            Toasty.success(getApplicationContext(),
                                    "路径规划成功")
                                    .show();
                        } else
                        {
                            Toasty.success(getApplicationContext(),
                                    "路径规划失败，请检查网络连接")
                                    .show();
                        }
                    }
                });
        viewModel.getIsLoading().observe(this,
                ProgressWidget.getObserver(
                        this.getSupportFragmentManager()));
    }

    public static void startOf(Context context, Route route)
    {
        Intent intent = new Intent(context, MapNavigationActivity.class);
        intent.putExtra(ARG,route);
        context.startActivity(intent);
    }
}
