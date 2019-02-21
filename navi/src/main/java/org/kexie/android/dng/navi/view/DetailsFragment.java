package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentDetailsBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.RouteMapViewModel;
import org.kexie.android.dng.navi.widget.DrivingRouteOverlay;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
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
    private FragmentDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_details,
                container,
                false);
        return binding.getRoot();
    }

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
            //mapController.addPolyline(viewModel.getLine());
            test(mapController,bundle);
            viewModel.getOnJump()
                    .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(request -> getFragmentManager()
                            .beginTransaction()
                            .add(getId(), Mapper.getOn(this, request))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit());
        }
    }

    private void test(AMap aMap,Bundle bundle)
    {
        Point from = bundle.getParcelable("from");
        Point to = bundle.getParcelable("to");
        DrivePath path = bundle.getParcelable("path");
        DrivingRouteOverlay overlay = new DrivingRouteOverlay(getContext(),aMap,path,from.unBox(LatLonPoint.class),to.unBox(LatLonPoint.class), Collections.emptyList());
        overlay.addToMap();
    }
}
