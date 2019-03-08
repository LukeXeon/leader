package org.kexie.android.dng.navi.view;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.view.RouteOverLay;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

@Route(path = "/navi/navi")
public final class NaviFragment extends Fragment
{
    private FragmentNaviBinding binding;

    private AMap mapController;

    private NaviViewModel naviViewModel;

    private List<RouteOverLay> routeOverLays;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navi,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        binding.setLifecycleOwner(this);

        naviViewModel = ViewModelProviders.of(this).get(NaviViewModel.class);
        naviViewModel.getRoutes().observe(this, routeInfos -> {
            if (!routeInfos.isEmpty())
            {
                Context context = requireContext().getApplicationContext();
                mapController.moveCamera(CameraUpdateFactory.changeTilt(0));
                List<RouteOverLay> overLays = StreamSupport.stream(routeInfos.entrySet())
                        .map(entry -> new RouteOverLay(
                                mapController,
                                entry.getValue().path,
                                context))
                        .collect(Collectors.toList());
                StreamSupport.stream(overLays).forEach(routeOverLay -> {
                    routeOverLay.setTrafficLine(true);
                    routeOverLay.setArrowOnRoute(true);
                    routeOverLay.setLightsVisible(true);
                    routeOverLay.setTrafficLightsVisible(true);
                    routeOverLay.setTransparency(0.4f);
                    routeOverLay.addToMap();
                });
                overLays.get(0).zoomToSpan(200);
                routeOverLays = overLays;
            } else
            {
                mapController.clear();
                Location location = mapController.getMyLocation();
                if (location != null)
                {
                    mapController.moveCamera(CameraUpdateFactory.newLatLngZoom(Point.form(
                            location.getLongitude(),
                            location.getLatitude())
                            .unBox(LatLng.class), 10));
                }
            }
        });
        naviViewModel.getCurrentSelect().observe(this, select -> {
            if (select != null && select != NaviViewModel.NO_SELECT)
            {

            } else
            {

            }
        });
        naviViewModel.isRunning().observe(this, isRun -> {
            Postcard postcard;
            if (isRun != null && isRun)
            {
                postcard = ARouter.getInstance().build("/navi/navi");
            } else
            {
                postcard = ARouter.getInstance().build("/navi/query");
            }
            Fragment fragment = (Fragment) postcard.navigation();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_upper, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        });
        naviViewModel.isRunning().setValue(false);

        TextureSupportMapFragment mapFragment = TextureSupportMapFragment
                .class.cast(getChildFragmentManager()
                .findFragmentById(R.id.map_view));
        mapController = Objects.requireNonNull(mapFragment).getMap();
        MyLocationStyle myLocationStyle = new MyLocationStyle().interval(1000);
        mapController.setMyLocationStyle(myLocationStyle);
        mapController.setOnMyLocationChangeListener(location -> {
            MyLocationStyle myLocationStyle1 = new MyLocationStyle()
                    .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            mapController.setMyLocationStyle(myLocationStyle1);
            mapController.setOnMyLocationChangeListener(null);
        });
        mapController.setMyLocationEnabled(true);
        mapController.getUiSettings().setMyLocationButtonEnabled(true);
    }
}