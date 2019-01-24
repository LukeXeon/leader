package kexie.android.navi.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import kexie.android.navi.R;
import kexie.android.navi.adapter.RouteBindingAdapter;
import kexie.android.navi.databinding.ActivityRouteQueryBinding;
import kexie.android.navi.databinding.ItemRouteBinding;
import kexie.android.navi.entity.Route;
import kexie.android.navi.viewmodel.RouteQueryViewModel;

public class RouteQueryActivity extends AppCompatActivity
{

    private final RouteBindingAdapter routeBindingAdapter = new RouteBindingAdapter();
    private final Stack<ItemRouteBinding> bindingCache = new Stack<>();
    private ActivityRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_route_query);

        for (int i = 0; i < 3; i++)
        {
            ItemRouteBinding itemBinding
                    = DataBindingUtil.inflate(getLayoutInflater(),
                    R.layout.item_route,
                    binding.vpPager,
                    false);
            bindingCache.add(itemBinding);
        }

        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);

        viewModel.getRoutes().observe(this,
                new Observer<List<Route>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Route> routes)
                    {
                        if (routes != null)
                        {
                            List<ItemRouteBinding> bindings = new ArrayList<>();
                            for (Route route : routes)
                            {
                                ItemRouteBinding routeBinding = bindingCache.pop();
                                routeBinding.setRoute(route);
                                bindings.add(routeBinding);
                            }
                            routeBindingAdapter.setBindings(bindings);
                            binding.setAdapter(routeBindingAdapter);
                        } else
                        {
                            binding.setAdapter(null);
                            List<ItemRouteBinding> bindings
                                    = routeBindingAdapter.getBindings();
                            if (bindings != null)
                            {
                                for (ItemRouteBinding binding : bindings)
                                {
                                    bindingCache.push(binding);
                                }
                            }
                        }
                    }
                });
    }


    public static void startOf(Activity context)
    {
        context.startActivity(new Intent(context, RouteQueryActivity.class));
    }
}
