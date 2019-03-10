package org.kexie.android.dng.navi.view;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CrossOverlay;
import com.amap.api.maps.model.CrossOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.AmapCameraOverlay;
import com.amap.api.navi.view.RouteOverLay;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.NaviViewModelFactory;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.RunningViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RunningInfo;
import org.kexie.android.dng.navi.widget.AMapCompatFragment;
import org.kexie.android.dng.navi.widget.CarOverlay;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import java8.util.stream.StreamSupport;
import me.jessyan.autosize.utils.AutoSizeUtils;

@Route(path = "/navi/navi")
public final class NaviFragment extends Fragment
{
    private FragmentNaviBinding binding;

    private AMap mapController;

    private QueryViewModel queryViewModel;

    private RunningViewModel runningViewModel;

    private SparseArrayCompat<RouteOverLay> routeOverLays;

    private CrossOverlay crossOverlay;

    private AMapNavi navi;

    private CarOverlay carOverlay;

    private AmapCameraOverlay cameraOverlay;

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

        AMapCompatFragment mapFragment = (AMapCompatFragment)
                Objects.requireNonNull(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        mapController = mapFragment.getMap();
        carOverlay = new CarOverlay(requireContext());
        cameraOverlay = new AmapCameraOverlay(requireContext());

        NaviViewModelFactory factory = new NaviViewModelFactory(requireContext(), navi);

        binding.setLifecycleOwner(this);

        runningViewModel = ViewModelProviders.of(this, factory).get(RunningViewModel.class);
        queryViewModel = ViewModelProviders.of(this, factory).get(QueryViewModel.class);
        //no run
        queryViewModel.getRoutes().observe(this, routeInfos -> {
            if (!routeInfos.isEmpty())
            {
                Context context = requireContext().getApplicationContext();
                Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                mapController.moveCamera(CameraUpdateFactory.changeTilt(0));
                SparseArrayCompat<RouteOverLay> overLays = new SparseArrayCompat<>();
                StreamSupport.stream(routeInfos.entrySet())
                        .forEach(entry -> {
                            RouteOverLay overLay = new RouteOverLay(
                                    mapController,
                                    entry.getValue().path,
                                    context);
                            overLay.setStartPointBitmap(bitmap);
                            overLay.setTrafficLine(false);
                            overLay.addToMap();
                            overLays.put(entry.getKey(), overLay);
                        });
                LatLngBounds latLngBounds = overLays.valueAt(0)
                        .getAMapNaviPath()
                        .getBoundsForPath();
                for (int i = 0; i < overLays.size(); i++)
                {
                    LatLngBounds bounds = overLays.valueAt(i).getAMapNaviPath().getBoundsForPath();
                    if (bounds.contains(latLngBounds))
                    {
                        latLngBounds = bounds;
                    }
                }
                Logger.d(AutoSizeUtils.dp2px(requireContext(), 450));
                CameraUpdate update = CameraUpdateFactory.newLatLngBoundsRect(latLngBounds,
                        AutoSizeUtils.dp2px(requireContext(), 100),
                        AutoSizeUtils.dp2px(requireContext(), 450),
                        AutoSizeUtils.dp2px(requireContext(), 50),
                        AutoSizeUtils.dp2px(requireContext(), 50));
                mapController.animateCamera(update, 1000, null);
                routeOverLays = overLays;
                //默认选择第一条路
                queryViewModel.select(overLays.keyAt(0));
            } else
            {
                mapController.clear();
                Location location = mapController.getMyLocation();
                if (location != null)
                {
                    mapController.animateCamera(CameraUpdateFactory.newLatLngZoom(Point.form(
                            location.getLongitude(),
                            location.getLatitude())
                            .unBox(LatLng.class), 10), 1000, null);
                }
                routeOverLays = new SparseArrayCompat<>();
            }
        });
        queryViewModel.getCurrentSelect().observe(this, select -> {
            if (select != null && select != QueryViewModel.NO_SELECT)
            {
                for (int i = 0; i < routeOverLays.size(); i++)
                {
                    RouteOverLay routeOverLay = routeOverLays.valueAt(i);
                    routeOverLay.setTransparency(0.4f);
                }
                RouteOverLay routeOverLay = routeOverLays.get(select);
                if (routeOverLay != null)
                {
                    routeOverLay.setTransparency(1);
                    routeOverLay.setZindex(Integer.MAX_VALUE);
                }
            }
        });

        //running
        runningViewModel.isLockCamera().observe(this, carOverlay::setLock);
        runningViewModel.getLocation().observe(this,
                location -> carOverlay.draw(mapController, new LatLng(
                                location.getCoord()
                                        .getLatitude(),
                                location.getCoord()
                                        .getLongitude()),
                        location.getBearing()));
        runningViewModel.getCameraInfo().observe(this,
                cameraInfos -> cameraOverlay.draw(mapController, cameraInfos));
        runningViewModel.getModeCross().observe(this, data -> {
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
        });
        runningViewModel.getRunningInfo().observe(this, new Observer<RunningInfo>()
        {
            private int roadIndex = Integer.MAX_VALUE;

            @Override
            public void onChanged(RunningInfo data)
            {
                if (data != null)
                {
                    if (roadIndex != data.getCurStep())
                    {
                        Integer select = queryViewModel.getCurrentSelect().getValue();
                        RouteOverLay routeOverLay = routeOverLays.get(select == null ? 0 : select);
                        if (routeOverLay != null)
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
            }
        });
        runningViewModel.isRunning().observe(this, isRun -> {
            Postcard postcard;
            if (isRun != null && isRun)
            {
                postcard = ARouter.getInstance().build("/navi/running");
                Logger.d("/navi/running");


                Integer select = queryViewModel.getCurrentSelect().getValue();
                if (select != null && select != QueryViewModel.NO_SELECT)
                {
                    Logger.d("select=" + select);
                    for (int i = 0; i < routeOverLays.size(); i++)
                    {
                        RouteOverLay routeOverLay = routeOverLays.valueAt(i);
                        if (routeOverLays.keyAt(i) == select)
                        {
                            Logger.d("index=" + i);
                            routeOverLay.setTrafficLine(true);
                            routeOverLay.setTrafficLightsVisible(true);
                            routeOverLay.setLightsVisible(true);
                            routeOverLay.setNaviArrowVisible(false);
                            Location location = mapController.getMyLocation();
                            LatLng latLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            android.graphics.Point point = mapController.getProjection()
                                    .toScreenLocation(latLng);
                            point.x -= AutoSizeUtils.dp2px(requireContext(), 140);
                            latLng = mapController.getProjection().fromScreenLocation(point);
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(latLng)
                                    .bearing(location.getBearing())
                                    .tilt(80)
                                    .zoom(20)
                                    .build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory
                                    .newCameraPosition(cameraPosition);
                            carOverlay.setLock(false);
                            carOverlay.draw(mapController, latLng, location.getBearing());
                            mapController.animateCamera(cameraUpdate,
                                    1000, new AMap.CancelableCallback()
                                    {
                                        @Override
                                        public void onFinish()
                                        {
                                            carOverlay.setLock(true);
                                            runningViewModel.start();
                                        }

                                        @Override
                                        public void onCancel()
                                        {
                                            onFinish();
                                        }
                                    });
                        } else
                        {
                            routeOverLay.removeFromMap();
                        }
                    }

                    mapController.setMyLocationEnabled(false);
                    UiSettings uiSettings = mapController.getUiSettings();
                    uiSettings.setMyLocationButtonEnabled(false);
                    uiSettings.setZoomControlsEnabled(false);
                }
            } else
            {
                postcard = ARouter.getInstance().build("/navi/query");
                Logger.d("/navi/query");

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
            }
            Fragment fragment = (Fragment) postcard.navigation();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_upper, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        });
        runningViewModel.isRunning().setValue(false);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        navi.destroy();
    }
}