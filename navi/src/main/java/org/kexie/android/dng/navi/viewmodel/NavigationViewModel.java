package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;

import org.kexie.android.dng.navi.model.Route;
import org.kexie.android.dng.navi.util.NavigationCallbacks;

import java.util.Collections;
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

public class NavigationViewModel extends AndroidViewModel
{
    private final AMapNavi navigation;
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> calculateResult = new MutableLiveData<>();
    private final MutableLiveData<String> loading = new MutableLiveData<>();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    public NavigationViewModel(@NonNull Application application)
    {
        super(application);
        navigation = AMapNavi.getInstance(application);
    }

    public void calculate(Route route)
    {
        loading.setValue("加载中");
        singleTask.execute(() -> {
            try
            {
                final Lock lock = new ReentrantLock();
                final Condition condition = lock.newCondition();
                navigation.addAMapNaviListener(
                        new NavigationCallbacks()
                        {
                            public void onCalculateRouteFailure(int code)
                            {
                                lock.lock();
                                navigation.removeAMapNaviListener(this);
                                calculateResult.postValue(false);
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
                                calculateResult.postValue(true);
                                condition.signalAll();
                                lock.unlock();
                            }
                        });
                lock.lock();
                navigation.calculateDriveRoute(Collections.singletonList(
                        route.getFrom().unBox(NaviLatLng.class)),
                        Collections.singletonList(route.getTo().unBox(NaviLatLng.class)),
                        StreamSupport.stream(route.getWays())
                                .map(p -> p.unBox(NaviLatLng.class))
                                .collect(Collectors.toList()), 9);
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
