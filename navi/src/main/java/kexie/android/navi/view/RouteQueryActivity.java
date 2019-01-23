package kexie.android.navi.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import kexie.android.navi.R;
import kexie.android.navi.databinding.ActivityRouteQueryBinding;
import kexie.android.navi.viewmodel.RouteQueryViewModel;

public class RouteQueryActivity extends AppCompatActivity
{

    private ActivityRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_route_query);

        setSupportActionBar(binding.tbToolBar);
        getSupportActionBar().setTitle("");

        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);
    }


    public static void startOf(Activity context)
    {
        context.startActivity(new Intent(context, RouteQueryActivity.class));
    }
}
