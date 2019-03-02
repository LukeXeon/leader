package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.navi.view.RouteOverLay;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapper;
import mapper.Request;

public class RouteFragment extends Fragment
{
    private FragmentRouteBinding binding;

    private AMap mapController;

    private NaviViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        if (binding == null)
        {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_route, container,
                    false);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.setLifecycleOwner(this);

        viewModel = ViewModelProviders.of(
                Objects.requireNonNull(requireParentFragment()
                        .getTargetFragment()))
                .get(NaviViewModel.class);

        TextureSupportMapFragment mapFragment
                = TextureSupportMapFragment.class
                .cast(getChildFragmentManager().findFragmentById(R.id.map_view));

        mapController = Objects.requireNonNull(mapFragment).getMap();

        UiSettings uiSettings = mapController.getUiSettings();

        uiSettings.setScrollGesturesEnabled(false);

        uiSettings.setZoomGesturesEnabled(false);

        uiSettings.setTiltGesturesEnabled(false);

        uiSettings.setRotateGesturesEnabled(false);

        uiSettings.setZoomControlsEnabled(false);

        binding.setOnJumpToDetails(v -> {
            Request request = new Request.Builder().uri("dng/navi/details").build();
            jumpTo(request);
        });

    }

    public void apply(int id)
    {
        viewModel.routeInfos
                .observe(this, routeInfos -> {
                    RouteInfo routeInfo = Objects.requireNonNull(routeInfos.get(id));

                    mapController.setMapStatusLimits(routeInfo.bounds);

                    mapController.moveCamera(CameraUpdateFactory.zoomOut());
                    mapController.moveCamera(CameraUpdateFactory.zoomOut());
                    mapController.moveCamera(CameraUpdateFactory.zoomOut());

                    RouteOverLay routeOverLay = new RouteOverLay(mapController,
                            routeInfo.path,
                            requireContext().getApplicationContext());
                    routeOverLay.setTrafficLine(false);
                    routeOverLay.addToMap();

                    binding.infosList.setGuideData(routeInfo.guideInfos);

                    binding.setOnJumpToNavi(x -> {
                        Request request = new Request.Builder()
                                .uri("dng/navi/navi")
                                .build();
                        jumpTo(request);
                    });

                });
    }


    private void jumpTo(Request request)
    {
        Fragment parent = requireParentFragment();
        parent.requireFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .hide(parent)
                .add(parent.getId(), Mapper.getOn(parent, request))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
