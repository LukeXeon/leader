package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.RouteSearch;

import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.model.Query;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
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
    private final MutableLiveData<String> tips = new MutableLiveData<>();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    public QueryViewModel(@NonNull Application application)
    {
        super(application);
        routeSearch = new RouteSearch(application);
    }

    public void tipQuery(String text)
    {
        singleTask.execute(() -> {
            final InputtipsQuery inputtipsQuery
                    = new InputtipsQuery(text, CITY);
            Inputtips inputtips = new Inputtips(getApplication(), inputtipsQuery);
            try
            {
                List<Tip> result = StreamSupport.stream(inputtips.requestInputtips())
                        .filter(tip -> !TextUtils.isEmpty(tip.getPoiID()))
                        .collect(Collectors.toList());

                //tips.postValue(result);
                //loading.postValue(null);
            } catch (Exception e)
            {
                e.printStackTrace();
                //tips.postValue(null);
                //loading.postValue(null);
            }
        });
    }

    private void routeQuery(Query query)
    {
        singleTask.execute(() -> {

            List<List<LatLonPoint>> avoids = query.avoids == null
                    ? Collections.emptyList()
                    : StreamSupport.stream(query.avoids)
                    .map(points -> StreamSupport.stream(points)
                            .map(Point::toLatLonPoint)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            List<LatLonPoint> ways = query.ways == null
                    ? Collections.emptyList()
                    : StreamSupport.stream(query.ways)
                    .map(Point::toLatLonPoint)
                    .collect(Collectors.toList());

            RouteSearch.DriveRouteQuery driveRouteQuery
                    = new RouteSearch.DriveRouteQuery(
                    new RouteSearch.FromAndTo(
                            query.from.toLatLonPoint(),
                            query.to.toLatLonPoint()),
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


    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage;
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage;
    }
}
