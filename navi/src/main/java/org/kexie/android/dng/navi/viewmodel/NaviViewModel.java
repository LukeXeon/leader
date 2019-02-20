package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;

import com.amap.api.maps.AMap;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;

import org.kexie.android.dng.navi.model.Route;
import org.kexie.android.dng.navi.widget.NavControllerCallbacks;

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

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> loading = new MutableLiveData<>();

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();

    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    private final AMapNavi navigation;

    private AMap mapController;

    public NaviViewModel(@NonNull Application application)
    {
        super(application);
        navigation = AMapNavi.getInstance(application);
    }

    public void initMapController(AMap aMap)
    {
        this.mapController = aMap;
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
                        new NavControllerCallbacks()
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
                                navigation.startNavi(NaviType.EMULATOR);
                                //calculateResult.postValue(true);
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
