package kexie.android.navi.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;

import java.util.List;


import kexie.android.navi.R;
import kexie.android.navi.adapter.RouteBindingAdapter;
import kexie.android.navi.databinding.ActivityRouteQueryBinding;
import kexie.android.navi.entity.Route;
import kexie.android.navi.viewmodel.RouteQueryViewModel;

public class RouteQueryActivity extends AppCompatActivity
{

    private final RouteBindingAdapter routeBindingAdapter
            = new RouteBindingAdapter();
    private ActivityRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_route_query);

        binding.setAdapter(routeBindingAdapter);

        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);

        viewModel.getRoutes().observe(this,
                new Observer<List<Route>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Route> routes)
                    {
                        routeBindingAdapter.setNewData(routes);
                    }
                });
    }


    public static void startOf(Activity context)
    {
        context.startActivity(new Intent(context, RouteQueryActivity.class));
    }
}
