package kexie.android.navi.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.enums.NaviType;
import com.orhanobut.logger.Logger;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kexie.android.navi.entity.Route;
import kexie.android.navi.util.Points;
import kexie.android.navi.widget.MapNavigationClient;

public class MapNavigationViewModel extends AndroidViewModel
{
    private final AMapNavi navigation;
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> calculateResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public MapNavigationViewModel(@NonNull Application application)
    {
        super(application);
        navigation = AMapNavi.getInstance(application);
    }

    public void calculate(final Route route)
    {
        isLoading.setValue(true);
        singleTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final Lock lock = new ReentrantLock();
                    final Condition condition = lock.newCondition();
                    navigation.addAMapNaviListener(
                            new MapNavigationClient()
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
                    Logger.d(Thread.currentThread());
                    navigation.calculateDriveRoute(Collections.singletonList(
                            route.getFrom().toNaviLatLng()),
                            Collections.singletonList(route.getTo().toNaviLatLng()),
                            Points.toNaviLatLngs(route.getPoints()), 9);
                    condition.await();
                    lock.unlock();
                    isLoading.postValue(false);
                } catch (Exception e)
                {
                    throw new AssertionError(e);
                }
            }
        });
    }

    public MutableLiveData<Boolean> getCalculateResult()
    {
        return calculateResult;
    }

    public MutableLiveData<Boolean> getIsLoading()
    {
        return isLoading;
    }

    @Override
    protected void onCleared()
    {
        navigation.destroy();
    }
}
