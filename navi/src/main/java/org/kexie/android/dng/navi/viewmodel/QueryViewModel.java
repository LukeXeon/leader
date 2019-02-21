package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;
import com.orhanobut.logger.Logger;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.model.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import mapper.Request;

public class QueryViewModel extends AndroidViewModel
{
    public static final String DEBUG_TEXT = "火车站";

    private static final String CITY = "西安";

    private final RouteSearch routeSearch;

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> queryText = new MutableLiveData<>();

    private final MutableLiveData<List<Request>> routes = new MutableLiveData<>();

    private final PublishSubject<String> onLoading = PublishSubject.create();

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();

    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    private final List<String> poiIds = new ArrayList<>();

    private GenericQuickAdapter<String> adapter;

    public void bindAdapter(GenericQuickAdapter<String> adapter)
    {
        this.adapter = adapter;
    }

    public QueryViewModel(@NonNull Application application)
    {
        super(application);
        routeSearch = new RouteSearch(application);
    }

    public LiveData<List<Request>> getRoutes()
    {
        return routes;
    }

    @MainThread
    public void tipQueryBy(String text)
    {
        singleTask.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                adapter.getData().clear();
                adapter.notifyDataSetChanged();
                poiIds.clear();
            });
            InputtipsQuery inputtipsQuery = new InputtipsQuery(text, CITY);
            Inputtips inputtips = new Inputtips(getApplication(), inputtipsQuery);
            try
            {
                StreamSupport.stream(inputtips.requestInputtips())
                        .filter(tip -> !TextUtils.isEmpty(tip.getPoiID()))
                        .forEach(tip -> handler.post(() -> {
                            adapter.addData(tip.getName());
                            poiIds.add(tip.getPoiID());
                        }));
            } catch (Exception e)
            {
                e.printStackTrace();
                onErrorMessage.onNext("输入提示查询失败,请检查网络连接");
            }
        });
    }

    @MainThread
    public void routeQueryBy(String tip)
    {
        int index = adapter.getData().indexOf(tip);
        String poiId = poiIds.get(index);
        if (poiId != null)
        {
            singleTask.execute(() -> {
                Point point = getTipPoint(tip, poiId);
                Query query = new Query.Builder()
                        .from(Point.form(108.947167,34.309997))
                        .to(point)
                        .build();
                routeQuery(query);
            });
        }
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnLoading()
    {
        return onLoading;
    }

    @WorkerThread
    private void routeQuery(Query query)
    {
        List<List<LatLonPoint>> avoids = query.avoids == null
                ? Collections.emptyList()
                : StreamSupport.stream(query.avoids)
                .map(points -> StreamSupport.stream(points)
                        .map(point -> point.unBox(LatLonPoint.class))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        LatLonPoint form = query.from == null ? null : query.from.unBox(LatLonPoint.class);


        LatLonPoint to = query.to == null ? null : query.to.unBox(LatLonPoint.class);

        List<LatLonPoint> ways = query.ways == null
                ? Collections.emptyList()
                : StreamSupport.stream(query.ways)
                .map(point -> point.unBox(LatLonPoint.class))
                .collect(Collectors.toList());

        RouteSearch.DriveRouteQuery driveRouteQuery
                = new RouteSearch.DriveRouteQuery(
                new RouteSearch.FromAndTo(form, to),
                query.mode,
                ways,
                avoids, "");
        try
        {
            List<Request> requests = StreamSupport.stream(routeSearch
                    .calculateDriveRoute(driveRouteQuery)
                    .getPaths())
                    .map(path -> {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("path", path);
                        bundle.putParcelable("from", query.from);
                        bundle.putParcelable("to", query.to);
                        return new Request.Builder()
                                .bundle(bundle)
                                .uri("dng/navi/route")
                                .build();
                    }).collect(Collectors.toList());
            Logger.d("route size "+requests.size());
            routes.postValue(requests);
        } catch (Exception e)
        {
            e.printStackTrace();
            onErrorMessage.onNext("路径规划失败,请检查网络连接");
            routes.postValue(null);
        }
    }

    @MainThread
    private Point getTipPoint(String tip, String poiId)
    {
        PoiSearch.Query query = new PoiSearch.Query(tip, "");
        query.setDistanceSort(false);
        query.requireSubPois(true);
        PoiSearch poiSearch = new PoiSearch(getApplication(), query);
        try
        {
            PoiItem item = poiSearch.searchPOIId(poiId);
            LatLonPoint latLonPoint
                    = ((latLonPoint = item.getEnter()) != null)
                    ? (latLonPoint)
                    : ((latLonPoint = item.getExit()) != null
                    ? latLonPoint
                    : item.getLatLonPoint());
            return Point.box(latLonPoint);
        } catch (AMapException e)
        {
            onErrorMessage.onNext("目标地点信息读取失败,请检查网络连接");
            e.printStackTrace();
            return null;
        }
    }

}