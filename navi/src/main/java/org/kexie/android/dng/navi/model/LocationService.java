package org.kexie.android.dng.navi.model;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import org.kexie.android.dng.common.contract.LBS;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.navi.model.beans.Point;

@Route(path = Module.Navi.location)
public class LocationService implements LBS {

    private AMapLocationClient locationSource;
    private int refCount;

    @Override
    public void init(Context context) {
        locationSource = new AMapLocationClient(context.getApplicationContext());
        locationSource.stopLocation();
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setInterval(1000);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setNeedAddress(false);
        locationSource.setLocationOption(option);
    }

    @Override
    public Session use() {
        return new SessionImpl();
    }

    private final class SessionImpl implements Session {
        boolean isClose = false;

        SessionImpl() {
            if (refCount == 0) {
                locationSource.startLocation();
            }
            refCount++;
        }

        @Override
        public IPoint lastLocation() throws InterruptedException {
            if (locationSource.isStarted()) {
                throw new AssertionError();
            }
            AMapLocation location;
            while ((location = locationSource.getLastKnownLocation()) == null) {
                Thread.sleep(100);
            }
            return Point.form(location.getLongitude(), location.getLatitude());
        }

        @Override
        public void close() {
            if (!isClose) {
                isClose = true;
                refCount--;
                if (refCount == 0) {
                    locationSource.stopLocation();
                }
            }
        }
    }
}