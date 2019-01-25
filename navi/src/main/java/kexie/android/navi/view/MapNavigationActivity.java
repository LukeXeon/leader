package kexie.android.navi.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import kexie.android.navi.R;
import kexie.android.navi.databinding.ActivityNavigationBinding;
import kexie.android.navi.entity.Point;
import kexie.android.navi.viewmodel.MapNavigationViewModel;

public class MapNavigationActivity extends AppCompatActivity
{
    private static final String ARG = "points";
    private MapNavigationViewModel viewModel;
    private ActivityNavigationBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ArrayList<Point> points = intent.getParcelableArrayListExtra(ARG);

        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_navigation);
        viewModel = ViewModelProviders.of(this)
                .get(MapNavigationViewModel.class);

        viewModel.begin(points);
    }

    public static void startOf(Context context, List<Point> points)
    {
        Intent intent = new Intent(context, MapNavigationActivity.class);
        intent.putParcelableArrayListExtra(ARG,
                points instanceof ArrayList
                        ? (ArrayList<Point>) points
                        : new ArrayList<>(points));
        context.startActivity(intent);
    }
}
