package kexie.android.navi.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import kexie.android.navi.entity.Route;

public class RouteQueryViewModel extends AndroidViewModel
{

    private final MutableLiveData<List<Route>> routes = new MutableLiveData<>();

    public RouteQueryViewModel(@NonNull Application application)
    {
        super(application);
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
