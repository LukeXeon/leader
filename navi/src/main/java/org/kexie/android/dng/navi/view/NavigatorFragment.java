package org.kexie.android.dng.navi.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

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
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.RouteOverlayOptions;
import com.amap.api.navi.view.AmapCameraOverlay;
import com.amap.api.navi.view.RouteOverLay;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNavigatorBinding;
import org.kexie.android.dng.navi.databinding.FragmentNavigatorPreviewBinding;
import org.kexie.android.dng.navi.databinding.FragmentNavigatorRunningBinding;
import org.kexie.android.dng.navi.model.beans.Point;
import org.kexie.android.dng.navi.viewmodel.NavigatorViewModel;
import org.kexie.android.dng.navi.viewmodel.beans.PathDescription;
import org.kexie.android.dng.navi.viewmodel.beans.TipText;
import org.kexie.android.dng.navi.widget.AMapCompatFragment;
import org.kexie.android.dng.navi.widget.CarMarker;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.collection.SparseArrayCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import me.jessyan.autosize.utils.AutoSizeUtils;

@Route(path = Module.Navi.navigator)
public class NavigatorFragment extends Fragment {

    private FragmentNavigatorBinding binding;

    private NavigatorViewModel viewModel;

    private AMap map;

    private int roadIndex = Integer.MAX_VALUE;

    private SparseArrayCompat<RouteOverLay> routeOverLays;

    private Map<Circle, Animator> circleAnimators;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(NavigatorViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navigator,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.setLifecycleOwner(this);
        map = bindMapToViewModel();
        viewModel.paths.observe(this, descriptions -> {
            if (descriptions.isEmpty()) {
                //默认选择第一条路
                clearRoutes();
            } else {
                drawRoutes(descriptions);
            }
        });
        viewModel.select.observe(this, select -> {
            if (select != NavigatorViewModel.NO_SELECT
                    && routeOverLays != null && routeOverLays.size() != 0) {
                for (int i = 0; i < routeOverLays.size(); i++) {
                    RouteOverLay routeOverLay = routeOverLays.valueAt(i);
                    routeOverLay.setTransparency(0.4f);
                }
                RouteOverLay routeOverLay = routeOverLays.get(select);
                if (routeOverLay != null) {
                    routeOverLay.setTransparency(1);
                    routeOverLay.setZindex(Integer.MAX_VALUE);
                }
            }
        });
        viewModel.naviDescription.observe(this, desc -> {
            if (desc != null) {
                if (roadIndex != desc.curStep) {
                    Integer select = viewModel.select.getValue();
                    RouteOverLay routeOverLay = routeOverLays.get(select == null ? 0 : select);
                    if (routeOverLay != null) {
                        List<NaviLatLng> arrow = routeOverLay.getArrowPoints(desc.curStep);
                        if (arrow != null && arrow.size() > 0) {
                            routeOverLay.drawArrow(arrow);
                            roadIndex = desc.curStep;
                        }
                    }
                }
            }
        });
        initMapState();
    }

    private void initMapState() {
        Fragment previewFragment = new PreviewFragment();
        setPreviewMapState();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.map_upper, previewFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
        viewModel.onPrepare.observe(this, __ -> {
            Fragment runningFragment = new RunningFragment();
            setRunningMapState();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_upper, runningFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitAllowingStateLoss();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == R.id.search_request_code) {
            if (Activity.RESULT_OK == resultCode && data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    TipText tipText = bundle.getParcelable("tip");
                    viewModel.enterPreviewModeByUser(tipText);
                }
            }
        }
    }

    private Observer<Location> locationObserver = location -> {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (circleAnimators == null) {
            MyLocationStyle myLocationStyle1 = map.getMyLocationStyle()
                    .myLocationType(MyLocationStyle
                            .LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            map.setMyLocationStyle(myLocationStyle1);
            circleAnimators = new ArrayMap<>();
            for (int i = 0; i < 3; i++) {
                Circle circle = addCircle(latLng);
                Animator animator = startScaleCircleAnimation(circle,
                        i * 800);
                circleAnimators.put(circle, animator);
            }
            zoomMapToLocation();
        } else {
            for (Circle circle : circleAnimators.keySet()) {
                circle.setCenter(latLng);
            }
        }
    };

    private void setPreviewMapState() {
        MyLocationStyle myLocationStyle = new MyLocationStyle()
                .radiusFillColor(Color.TRANSPARENT)
                .strokeColor(Color.TRANSPARENT)
                .interval(1000)
                .strokeWidth(0);
        map.setMyLocationStyle(myLocationStyle);
        viewModel.selfLocation.removeObserver(locationObserver);
        viewModel.selfLocation.observe(this, locationObserver);
        map.setMyLocationEnabled(true);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
    }

    private Circle addCircle(LatLng latLng) {
        float accuracy = (float) ((latLng.longitude / latLng.latitude) * 20);
        return map.addCircle(new CircleOptions()
                .center(latLng)
                .fillColor(Color.argb(0, 98, 198, 255))
                .radius(accuracy)
                .strokeColor(Color.argb(0, 98, 198, 255))
                .strokeWidth(0));
    }

    private static Animator startScaleCircleAnimation(Circle circle, long startDelay) {
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

    private void setRunningMapState() {
        Integer select = viewModel.select.getValue();
        if (select != null && select != NavigatorViewModel.NO_SELECT) {
            Logger.d("select=" + select);
            for (int i = 0; i < routeOverLays.size(); i++) {
                RouteOverLay routeOverLay = routeOverLays.valueAt(i);
                if (routeOverLays.keyAt(i) == select) {
                    selectRoute(routeOverLay);
                    animatedNavigationBegin();
                    break;
                }
            }
            removePreviewMapState();
        }
    }

    private void removePreviewMapState() {
        map.setMyLocationEnabled(false);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        if (circleAnimators == null) {
            return;
        }
        for (Map.Entry<Circle, Animator> entry : circleAnimators.entrySet()) {
            entry.getValue().cancel();
            entry.getKey().remove();
        }
        circleAnimators.clear();
    }

    private void animatedNavigationBegin() {
        Location location = map.getMyLocation();
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .bearing(location.getBearing())
                .tilt(80)
                .zoom(20)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate,
                1000, new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        viewModel.beginNavigation();
                    }

                    @Override
                    public void onCancel() {
                        onFinish();
                    }
                });
    }

    private void selectRoute(RouteOverLay routeOverLay) {
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

    private void zoomMapToLocation() {
        Location location = map.getMyLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                map.getMaxZoomLevel() - 2);
        map.animateCamera(cameraUpdate, 1000, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {
                map.moveCamera(cameraUpdate);
            }
        });
    }

    private void clearRoutes() {
        zoomMapToLocation();
        if (routeOverLays != null) {
            for (int i = 0; i < routeOverLays.size(); i++) {
                routeOverLays.valueAt(i).destroy();
            }
            routeOverLays.clear();
        }
    }

    private void drawRoutes(Collection<PathDescription> descriptions) {
        Context context = requireContext().getApplicationContext();
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        map.moveCamera(CameraUpdateFactory.changeTilt(0));
        SparseArrayCompat<RouteOverLay> overLays = new SparseArrayCompat<>();
        for (PathDescription it : descriptions) {
            RouteOverLay overLay = new RouteOverLay(map, it.path, context);
            overLay.setStartPointBitmap(bitmap);
            overLay.setTrafficLine(false);
            overLay.addToMap();
            overLays.put(it.id, overLay);
        }
        LatLngBounds latLngBounds = overLays.valueAt(0)
                .getAMapNaviPath()
                .getBoundsForPath();
        for (int i = 1; i < overLays.size(); i++) {
            LatLngBounds bounds = overLays
                    .valueAt(i)
                    .getAMapNaviPath()
                    .getBoundsForPath();
            if (bounds.contains(latLngBounds)) {
                latLngBounds = bounds;
            }
        }
        CameraUpdate update = CameraUpdateFactory.newLatLngBoundsRect(latLngBounds,
                AutoSizeUtils.dp2px(requireContext(), 100),
                AutoSizeUtils.dp2px(requireContext(), 450),
                AutoSizeUtils.dp2px(requireContext(), 50),
                AutoSizeUtils.dp2px(requireContext(), 50));
        map.animateCamera(update, 1000, null);
        routeOverLays = overLays;
    }

    private AMap bindMapToViewModel() {
        AMapCompatFragment mapCompatFragment = (AMapCompatFragment)
                getChildFragmentManager().findFragmentById(R.id.map_view);
        if (mapCompatFragment != null) {
            AMap aMap = mapCompatFragment.getMap();
            viewModel.bindMapLocation(aMap);
            return aMap;
        }
        throw new AssertionError();
    }

    public static final class RunningFragment extends Fragment {

        private CrossOverlay crossOverlay;

        private CarMarker carMarker;

        private AmapCameraOverlay cameraOverlay;

        private NavigatorViewModel viewModel;

        private AMap map;

        private long last = SystemClock.uptimeMillis();

        private FragmentNavigatorRunningBinding binding;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            viewModel = ViewModelProviders.of(requireParentFragment())
                    .get(NavigatorViewModel.class);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_navigator_running,
                    container,
                    false);
            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view,
                                  @Nullable Bundle savedInstanceState) {
            binding.setLifecycleOwner(this);
            map = getMap();
            carMarker = CarMarker.getDefault(requireContext(), map);
            cameraOverlay = new AmapCameraOverlay(requireContext());
            Location location = map.getMyLocation();
            LatLng latLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            carMarker.draw(Point.box(latLng), location.getBearing());
            viewModel.naviDescription.observe(this, naviDescription -> {
                binding.myTrafficBar.update(
                        naviDescription.allLength,
                        naviDescription.pathRetainDistance,
                        naviDescription.trafficStatuses
                );
                binding.iconNextTurnTip.setIconType(naviDescription.iconType);
                binding.textNextRoadName.setText(naviDescription.nextRoadName);
                binding.textNextRoadDistance.setText(naviDescription.nextRoadDistance);
            });
            viewModel.isRunning.observe(this, isRunning -> {
                binding.setIsLoading(!isRunning);
                binding.progressBar.enableIndeterminateMode(!isRunning);;
            });
            viewModel.laneInfo.observe(this, laneInfo -> {
                if (laneInfo != null) {
                    binding.myDriveWayView.setVisibility(View.VISIBLE);
                    binding.myDriveWayView.buildDriveWay(laneInfo);

                } else {
                    binding.myDriveWayView.hide();
                }
            });
            viewModel.crossImage.observe(this, cross -> {
                if (cross != null) {
                    binding.myZoomInIntersectionView.setIntersectionBitMap(cross);
                    binding.myZoomInIntersectionView.setVisibility(View.VISIBLE);
                } else {
                    binding.myZoomInIntersectionView.setVisibility(View.GONE);
                }
            });
            viewModel.showText.observe(this,
                    text -> Toasty.info(requireContext(), text).show());
            viewModel.isLockCamera.observe(this,
                    isLock -> carMarker.setLock(isLock));
            viewModel.naviLocation.observe(this,
                    naviLocation -> carMarker.draw(Point.box(naviLocation.getCoord()),
                            naviLocation.getBearing()));
            viewModel.cameraInfoSet.observe(this, set -> cameraOverlay.draw(map, set));
            viewModel.modelCrossImage.observe(this, modelCross -> {
                if (modelCross != null) {
                    crossOverlay = map.addCrossOverlay(
                            new CrossOverlayOptions()
                                    .setAttribute(modelCross.attr)
                                    .setRes(modelCross.res));
                    crossOverlay.setData(modelCross.buffer);
                    crossOverlay.setVisible(true);
                } else {
                    if (crossOverlay != null) {
                        crossOverlay.setVisible(false);
                        crossOverlay.remove();
                    }
                }
            });
            getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
                if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                    binding.myZoomInIntersectionView.recycleResource();
                }
            });
            requireActivity().getOnBackPressedDispatcher()
                    .addCallback(this, new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            long now = SystemClock.uptimeMillis();
                            if (now - last > 1000) {
                                Logger.d(now - last);
                                Toasty.warning(requireContext(), "再按一次退出导航")
                                        .show();
                                last = now;
                                return;
                            }
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    });
        }

        private AMap getMap() {
            AMapCompatFragment mapCompatFragment = (AMapCompatFragment)
                    requireFragmentManager().findFragmentById(R.id.map_view);
            if (mapCompatFragment != null) {
                AMap aMap = mapCompatFragment.getMap();
                viewModel.bindMapLocation(aMap);
                return aMap;
            }
            throw new AssertionError();
        }
    }

    public static final class PreviewFragment extends Fragment {

        private NavigatorViewModel viewModel;

        private FragmentNavigatorPreviewBinding binding;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            viewModel = ViewModelProviders.of(requireParentFragment())
                    .get(NavigatorViewModel.class);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_navigator_preview,
                    container,
                    false);
            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            binding.paths.setAdapter(viewModel.paths);
            viewModel.selfLocationName.observe(this, s -> binding.setFormText(s));
            viewModel.isPreview.observe(this, val -> binding.setIsPreview(val));
            binding.setOpenSearch(v -> {
                Fragment fragment = (Fragment) ARouter.getInstance()
                        .build(Module.Navi.search)
                        .navigation();
                Fragment parent = requireParentFragment();
                fragment.setTargetFragment(parent, R.id.search_request_code);
                parent.requireFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .add(parent.getId(), fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commitAllowingStateLoss();
            });
            binding.setOnBack(v -> viewModel.exitPreviewMode());
        }
    }
}