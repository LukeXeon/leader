package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;

import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.model.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class QueryViewModel extends AndroidViewModel
{
    private static final String DEBUG_TEXT = "火车站";
    private static final String CITY = "西安";
    private final RouteSearch routeSearch;
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> queryText = new MutableLiveData<>();
    private final MutableLiveData<List<String>> tipText = new MutableLiveData<>();
    private final IdentityHashMap<String, String> tipPoiId = new IdentityHashMap<>();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    public QueryViewModel(@NonNull Application application)
    {
        super(application);
        routeSearch = new RouteSearch(application);
    }

    @MainThread
    public void tipQuery(String text)
    {
        singleTask.execute(() -> {
            InputtipsQuery inputtipsQuery = new InputtipsQuery(text, CITY);
            Inputtips inputtips = new Inputtips(getApplication(), inputtipsQuery);
            try
            {
                Map<String, String> rawResult = StreamSupport.stream(inputtips.requestInputtips())
                        .filter(tip -> !TextUtils.isEmpty(tip.getPoiID()))
                        .collect(Collectors.toMap(Tip::getName, Tip::getPoiID));
                tipPoiId.clear();
                tipPoiId.putAll(rawResult);
                tipText.postValue(new ArrayList<>(rawResult.keySet()));
                //tipText.postValue(result);
                //loading.postValue(null);
            } catch (Exception e)
            {
                e.printStackTrace();
                //tipText.postValue(null);
                //loading.postValue(null);
            }
        });
    }

    @MainThread
    private void routeQuery(Query query)
    {
        singleTask.execute(() -> {

            List<List<LatLonPoint>> avoids = query.avoids == null
                    ? Collections.emptyList()
                    : StreamSupport.stream(query.avoids)
                    .map(points -> StreamSupport.stream(points)
                            .map(point -> point.unBox(LatLonPoint.class))
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            List<LatLonPoint> ways = query.ways == null
                    ? Collections.emptyList()
                    : StreamSupport.stream(query.ways)
                    .map(point -> point.unBox(LatLonPoint.class))
                    .collect(Collectors.toList());

            RouteSearch.DriveRouteQuery driveRouteQuery
                    = new RouteSearch.DriveRouteQuery(
                    new RouteSearch.FromAndTo(
                            query.from.unBox(LatLonPoint.class),
                            query.to.unBox(LatLonPoint.class)),
                    query.mode,
                    ways,
                    avoids, "");
            try
            {
                StreamSupport.stream(routeSearch
                        .calculateDriveRoute(driveRouteQuery)
                        .getPaths());
                //RouteQueryViewModel.this.routes.setValue(routes);
            } catch (Exception e)
            {
                e.printStackTrace();
                //RouteQueryViewModel.this.routes.setValue(null);
            }
        });
    }

    @WorkerThread
    private Point convertTipToPoint(String name, String poiId)
    {
        PoiSearch.Query query = new PoiSearch.Query(name, "");
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
            e.printStackTrace();
            return null;
        }
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage;
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage;
    }
}