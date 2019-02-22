package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.navi.view.RouteOverLay;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/navi/route")
public class RouteFragment extends Fragment
{
    private FragmentRouteBinding binding;

    private AMap mapController;

    NaviViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_route, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        TextureSupportMapFragment mapFragment
                = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        mapController = mapFragment.getMap();
        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            viewModel = ViewModelProviders.of(getActivity())
                    .get(NaviViewModel.class);
            int id = bundle.getInt("pathId");

            binding.setOnJumpToNavi(v -> viewModel.jumpToNavi(id));
            mapController.setOnMapClickListener(latLng ->{
                Logger.d(latLng);
                viewModel.jumpToDetails(id);
            });

            RouteOverLay routeOverLay = new RouteOverLay(mapController,
                    viewModel.getPath(id),
                    getContext().getApplicationContext());

            routeOverLay.setTrafficLine(false);
            routeOverLay.addToMap();

            binding.infosList.setGuideData(viewModel.getGuideInfo(id));
            binding.setRoute(viewModel.getRouteInfo(id));
            viewModel.getOnJump()
                    .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(this::jumpTo);
        }
    }

    private void jumpTo(Request request)
    {
        Logger.d(request);
        Fragment parent = getParentFragment();
        parent.getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(parent.getId(), Mapper.getOn(parent, request))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
