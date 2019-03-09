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
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CrossOverlay;
import com.amap.api.maps.model.CrossOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.NaviViewModelFactory;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.ModeCross;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;
import org.kexie.android.dng.navi.viewmodel.entity.RunningInfo;
import org.kexie.android.dng.navi.widget.AMapCompat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import me.jessyan.autosize.utils.AutoSizeUtils;

@Route(path = "/navi/navi")
public final class NaviFragment extends Fragment
{
    private FragmentNaviBinding binding;

    private AMap mapController;

    private QueryViewModel queryViewModel;

    private NaviViewModel naviViewModel;

    private AMapNavi navi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        navi = AMapNavi.getInstance(requireContext().getApplicationContext());
    }

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

        NaviViewModelFactory factory = new NaviViewModelFactory(requireContext(), navi);

        binding.setLifecycleOwner(this);

        naviViewModel = ViewModelProviders.of(this, factory).get(NaviViewModel.class);
        queryViewModel = ViewModelProviders.of(this, factory).get(QueryViewModel.class);
        //no run
        queryViewModel.getRoutes().observe(this, new Observer<Map<Integer, RouteInfo>>()
        {
            private List<RouteOverLay> routeOverLays;

            @Override
            public void onChanged(Map<Integer, RouteInfo> routeInfos)
            {
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
                    LatLngBounds latLngBounds = overLays.get(0)
                            .getAMapNaviPath()
                            .getBoundsForPath();
                    for (RouteOverLay overLay : overLays.subList(1, overLays.size()))
                    {
                        LatLngBounds bounds = overLay.getAMapNaviPath().getBoundsForPath();
                        if (bounds.contains(latLngBounds))
                        {
                            latLngBounds = bounds;
                        }
                    }
                    CameraUpdate update = CameraUpdateFactory.newLatLngBoundsRect(latLngBounds,
                            200, AutoSizeUtils.dp2px(requireContext(), 450), 300, 300);
                    mapController.animateCamera(update);
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
                    routeOverLays = Collections.emptyList();
                }
            }
        });
        queryViewModel.getCurrentSelect().observe(this, select -> {
            if (select != null && select != QueryViewModel.NO_SELECT)
            {

            } else
            {

            }
        });

        //running
        naviViewModel.getModeCross().observe(this, new Observer<ModeCross>()
        {
            private CrossOverlay crossOverlay;

            @Override
            public void onChanged(ModeCross data)
            {
                if (data != null)
                {
                    crossOverlay = mapController.addCrossOverlay(
                            new CrossOverlayOptions()
                                    .setAttribute(data.getAttr())
                                    .setRes(data.getRes()));
                    crossOverlay.setData(data.getBuffer());
                    crossOverlay.setVisible(true);
                } else
                {
                    if (crossOverlay != null)
                    {
                        crossOverlay.setVisible(false);
                        crossOverlay.remove();
                    }
                }
            }
        });
        naviViewModel.getRunningInfo().observe(this, new Observer<RunningInfo>()
        {
            private int roadIndex;
            private RouteOverLay routeOverLay;

            @Override
            public void onChanged(RunningInfo data)
            {
                if (data != null)
                {
                    if (roadIndex != data.getCurStep())
                    {
                        List<NaviLatLng> arrow = routeOverLay.getArrowPoints(data.getCurStep());
                        if (arrow != null && arrow.size() > 0)
                        {
                            routeOverLay.drawArrow(arrow);
                            roadIndex = data.getCurStep();
                        }
                    }
                }
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
                    .replace(R.id.map_upper, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        });
        naviViewModel.isRunning().setValue(false);

        TextureSupportMapFragment mapFragment = Objects
                .requireNonNull(TextureSupportMapFragment
                        .class.cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view)));
        mapController = mapFragment.getMap();
        MyLocationStyle myLocationStyle = new MyLocationStyle().interval(1000);
        mapController.setMyLocationStyle(myLocationStyle);
        mapController.setOnMyLocationChangeListener(location -> {
            MyLocationStyle myLocationStyle1 = new MyLocationStyle()
                    .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            mapController.setMyLocationStyle(myLocationStyle1);
            mapController.setOnMyLocationChangeListener(null);
        });
        mapController.setMyLocationEnabled(true);
        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        AMapCompat.hideLogo(mapFragment);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        navi.destroy();
    }
}