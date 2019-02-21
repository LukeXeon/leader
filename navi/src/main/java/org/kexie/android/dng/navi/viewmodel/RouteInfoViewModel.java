package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.bumptech.glide.Glide;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.viewmodel.entity.LiteRoute;
import org.kexie.android.dng.navi.viewmodel.entity.LiteStep;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class RouteInfoViewModel extends AndroidViewModel
{
    private final MutableLiveData<LiteRoute> routeInfo = new MutableLiveData<>();

    public void loadInfo(Bundle bundle)
    {
        DrivePath path = bundle.getParcelable("path");
        new Thread()
        {
            @Override
            public void run()
            {
                LiteRoute liteRoute = new LiteRoute.Builder()
                        .name(getPathName(path))
                        .length(getPathLength(path))
                        .time(getPathTime(path))
                        .steps(getPathStep(path))
                        .build();
                routeInfo.postValue(liteRoute);
            }
        }.start();
    }

    public LiveData<LiteRoute> getRouteInfo()
    {
        return routeInfo;
    }

    public RouteInfoViewModel(@NonNull Application application)
    {
        super(application);
    }

    @WorkerThread
    private List<LiteStep> getPathStep(DrivePath path)
    {
        return StreamSupport.stream(path.getSteps())
                .map(DriveStep::getAction)
                .map(a -> {
                    Drawable drawable = null;
                    try
                    {
                        drawable = Glide.with(getApplication())
                                .load(getActionRes(a))
                                .submit()
                                .get();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return new LiteStep(a, drawable);
                }).collect(Collectors.toList());
    }

    private static int getActionRes(String actionName)
    {
        if (actionName == null || actionName.equals(""))
        {
            return R.mipmap.dir3;
        }
        if ("左转".equals(actionName))
        {
            return R.mipmap.dir2;
        }
        if ("右转".equals(actionName))
        {
            return R.mipmap.dir1;
        }
        if ("向左前方行驶".equals(actionName) || "靠左".equals(actionName))
        {
            return R.mipmap.dir6;
        }
        if ("向右前方行驶".equals(actionName) || "靠右".equals(actionName))
        {
            return R.mipmap.dir5;
        }
        if ("向左后方行驶".equals(actionName) || "左转调头".equals(actionName))
        {
            return R.mipmap.dir7;
        }
        if ("向右后方行驶".equals(actionName))
        {
            return R.mipmap.dir8;
        }
        if ("直行".equals(actionName))
        {
            return R.mipmap.dir3;
        }
        if ("减速行驶".equals(actionName))
        {
            return R.mipmap.dir4;
        }
        return R.mipmap.dir3;
    }

    private static String getPathTime(DrivePath path)
    {
        long second = path.getDuration();
        if (second > 3600)
        {
            long hour = second / 3600;
            long miniate = (second % 3600) / 60;
            return hour + "小时" + miniate + "分钟";
        }
        if (second >= 60)
        {
            long miniate = second / 60;
            return miniate + "分钟";
        }
        return second + "秒";
    }

    @SuppressWarnings("All")
    private static String getPathLength(DrivePath path)
    {
        int lenMeter = (int) path.getDistance();
        if (lenMeter > 10000) // 10 km
        {
            float dis = lenMeter / 1000;
            return dis + "千米";
        }
        if (lenMeter > 1000)
        {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + "千米";
        }
        if (lenMeter > 100)
        {
            float dis = lenMeter / 50 * 50;
            return dis + "米";
        }
        float dis = lenMeter / 10 * 10;
        if (dis == 0)
        {
            dis = 10;
        }
        return dis + "米";
    }

    private static String getPathName(DrivePath path)
    {
        return path.getStrategy();
    }
}
