package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.viewmodel.RouteMapViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapper;
import mapper.Mapping;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/navi/details")
public class DetailsFragment extends Fragment
{

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        RouteMapViewModel viewModel = ViewModelProviders.of(this)
                .get(RouteMapViewModel.class);
        TextureSupportMapFragment mapFragment
                = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        AMap mapController = mapFragment.getMap();
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            viewModel.init(bundle);
            mapController.addPolyline(viewModel.getLine());
            viewModel.getOnJump()
                    .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(request -> getFragmentManager()
                            .beginTransaction()
                            .add(getId(), Mapper.getOn(this, request))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit());
        }
    }
}
