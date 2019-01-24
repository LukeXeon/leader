package kexie.android.navi.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kexie.android.navi.entity.Route;
import kexie.android.navi.entity.Step;

public class RouteQueryViewModel extends AndroidViewModel
{

    private final MutableLiveData<List<Route>> routes = new MutableLiveData<>();

    public RouteQueryViewModel(@NonNull Application application)
    {
        super(application);
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < 3; i++)
        {
            List<Step> steps = new ArrayList<>();
            for (int j = 0; j < 10; j++)
            {
                Step step = new Step("1212", "dir1");
                steps.add(step);
            }
            Route route = new Route("123123",
                    "123123",
                    "131",
                    steps);
            routes.add(route);
        }
        this.routes.setValue(routes);
    }

    public MutableLiveData<List<Route>> getRoutes()
    {
        return routes;
    }

    @Override
    protected void onCleared()
    {
        super.onCleared();
    }
}
