package kexie.android.navi.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class RouteQueryViewModel extends AndroidViewModel
{
    public RouteQueryViewModel(@NonNull Application application)
    {
        super(application);
    }

    @Override
    protected void onCleared()
    {
        super.onCleared();
    }
}
