package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.NaviPath;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiSearch;

import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.model.Query;
import org.kexie.android.dng.navi.viewmodel.entity.GuideInfo;
import org.kexie.android.dng.navi.viewmodel.entity.LiteRouteInfo;
import org.kexie.android.dng.navi.viewmodel.entity.LiteTip;
import org.kexie.android.dng.navi.widget.NaviCallbacks;
import org.kexie.android.dng.navi.widget.NaviCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.Collectors;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import mapper.Request;

public class NaviViewModel extends AndroidViewModel implements OnBackPressedCallback
{

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Request>> routes = new MutableLiveData<>();

    private final PublishSubject<String> onLoading = PublishSubject.create();

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();

    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    private final AMapNavi navigation;

    private final AMapLocationClient locationClient;

    public NaviViewModel(@NonNull Application application)
    {
        super(application);
        navigation = AMapNavi.getInstance(application);
        locationClient = new AMapLocationClient(getApplication());
        locationClient.startLocation();
    }

    public LiveData<List<Request>> getRoutes()
    {
        return routes;
    }

    private void setRoute(int[] paths)
    {
        if (paths == null)
        {
            routes.setValue(null);
            return;
        }
        List<Request> requests = IntStreams.of(paths)
                .boxed()
                .map(id -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("pathId", id);
                    return new Request.Builder()
                            .bundle(bundle)
                            .uri("dng/navi/route")
                            .build();
                }).collect(Collectors.toList());
        routes.setValue(requests);
    }

    public void startNavi()
    {
        navigation.startNavi(NaviType.EMULATOR);
    }

    public void query(LiteTip tip)
    {
        onLoading.onNext("加载中");
        singleTask.execute(() -> {
            Executor executor = Executors.newCachedThreadPool();
            List<FutureTask<Point>> tasks = Arrays.asList(
                    new FutureTask<>(this::loadLocation),
                    new FutureTask<>(() -> loadTarget(tip)));
            for (FutureTask<Point> task : tasks)
            {
                executor.execute(task);
            }
            List<Point> points = new LinkedList<>();
            for (FutureTask<Point> task : tasks)
            {
                try
                {
                    points.add(task.get());
                } catch (Exception e)
                {
                    e.printStackTrace();
                    onErrorMessage.onNext("获取位置信息失败");
                    onLoading.onNext("");
                    return;
                }
            }
            Query query = new Query.Builder()
                    .from(points.get(0))
                    .to(points.get(1))
                    .build();
            loadRoute(query);
            onLoading.onNext("");
        });
    }

    @WorkerThread
    private Point loadLocation()
    {
        try
        {
            AMapLocation location = locationClient.getLastKnownLocation();
            if (location != null)
            {
                return Point.form(location.getLongitude(), location.getLatitude());
            }
            Lock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            locationClient.setLocationListener(
                    new AMapLocationListener()
                    {
                        @Override
                        public void onLocationChanged(AMapLocation aMapLocation)
                        {
                            lock.lock();
                            locationClient.unRegisterLocationListener(this);
                            condition.signalAll();
                            lock.unlock();
                        }
                    });
            lock.lock();
            condition.await();
            lock.unlock();
            location = locationClient.getLastKnownLocation();
            return Point.form(location.getLongitude(), location.getLatitude());
        } catch (Exception e)
        {
            e.printStackTrace();
            onErrorMessage.onNext("获取位置信息失败");
            return null;
        }
    }

    @WorkerThread
    private void loadRoute(Query query)
    {
        try
        {
            Lock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            navigation.addAMapNaviListener(
                    new NaviCallbacks()
                    {
                        public void onCalculateRouteFailure(int code)
                        {
                            lock.lock();
                            navigation.removeAMapNaviListener(this);
                            setRoute(null);
                            onErrorMessage.onNext("路径规划失败,请检查网络连接");
                            condition.signalAll();
                            lock.unlock();
                        }

                        @Override
                        public void onCalculateRouteSuccess(int[] ints)
                        {
                            lock.lock();
                            navigation.removeAMapNaviListener(this);
                            setRoute(ints);
                            onSuccessMessage.onNext("路径规划成功");
                            condition.signalAll();
                            lock.unlock();
                        }
                    });
            lock.lock();

            List<NaviLatLng> form = query.from == null
                    ? Collections.emptyList()
                    : Collections.singletonList(query.from
                    .unBox(NaviLatLng.class));

            List<NaviLatLng> to = query.to == null
                    ? Collections.emptyList()
                    : Collections.singletonList(query.to
                    .unBox(NaviLatLng.class));

            List<NaviLatLng> ways = query.ways == null
                    || query.ways.size() == 0
                    ? Collections.emptyList()
                    : StreamSupport.stream(query.ways)
                    .map(p -> p.unBox(NaviLatLng.class))
                    .collect(Collectors.toList());

            navigation.calculateDriveRoute(form, to, ways, 10);
            condition.await();
            lock.unlock();
        } catch (Exception e)
        {
            e.printStackTrace();
            onErrorMessage.onNext("路径规划失败,请检查网络连接");
        }
    }

    @WorkerThread
    private Point loadTarget(LiteTip tip)
    {
        PoiSearch.Query query = new PoiSearch.Query(tip.text, "");
        query.setDistanceSort(false);
        query.requireSubPois(true);
        PoiSearch poiSearch = new PoiSearch(getApplication(), query);
        try
        {
            PoiItem item = poiSearch.searchPOIId(tip.poiId);
            LatLonPoint latLonPoint
                    = ((latLonPoint = item.getEnter()) != null)
                    ? (latLonPoint)
                    : ((latLonPoint = item.getExit()) != null
                    ? latLonPoint
                    : item.getLatLonPoint());
            return Point.box(latLonPoint);
        } catch (AMapException e)
        {
            onErrorMessage.onNext("目标地点信息读取失败,请检查网络连接");
            e.printStackTrace();
            return null;
        }
    }

    public AMapNaviPath getPath(int id)
    {
        return Objects.requireNonNull(NaviCompat.getNaviPath(navigation).get(id))
                .amapNaviPath;
    }

    public List<GuideInfo> getGuideInfo(int id)
    {
        return getGuideInfo(NaviCompat.getNaviPath(navigation).get(id));
    }

    public LiteRouteInfo getRouteInfo(int id)
    {
        NaviPath path = NaviCompat.getNaviPath(navigation).get(id);
        return new LiteRouteInfo.Builder()
                .length(getPathLength(path.getAllLength()))
                .time(getPathTime(path.getAllTime()))
                .name(path.amapNaviPath.getLabels())
                .build();
    }

    private static List<GuideInfo> getGuideInfo(NaviPath naviPath)
    {
        List<GuideInfo> steps = new ArrayList<>();
        try
        {
            List<AMapNaviGuide> aMapNaviGuides = naviPath.getGuideList();
            AMapNaviPath path = naviPath.amapNaviPath;
            List<AMapNaviStep> aMapNaviSteps = path.getSteps();

            for (int j = 0; j < aMapNaviGuides.size(); j++)
            {
                AMapNaviGuide g = aMapNaviGuides.get(j);
                GuideInfo group = new GuideInfo();
                group.setGroupIconType(g.getIconType());
                group.setGroupLen(g.getLength());
                group.setGroupName(g.getName());
                group.setGroupToll(g.getToll());
                int count = g.getSegCount();
                int startSeg = g.getStartSegId();
                int traffics = 0;
                for (int i = startSeg; i < count + startSeg; i++)
                {
                    AMapNaviStep step = aMapNaviSteps.get(i);
                    traffics += step.getTrafficLightNumber();
                    String roadName = "";
                    if (i == (count + startSeg - 1) && j == aMapNaviGuides.size() - 1)
                    {
                        roadName = "终点";
                    } else if (i == (count + startSeg - 1) && j + 1 < aMapNaviGuides.size() - 1)
                    {
                        AMapNaviGuide ag = aMapNaviGuides.get(j + 1);
                        roadName = ag.getName();
                    } else
                    {
                        roadName = step.getLinks().get(0).getRoadName();
                    }

                    GuideInfo.Step lbsGuidStep = new GuideInfo.Step(step.getIconType(),
                            roadName, step.getLength());
                    group.getSteps().add(lbsGuidStep);
                }
                group.setGroupTrafficLights(traffics);
                steps.add(group);
            }
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return steps;
    }

    private static String getPathTime(int time)
    {
        if ((long) time > 3600)
        {
            long hour = (long) time / 3600;
            long miniate = ((long) time % 3600) / 60;
            return hour + "小时" + miniate + "分钟";
        }
        if ((long) time >= 60)
        {
            long miniate = (long) time / 60;
            return miniate + "分钟";
        }
        return (long) time + "秒";
    }

    private static String getPathLength(int path)
    {
        if (path > 10000) // 10 km
        {
            float dis = path / 1000;
            return dis + "千米";
        }
        if (path > 1000)
        {
            float dis = (float) path / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + "千米";
        }
        if (path > 100)
        {
            float dis = path / 50 * 50;
            return dis + "米";
        }
        float dis = path / 10 * 10;
        if (dis == 0)
        {
            dis = 10;
        }
        return dis + "米";
    }

    public Observable<String> getOnLoading()
    {
        return onLoading.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage.observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onCleared()
    {
        navigation.destroy();
        locationClient.stopLocation();
    }

    public LatLngBounds getBounds(int id)
    {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        StreamSupport.stream(getPath(id).getCoordList())
                .map(Point::box)
                .map(x -> x.unBox(LatLng.class))
                .forEach(builder::include);
        LatLngBounds bounds = builder.build();
        bounds = LatLngBounds.builder()
                .include(Point.box(bounds.northeast).add(Point.form(0.4, 0.1)).unBox(LatLng.class))
                .include(Point.box(bounds.southwest).add(Point.form(-0.4, -0.05)).unBox(LatLng.class))
                .build();
        return bounds;
    }

    @Override
    public boolean handleOnBackPressed()
    {
        if (routes.getValue() != null)
        {
            routes.setValue(null);
            return true;
        }
        return false;
    }
}
