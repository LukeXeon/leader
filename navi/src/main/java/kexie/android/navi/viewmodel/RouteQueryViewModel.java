package kexie.android.navi.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.RouteSearch;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kexie.android.navi.entity.Point;
import kexie.android.navi.entity.Query;
import kexie.android.navi.entity.Route;
import kexie.android.navi.util.Points;

public class RouteQueryViewModel extends AndroidViewModel
{
    private static final String DEBUG_TEXT = "火车站";
    private static final String CITY = "西安";
    private final RouteSearch routeSearch;
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Route>> routes = new MutableLiveData<>();
    private final MutableLiveData<List<Tip>> tips = new MutableLiveData<>();
    private final MutableLiveData<Map<String,View.OnClickListener>> actions = new MutableLiveData<>();
    private final MutableLiveData<String> queryText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    public RouteQueryViewModel(@NonNull Application application)
    {
        super(application);
        routeSearch = new RouteSearch(application);
        initActions();
    }

    private void initActions()
    {
        actions.setValue(new HashMap<String, View.OnClickListener>()
        {
            {
                put("开始查询", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        textQuery(DEBUG_TEXT);
                    }
                });
            }
        });
    }

    public MutableLiveData<Boolean> getLoading()
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

    public MutableLiveData<List<Route>> getRoutes()
    {
        return routes;
    }

    public MutableLiveData<Map<String, View.OnClickListener>> getActions()
    {
        return actions;
    }

    private void textQuery(final String text)
    {
        if (!TextUtils.isEmpty(text))
        {
            loading.setValue(true);
            singleTask.execute(new Runnable()
            {
                @Override
                public void run()
                {

                    final InputtipsQuery inputtipsQuery
                            = new InputtipsQuery(text, CITY);
                    Inputtips inputtips = new Inputtips(getApplication(), inputtipsQuery);
                    try
                    {
                        List<Tip> rawResult = inputtips.requestInputtips();
                        List<Tip> result = new ArrayList<>();
                        int i=0;
                        for (Tip tip : rawResult)
                        {
                            if (!TextUtils.isEmpty(tip.getPoiID()))
                            {
                                result.add(tip);
                                Logger.d(++i + "   " + tip.getName());
                            }
                        }
                        tips.postValue(result);
                        loading.postValue(false);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        tips.postValue(null);
                        loading.postValue(false);
                    }
                }
            });
        }
    }

    private void routeQuery(final Query query)
    {
        List<List<LatLonPoint>> lists = new ArrayList<>();
        if (query.avoids != null)
        {
            for (List<Point> points : query.avoids)
            {
                lists.add(Points.toLatLatLonPoints(points));
            }
        }
        final RouteSearch.DriveRouteQuery driveRouteQuery
                = new RouteSearch.DriveRouteQuery(
                new RouteSearch.FromAndTo(query.from.toLatLonPoint(),
                        query.to.toLatLonPoint()),
                query.mode,
                Points.toLatLatLonPoints(query.ways),
                lists, "");
        singleTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final List<Route> routes = new ArrayList<>();
                    for (DrivePath path : routeSearch
                            .calculateDriveRoute(driveRouteQuery)
                            .getPaths())
                    {
                        routes.add(new Route.Builder()
                                .from(query.from)
                                .to(query.to)
                                .path(path)
                                .build());
                    }
                    RouteQueryViewModel.this.routes.setValue(routes);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    RouteQueryViewModel.this.routes.setValue(null);
                }
            }
        });
    }

    @Override
    protected void onCleared()
    {
        System.gc();
    }
}
