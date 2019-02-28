package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.Context;

import org.kexie.android.dng.ux.model.AppInfoProvider;
import org.kexie.android.dng.ux.viewmodel.entity.App;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class AppsViewModel extends AndroidViewModel
{
    public final MutableLiveData<List<App>> apps = new MutableLiveData<>();

    public AppsViewModel(@NonNull Application application)
    {
        super(application);
        loadAppInfo();
    }

    private void loadAppInfo()
    {
        Observable.<Context>just(getApplication())
                .observeOn(Schedulers.io())
                .map(context -> StreamSupport.stream(AppInfoProvider.getLaunchApps(getApplication()))
                        .map(x -> new App(x.getName(), x.getIcon(), x.getPackageName()))
                        .collect(Collectors.toList()))
                .subscribe(new Observer<List<App>>()
                {

                    @Override
                    public void onSubscribe(Disposable d)
                    {
                    }

                    @Override
                    public void onNext(List<App> apps)
                    {
                        AppsViewModel.this.apps.postValue(apps);
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onComplete()
                    {

                    }
                });
    }
}