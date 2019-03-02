package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;

import com.amap.api.navi.AMapNavi;

import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class NaviViewModel extends AndroidViewModel
{
    private final AMapNavi navi;

    public final MutableLiveData<Map<Integer, RouteInfo>> routeInfos = new MutableLiveData<>();

    public NaviViewModel(@NonNull Application application)
    {
        super(application);

        this.navi = AMapNavi.getInstance(application);
    }


    @Override
    protected void onCleared()
    {
        navi.destroy();
    }
}
