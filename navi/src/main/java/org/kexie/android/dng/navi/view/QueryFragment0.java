package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.View;

import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

public class QueryFragment0 extends Fragment
{
    private NaviViewModel naviViewModel;

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        naviViewModel = ViewModelProviders.of(Objects.requireNonNull(getTargetFragment()))
                .get(NaviViewModel.class);
        Transformations.map(naviViewModel.routeInfos, Map::keySet)
                .observe(this, x -> {

                });


    }
}
