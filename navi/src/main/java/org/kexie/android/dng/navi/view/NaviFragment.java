package org.kexie.android.dng.navi.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.CrossOverlay;
import com.amap.api.maps.model.CrossOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.RouteOverlayOptions;
import com.amap.api.navi.view.AmapCameraOverlay;
import com.amap.api.navi.view.RouteOverLay;
import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.AnimationAdapter;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.NaviViewModelFactory;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.RunningViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;
import org.kexie.android.dng.navi.viewmodel.entity.RunningInfo;
import org.kexie.android.dng.navi.widget.AMapCompatFragment;
import org.kexie.android.dng.navi.widget.CarMarker;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.collection.SparseArrayCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import me.jessyan.autosize.utils.AutoSizeUtils;

import static androidx.lifecycle.Lifecycle.Event;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = PR.navi.navi)
public final class NaviFragment extends Fragment
{

    private FragmentNaviBinding binding;

    private AMap mapController;

    private QueryViewModel queryViewModel;

    private RunningViewModel runningViewModel;

    private SparseArrayCompat<RouteOverLay> routeOverLays;

    private CrossOverlay crossOverlay;

    private AMapNavi navi;

    private CarMarker carMarker;

    private AmapCameraOverlay cameraOverlay;

    private Map<Circle, Animator> circles;

    private Observable<Location> uiLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navi = AMapNavi.getInstance(requireContext().getApplicationContext());
        NaviViewModelFactory factory = new NaviViewModelFactory(requireContext(), navi);
        runningViewModel = ViewModelProviders.of(this, factory).get(RunningViewModel.class);
        queryViewModel = ViewModelProviders.of(this, factory).get(QueryViewModel.class);
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

    @SuppressLint({"ClickableViewAccessibility", "MissingPermission"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        Glide.with(this)
                .load(R.drawable.image_splash)
                .into(binding.loading);
        binding.loading.setOnTouchListener((x, y) -> true);

        AMapCompatFragment mapFragment = (AMapCompatFragment)
                Objects.requireNonNull(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        mapController = Objects.requireNonNull(mapFragment.getMap());
        mapController.setOnMapLoadedListener(() -> {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(1000);
            alphaAnimation.setAnimationListener(new AnimationAdapter()
            {
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    binding.loading.setVisibility(View.GONE);
                    binding.loading.setOnTouchListener(null);
                    uiLocation.firstElement()
                            .as(autoDisposable(from(getLifecycle(), Event.ON_DESTROY)))
                            .subscribe(location -> zoomMapToLocation());
                }
            });
            binding.loading.startAnimation(alphaAnimation);
            mapController.setOnMapLoadedListener(null);
        });

        carMarker = new CarMarker(requireContext(), mapController);
        cameraOverlay = new AmapCameraOverlay(requireContext());

        PublishSubject<Location> subject = PublishSubject.create();
        mapController.setOnMyLocationChangeListener(subject::onNext);
        uiLocation = subject;
        //no run
        queryViewModel.getRoutes().observe(this, routeInfos -> {
            if (!routeInfos.isEmpty())
            {
                drawRoutes(routeInfos);
                //默认选择第一条路
            } else
            {
                clearRoutes();
            }
        });
        queryViewModel.getCurrentSelect().observe(this, select -> {
            if (select != null && select != QueryViewModel.NO_SELECT
                    && routeOverLays.size() != 0)
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
        runningViewModel.isLockCamera().observe(this,
                data -> carMarker.setLock(data));
        runningViewModel.getLocation().observe(this,
                data -> carMarker.draw(Point.box(data.getCoord()), data.getBearing()));
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
                postcard = ARouter.getInstance().build(PR.navi.running);
                Logger.d(PR.navi.running);
                setRunningMapState();
            } else
            {
                postcard = ARouter.getInstance().build(PR.navi.query);
                Logger.d(PR.navi.query);
                setQueryMapState();
            }
            Fragment fragment = (Fragment) postcard.navigation();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_upper, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitAllowingStateLoss();
        });
        runningViewModel.isRunning().setValue(false);
    }

    private void clearRoutes()
    {
        zoomMapToLocation();
        if (routeOverLays != null)
        {
            for (int i = 0; i < routeOverLays.size(); i++)
            {
                routeOverLays.valueAt(i).destroy();
            }
            routeOverLays.clear();
        }
    }

    private void drawRoutes(Map<Integer, RouteInfo> routeInfos)
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
        for (int i = 1; i < overLays.size(); i++)
        {
            LatLngBounds bounds = overLays
                    .valueAt(i)
                    .getAMapNaviPath()
                    .getBoundsForPath();
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
    }

    private void zoomMapToLocation()
    {
        Location location = mapController.getMyLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                mapController.getMaxZoomLevel() - 2);
        mapController.animateCamera(cameraUpdate, 1000,
                new AMap.CancelableCallback()
                {
                    @Override
                    public void onFinish()
                    {

                    }

                    @Override
                    public void onCancel()
                    {
                        mapController.moveCamera(cameraUpdate);
                    }
                });
    }

    private void setRunningMapState()
    {
        Integer select = queryViewModel.getCurrentSelect().getValue();
        if (select != null && select != QueryViewModel.NO_SELECT)
        {
            Logger.d("select=" + select);
            for (int i = 0; i < routeOverLays.size(); i++)
            {
                RouteOverLay routeOverLay = routeOverLays.valueAt(i);

                if (routeOverLays.keyAt(i) == select)
                {
                    selectRoute(routeOverLay);
                    startNavi();
                    break;
                }
            }
            removeQueryMapState();
        }
    }

    private void startNavi()
    {
        Location location = mapController.getMyLocation();
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        carMarker.setLock(false);
        carMarker.draw(Point.box(latLng), location.getBearing());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .bearing(location.getBearing())
                .tilt(80)
                .zoom(20)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        mapController.animateCamera(cameraUpdate,
                1000, new AMap.CancelableCallback()
                {
                    @Override
                    public void onFinish()
                    {
                        carMarker.setLock(true);
                        runningViewModel.start();
                    }

                    @Override
                    public void onCancel()
                    {
                        onFinish();
                    }
                });
    }

    private void selectRoute(RouteOverLay routeOverLay)
    {
        Resources resources = getResources();

        Bitmap smoothTraffic = BitmapFactory.decodeResource(resources, R.mipmap.map_1);
        Bitmap unknownTraffic = BitmapFactory.decodeResource(resources, R.mipmap.map_2);
        Bitmap slowTraffic = BitmapFactory.decodeResource(resources, R.mipmap.map_3);
        Bitmap jamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.map_4);
        Bitmap veryJamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.map_5);

        RouteOverlayOptions routeOverlayOptions = new RouteOverlayOptions();

        routeOverlayOptions.setSmoothTraffic(smoothTraffic);
        routeOverlayOptions.setUnknownTraffic(unknownTraffic);
        routeOverlayOptions.setSlowTraffic(slowTraffic);
        routeOverlayOptions.setJamTraffic(jamTraffic);
        routeOverlayOptions.setVeryJamTraffic(veryJamTraffic);

        routeOverLay.setRouteOverlayOptions(routeOverlayOptions);
        routeOverLay.setTrafficLine(true);
        routeOverLay.setTrafficLightsVisible(true);
        routeOverLay.setLightsVisible(true);
        routeOverLay.setNaviArrowVisible(false);
    }

    private void removeQueryMapState()
    {
        mapController.setMyLocationEnabled(false);
        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        StreamSupport.stream(circles.entrySet())
                .forEach(entry -> {
                    entry.getValue().cancel();
                    entry.getKey().remove();
                });
        circles.clear();
    }

    private void setQueryMapState()
    {
        MyLocationStyle myLocationStyle = new MyLocationStyle()
                .radiusFillColor(Color.TRANSPARENT)
                .strokeColor(Color.TRANSPARENT)
                .interval(1000)
                .strokeWidth(0);
        mapController.setMyLocationStyle(myLocationStyle);
        uiLocation.map(location -> new LatLng(location.getLatitude(), location.getLongitude()))
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(location -> {
                    if (circles == null)
                    {
                        MyLocationStyle myLocationStyle1 = mapController
                                .getMyLocationStyle()
                                .myLocationType(MyLocationStyle
                                        .LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
                        mapController.setMyLocationStyle(myLocationStyle1);
                        circles = new ArrayMap<>();
                        IntStreams.iterate(0, i -> i < 3, i -> i + 1)
                                .forEach(i -> {
                                    Circle circle = addCircle(location);
                                    Animator animator = startScaleCircleAnimation(circle,
                                            i * 800);
                                    circles.put(circle, animator);
                                });
                    } else
                    {
                        StreamSupport.stream(circles.keySet())
                                .forEach(circle -> circle.setCenter(location));
                    }
                });
        mapController.setMyLocationEnabled(true);
        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
    }
    
    private Circle addCircle(LatLng latLng)
    {
        float accuracy = (float) ((latLng.longitude / latLng.latitude) * 20);
        return mapController.addCircle(new CircleOptions()
                .center(latLng)
                .fillColor(Color.argb(0, 98, 198, 255))
                .radius(accuracy)
                .strokeColor(Color.argb(0, 98, 198, 255))
                .strokeWidth(0));
    }

    private static Animator startScaleCircleAnimation(Circle circle, long startDelay)
    {
        ValueAnimator vm = ValueAnimator.ofFloat(0, (float) circle.getRadius());
        vm.addUpdateListener(animation -> circle.setRadius((float) animation.getAnimatedValue()));
        ValueAnimator vm1 = ValueAnimator.ofInt(160, 0);
        vm1.addUpdateListener(animation -> circle
                .setFillColor(Color.argb((int) animation.getAnimatedValue(),
                        98,
                        198,
                        255)));
        vm.setRepeatCount(ValueAnimator.INFINITE);
        vm.setRepeatMode(ValueAnimator.RESTART);
        vm1.setRepeatCount(ValueAnimator.INFINITE);
        vm1.setRepeatMode(ValueAnimator.RESTART);
        AnimatorSet set = new AnimatorSet();
        set.setStartDelay(startDelay);
        set.playTogether(vm, vm1);
        set.setDuration(2500);
        set.setInterpolator(new LinearInterpolator());
        set.start();
        return set;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        navi.destroy();
    }
}