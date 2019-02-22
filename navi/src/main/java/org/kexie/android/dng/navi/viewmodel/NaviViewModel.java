package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;

import com.amap.api.col.n3.ik;
import com.amap.api.col.n3.ip;
import com.amap.api.col.n3.ir;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.navi.model.Route;
import org.kexie.android.dng.navi.widget.NaviCallbacks;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class NaviViewModel extends AndroidViewModel
{

    private final static Field sInnerNavi;

    private final static Field sNaviImpl;

    private final static Field sNaviPath;

    private final static Field sNaviPathManager;

    private final static Field sAllNaviPath;

    private final static Field sWayCount;

    static
    {
        try
        {
            //get inner -> get ir
            sInnerNavi = AMapNavi.class.getDeclaredField("mINavi");
            //get impl -> get ik
            sNaviImpl = ir.class.getDeclaredField("m");
            //get path -> get NaviPath
            sNaviPath = ik.class.getDeclaredField("c");
            //get allPathManager -> get ip
            sNaviPathManager = ik.class.getDeclaredField("b");
            //get allPath -> get Map<int,NaviPath>
            sAllNaviPath = ip.class.getDeclaredField("h");

            sWayCount = ik.class.getDeclaredField("h");


            sInnerNavi.setAccessible(true);
            sNaviImpl.setAccessible(true);
            sNaviPath.setAccessible(true);
            sNaviPathManager.setAccessible(true);
            sAllNaviPath.setAccessible(true);
            sWayCount.setAccessible(true);
        } catch (Exception e)
        {
            throw new RuntimeException("compat load failed", e);
        }
    }

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> loading = new MutableLiveData<>();

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();

    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    private final AMapNavi navigation;

    public NaviViewModel(@NonNull Application application)
    {
        super(application);
        navigation = AMapNavi.getInstance(application);
    }

    public void beginBy(Route route)
    {
        loading.setValue("加载中");
        singleTask.execute(() -> {
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
                                //calculateResult.postValue(false);
                                condition.signalAll();
                                lock.unlock();
                            }

                            @Override
                            public void onCalculateRouteSuccess(int[] ints)
                            {
                                lock.lock();
                                navigation.removeAMapNaviListener(this);
                                navigation.selectRouteId(ints[0]);
                                Logger.d(ints.length);
                                navigation.startNavi(NaviType.EMULATOR);
                                condition.signalAll();
                                lock.unlock();
                            }
                        });

                lock.lock();

                List<NaviLatLng> form = route.getFrom() == null
                        ? Collections.emptyList()
                        : Collections.singletonList(route.getFrom()
                        .unBox(NaviLatLng.class));

                List<NaviLatLng> to = route.getTo() == null
                        ? Collections.emptyList()
                        : Collections.singletonList(route.getTo()
                        .unBox(NaviLatLng.class));

                List<NaviLatLng> ways = route.getWays() == null
                        || route.getWays().size() == 0
                        ? Collections.emptyList()
                        : StreamSupport.stream(route.getWays())
                        .map(p -> p.unBox(NaviLatLng.class))
                        .collect(Collectors.toList());

                navigation.calculateDriveRoute(form, to, ways, 9);
                condition.await();
                lock.unlock();
                loading.postValue(null);
            } catch (Exception e)
            {
                throw new AssertionError(e);
            }
        });
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage;
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage;
    }

    @Override
    protected void onCleared()
    {
        navigation.destroy();
    }
}
