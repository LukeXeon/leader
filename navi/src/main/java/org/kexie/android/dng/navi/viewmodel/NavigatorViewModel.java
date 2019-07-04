package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.maps.AMap;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.NaviPath;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.ae.gmap.gloverlay.GLCrossVector;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.contract.TTS;
import org.kexie.android.dng.common.util.LiveEvent;
import org.kexie.android.dng.navi.model.beans.Point;
import org.kexie.android.dng.navi.model.beans.Query;
import org.kexie.android.dng.navi.util.AMapCompat;
import org.kexie.android.dng.navi.util.DensityUtils;
import org.kexie.android.dng.navi.util.NaviUtils;
import org.kexie.android.dng.navi.viewmodel.beans.ModelCrossImage;
import org.kexie.android.dng.navi.viewmodel.beans.NaviDescription;
import org.kexie.android.dng.navi.viewmodel.beans.PathDescription;
import org.kexie.android.dng.navi.viewmodel.beans.StationDescription;
import org.kexie.android.dng.navi.viewmodel.beans.TipText;
import org.kexie.android.dng.navi.widget.NaviCallbacks;
import org.kexie.android.dng.navi.widget.PathAdapter;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class NavigatorViewModel extends AndroidViewModel {

    public static final int NO_SELECT = Integer.MIN_VALUE;

    public final PathAdapter paths;

    public final MutableLiveData<String> selfLocationName = new MutableLiveData<>();

    public final MutableLiveData<Location> selfLocation = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isLockCamera = new MutableLiveData<>(false);

    public final MutableLiveData<AMapNaviCameraInfo[]> cameraInfoSet = new MutableLiveData<>();

    public final MutableLiveData<AMapNaviLocation> naviLocation = new MutableLiveData<>();

    public final MutableLiveData<ModelCrossImage> modelCrossImage = new MutableLiveData<>();

    public final MutableLiveData<AMapNaviCross> crossImage = new MutableLiveData<>();

    public final LiveEvent<Void> onPrepare = new LiveEvent<>();

    public final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);

    public final MutableLiveData<Boolean> isPreview = new MutableLiveData<>(false);

    public final MutableLiveData<Integer> select = new MutableLiveData<>(NO_SELECT);

    public final MutableLiveData<NaviDescription> naviDescription = new MutableLiveData<>();

    public final MutableLiveData<AMapLaneInfo> laneInfo = new MutableLiveData<>();

    public final LiveEvent<String> showText = new LiveEvent<>();

    private TTS tts;

    private AMapNavi navi;

    private HandlerThread workerThread;

    private Handler worker;

    private Handler main;

    public NavigatorViewModel(@NonNull Application application) {
        super(application);
        navi = AMapNavi.getInstance(application);
        navi.addAMapNaviListener(new RunningEventHandler());
        navi.setUseInnerVoice(false);
        workerThread = new HandlerThread("navigator");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        main = new Handler(Looper.getMainLooper());
        tts = (TTS) ARouter.getInstance().build(Module.Ai.tts).navigation(application);
        paths = new PathAdapter(() -> onPrepare.post(null), this::select);
    }

    public void beginNavigation() {
        navi.setEmulatorNaviSpeed(40);
        navi.startNavi(NaviType.EMULATOR);
        isRunning.setValue(true);
        isLockCamera.setValue(true);
    }

    public void exitPreviewMode() {
        isPreview.setValue(false);
        paths.setValue(Collections.emptyList());
    }

    public void bindMapLocation(AMap map) {
        map.setOnMyLocationChangeListener(location -> {
            selfLocation.setValue(location);
            if (selfLocationName.hasActiveObservers()) {
                worker.post(() -> {
                    GeocodeSearch search = new GeocodeSearch(getApplication());
                    RegeocodeQuery query = new RegeocodeQuery(
                            Point.form(location.getLongitude(), location.getLatitude())
                                    .unBox(LatLonPoint.class), 200f, GeocodeSearch.AMAP);
                    try {
                        String name = search.getFromLocation(query).getProvince();
                        selfLocationName.postValue(name);
                    } catch (AMapException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @MainThread
    private void select(int id) {
        select.setValue(id);
        if (id != NO_SELECT) {
            navi.selectRouteId(id);
        }
    }

    public void enterPreviewModeByUser(TipText tip) {
        worker.post(() -> {
            try {
                PoiSearch.Query poiQuery = new PoiSearch.Query(tip.text, "");
                poiQuery.setDistanceSort(false);
                poiQuery.requireSubPois(true);
                PoiSearch poiSearch = new PoiSearch(getApplication(), poiQuery);
                PoiItem poiItem = poiSearch.searchPOIId(tip.id);
                Query query = new Query.Builder()
                        .to(toPoiPoint(poiItem))
                        .mode(10)
                        .build();
                findPaths(query);
                isPreview.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();

            }
        });
    }

    public void enterPreviewModeByRemote(Query query) {
        worker.post(() -> {
            try {
                findPaths(query);
                isPreview.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @WorkerThread
    private void findPaths(Query query) throws Exception {
        int[] ids = requestPathIds(query);
        List<PathDescription> pathDescriptions = new LinkedList<>();
        for (int id : ids) {
            PathDescription description = findPathById(id);
            if (description != null) {
                pathDescriptions.add(description);
            }
        }
        main.post(() -> {
            paths.setValue(pathDescriptions);
            select(ids[0]);
        });
    }

    private int[] requestPathIds(Query query) throws Exception {
        int[][] result = new int[1][];
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        navi.addAMapNaviListener(new NaviCallbacks() {
            @Override
            public void onCalculateRouteFailure(int code) {
                lock.lock();
                navi.removeAMapNaviListener(this);
                condition.signalAll();
                Logger.d("error code " + code);
                lock.unlock();
            }

            @Override
            public void onCalculateRouteSuccess(int[] ints) {
                navi.removeAMapNaviListener(this);
                lock.lock();
                result[0] = ints;
                condition.signalAll();
                lock.unlock();
            }
        });
        lock.lock();
        List<NaviLatLng> to = Collections.singletonList(query.to.unBox(NaviLatLng.class));
        List<NaviLatLng> way = (query.ways == null || query.ways.size() == 0)
                ? Collections.emptyList()
                : StreamSupport.stream(query.ways)
                .map(p -> p.unBox(NaviLatLng.class))
                .collect(Collectors.toList());
        navi.calculateDriveRoute(to, way, query.mode);
        condition.await();
        lock.unlock();
        if (result[0] != null) {
            return result[0];
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected void onCleared() {
        tts.stop();
        navi.destroy();
        workerThread.quit();
        main.removeCallbacksAndMessages(null);
        worker.removeCallbacksAndMessages(null);
    }

    private PathDescription findPathById(int id) {
        NaviPath path = AMapCompat.getAllNaviPath(navi).get(id);
        return path != null ? new PathDescription.Builder()
                .id(id)
                .length(toPathLength(path.getAllLength()))
                .time(toPathTime(path.getAllTime()))
                .name(path.amapNaviPath.getLabels())
                .path(path.amapNaviPath)
                .stationDescriptions(toDescription(path))
                .build() : null;
    }

    private boolean isRunning() {
        Boolean box;
        return (box = isRunning.getValue()) != null && box;
    }

    private static Point toPoiPoint(PoiItem item) {
        LatLonPoint point;
        if (item.getEnter() != null) {
            point = item.getEnter();
        } else if (item.getExit() != null) {
            point = item.getExit();
        } else {
            point = item.getLatLonPoint();
        }
        return Point.box(point);
    }

    private static List<StationDescription> toDescription(NaviPath path) {
        List<StationDescription> steps = new ArrayList<>();
        List<AMapNaviGuide> aMapNaviGuides = path.getGuideList();
        List<AMapNaviStep> aMapNaviSteps = path.getSteps();
        for (int j = 0; j < aMapNaviGuides.size(); j++) {
            AMapNaviGuide g = aMapNaviGuides.get(j);
            StationDescription group = new StationDescription();
            group.setGroupIconType(g.getIconType());
            group.setGroupLen(g.getLength());
            group.setGroupName(g.getName());
            group.setGroupToll(g.getToll());
            int count = g.getSegCount();
            int startSeg = g.getStartSegId();
            int traffics = 0;
            for (int i = startSeg; i < count + startSeg; i++) {
                AMapNaviStep step = aMapNaviSteps.get(i);
                traffics += step.getTrafficLightNumber();
                String roadName;
                if (i == (count + startSeg - 1) && j == aMapNaviGuides.size() - 1) {
                    roadName = "终点";
                } else if (i == (count + startSeg - 1) && j + 1 < aMapNaviGuides.size() - 1) {
                    AMapNaviGuide ag = aMapNaviGuides.get(j + 1);
                    roadName = ag.getName();
                } else {
                    roadName = step.getLinks().get(0).getRoadName();
                }

                StationDescription.Step lbsGuidStep
                        = new StationDescription.Step(step.getIconType(), roadName, step.getLength());
                group.getSteps().add(lbsGuidStep);
            }
            group.setGroupTrafficLights(traffics);
            steps.add(group);
        }
        return steps;
    }

    private static String toPathTime(int time) {
        if (time > 3600) {
            int hour = time / 3600;
            int minute = time % 3600 / 60;
            return hour + "小时" + minute + "分钟";
        }
        if (time >= 60) {
            int minute = time / 60;
            return minute + "分钟";
        }
        return time + "秒";
    }

    private static String toPathLength(int path) {
        if (path > 10000)
        // 10 km
        {
            float dis = (path / 1000f);
            return dis + "千米";
        }
        if (path > 1000) {
            float dis = path / 1000f;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + "千米";
        }
        if (path > 100) {
            float dis = (path / 50f * 50f);
            return dis + "米";
        }
        float dis = (path / 10f * 10f);
        if (dis == 0f) {
            dis = 10f;
        }
        return dis + "米";
    }

    private final class RunningEventHandler extends NaviCallbacks {
        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {
            if (!isRunning()) {
                return;
            }
            if (naviDescription.hasActiveObservers()) {
                int allLength = navi.getNaviPath().getAllLength();

                List<AMapTrafficStatus> trafficStatuses = navi.getTrafficStatuses(0, 0);

                int pathRetainDistance = naviInfo.getPathRetainDistance();

                int iconType = naviInfo.getIconType();

                String nextRoadName = naviInfo.getNextRoadName();

                String nextRoadDistance = NaviUtils.formatKM(naviInfo.getCurStepRetainDistance());

                int curStep = naviInfo.getCurStep();

                NaviDescription description = new NaviDescription(allLength,
                        trafficStatuses,
                        pathRetainDistance,
                        iconType,
                        nextRoadName,
                        nextRoadDistance,
                        curStep);

                naviDescription.setValue(description);
            }
        }

        @Override
        public void hideLaneInfo() {
            if (!isRunning()) {
                return;
            }
            laneInfo.setValue(null);
        }

        @Override
        public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
            if (!isRunning()) {
                return;
            }
            laneInfo.setValue(aMapLaneInfo);
        }

        @Override
        public void showCross(AMapNaviCross aMapNaviCross) {
            if (!isRunning()) {
                return;
            }
            crossImage.setValue(aMapNaviCross);
        }

        @Override
        public void hideCross() {
            if (!isRunning()) {
                return;
            }
            crossImage.setValue(null);
        }

        @Override
        public void showModeCross(AMapModelCross aMapModelCross) {
            if (isRunning()) {
                return;
            }
            if (!modelCrossImage.hasActiveObservers()) {
                return;
            }
            worker.post(() -> {
                GLCrossVector.AVectorCrossAttr attr = new GLCrossVector.AVectorCrossAttr();
                // 设置显示区域
                attr.stAreaRect = new Rect(0, DensityUtils.dp2px(getApplication(), 50f),
                        DensityUtils.getScreenWidth(getApplication()),
                        DensityUtils.dp2px(getApplication(),
                                300f));
                /* 背景颜色 */
                attr.stAreaColor = Color.argb(217, 95, 95, 95);
                /* 箭头边线宽度 */
                attr.fArrowBorderWidth = DensityUtils.dp2px(getApplication(), 22f);
                /* 箭头边线颜色 */
                attr.stArrowBorderColor = Color.argb(0, 0, 50, 20);
                /* 箭头内部宽度 */
                attr.fArrowLineWidth = DensityUtils.dp2px(getApplication(), 18f);
                /* 箭头内部颜色 */
                attr.stArrowLineColor = Color.argb(255, 255, 253, 65);
                attr.dayMode = false;
                /* 箭头内部宽度 */
                attr.fArrowLineWidth = 18;
                /* 箭头内部颜色 */
                attr.stArrowLineColor = Color.argb(255, 255, 253, 65);
                attr.dayMode = true;
                ModelCrossImage image = null;
                try {
                    InputStream inputStream = getApplication()
                            .getAssets()
                            .open("vector3d_arrow_in.png");
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    image = new ModelCrossImage(aMapModelCross.getPicBuf1(), attr, bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                modelCrossImage.postValue(image);
            });
        }

        @Override
        public void hideModeCross() {
            if (!isRunning()) {
                return;
            }
            modelCrossImage.setValue(null);
        }

        @Override
        public void notifyParallelRoad(int code) {
            if (!isRunning()) {
                return;
            }
            switch (code) {
                case 0: {
                    showText.post("当前在主辅路过渡");
                }
                break;
                case 1: {
                    showText.post("当前在主路");
                }
                break;
                case 2: {
                    showText.post("当前在辅路");
                }
                break;
            }
        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
            if (!isRunning()) {
                return;
            }
            naviLocation.setValue(aMapNaviLocation);
        }

        @Override
        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
            if (!isRunning()) {
                return;
            }
            cameraInfoSet.setValue(aMapNaviCameraInfos);
        }

        @Override
        public void onGetNavigationText(String s) {
            if (!TextUtils.isEmpty(s)) {
                tts.send(s);
            }
        }
    }
}