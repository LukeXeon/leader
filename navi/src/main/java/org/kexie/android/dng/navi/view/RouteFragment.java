package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.navi.view.RouteOverLay;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = "/navi/route")
public final class RouteFragment extends Fragment
{
    private FragmentRouteBinding binding;

    private NaviViewModel naviViewModel;

    private AMap mapController;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        if (binding == null)
        {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_route,
                    container,
                    false);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        binding.setLifecycleOwner(this);

        naviViewModel = ViewModelProviders.of(requireParentFragment().requireParentFragment())
                .get(NaviViewModel.class);

        TextureSupportMapFragment mapFragment = TextureSupportMapFragment
                .class.cast(getChildFragmentManager().findFragmentById(R.id.map_view));

        mapController = Objects.requireNonNull(mapFragment).getMap();

        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setZoomControlsEnabled(false);

        Bundle bundle = getArguments();
        if (bundle != null)
        {

        }
    }

    private void apply(int id)
    {
        Map<Integer, RouteInfo> routeInfos = naviViewModel.getRoutes().getValue();

        if (routeInfos != null)
        {
            RouteInfo routeInfo = routeInfos.get(id);
            if (routeInfo != null)
            {
                binding.setRoute(routeInfo);
                binding.setOnJumpToNavi(v -> {

                });
                binding.setOnJumpToNavi(v -> {

                });

                RouteOverLay routeOverLay = new RouteOverLay(mapController,
                        routeInfo.path,
                        requireContext().getApplicationContext());
                routeOverLay.setTrafficLine(true);
                routeOverLay.addToMap();
                routeOverLay.zoomToSpan(200);


            }
        }
    }
}
