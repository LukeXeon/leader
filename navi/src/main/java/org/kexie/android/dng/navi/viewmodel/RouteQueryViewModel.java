package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.view.View;

import com.amap.api.services.help.Tip;
import com.amap.api.services.route.RouteSearch;

import org.kexie.android.dng.navi.entity.SdkRoute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class RouteQueryViewModel extends AndroidViewModel
{
    private static final String DEBUG_TEXT = "火车站";
    private static final String CITY = "西安";
    private final RouteSearch routeSearch;
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<SdkRoute>> routes = new MutableLiveData<>();
    private final MutableLiveData<List<Tip>> tips = new MutableLiveData<>();
    private final MutableLiveData<String> queryText = new MutableLiveData<>();
    private final MutableLiveData<String> loading = new MutableLiveData<>();

    public RouteQueryViewModel(@NonNull Application application)
    {
        super(application);
        routeSearch = new RouteSearch(application);
    }

    public MutableLiveData<String> getLoading()
    {
        return loading;
    }

    public MutableLiveData<String> getQueryText()
    {
        return queryText;
    }

    public MutableLiveData<List<Tip>> getTips()
    {
        return tips;
    }

    public MutableLiveData<List<SdkRoute>> getRoutes()
    {
        return routes;
    }

    public Map<String, View.OnClickListener> getActions()
    {
        return new HashMap<String, View.OnClickListener>()
        {
            {
            }
        };
    }

}
