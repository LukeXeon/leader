package org.kexie.android.dng.navi.model;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.LBS;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.navi.model.beans.Point;

import java.util.concurrent.atomic.AtomicInteger;

@Route(path = Module.Navi.location)
public class AMapLBS implements LBS,AMapLocationListener {

    private AMapLocationClient locationSource;
    private AtomicInteger refCount = new AtomicInteger(0);

    @Override
    public void init(Context context) {
        locationSource = new AMapLocationClient(context.getApplicationContext());
        locationSource.setLocationListener(this);
    }

    @Override
    public Session use() {
        return new SessionImpl();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    private final class SessionImpl implements Session {
        boolean isClose = false;

        SessionImpl() {
            if (refCount.getAndIncrement() == 0) {
                AMapLocationClientOption option = new AMapLocationClientOption();
                option.setInterval(1000);
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                option.setNeedAddress(false);
                locationSource.setLocationOption(option);
                locationSource.stopLocation();
                locationSource.startLocation();
//                Logger.d("start lbs");
            }
        }

        @Override
        public IPoint lastLocation() throws InterruptedException {
            AMapLocation location;
            while ((location = locationSource.getLastKnownLocation()) == null) {
                Logger.d("wait location");
                Thread.sleep(100);
            }
            return Point.form(location.getLongitude(), location.getLatitude());
        }

        @Override
        public void close() {
            if (!isClose) {
                isClose = true;
                if (refCount.decrementAndGet() == 0) {
  //                  Logger.d("stop lbs");
                    locationSource.stopLocation();
                }
            }
        }
    }
}