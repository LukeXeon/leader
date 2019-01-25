package kexie.android.navi.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.amap.api.navi.AMapNavi;

import java.util.List;

import kexie.android.navi.entity.Point;

public class MapNavigationViewModel extends AndroidViewModel
{
    private final AMapNavi navigation;

    public MapNavigationViewModel(@NonNull Application application)
    {
        super(application);
        navigation = AMapNavi.getInstance(application);
    }

    public void begin(List<Point> points)
    {

    }

    @Override
    protected void onCleared()
    {
        navigation.destroy();
    }
}
