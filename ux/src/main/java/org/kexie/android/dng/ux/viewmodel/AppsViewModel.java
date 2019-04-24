package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.Context;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.model.AppInfoProvider;
import org.kexie.android.dng.ux.viewmodel.entity.App;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class AppsViewModel extends AndroidViewModel {

    private Disposable disposable;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public final GenericQuickAdapter<App> appAdapter = new GenericQuickAdapter<>(R.layout.item_app, BR.app);

    public AppsViewModel(@NonNull Application application) {
        super(application);
        load();
    }

    @MainThread
    private void load() {
        isLoading.setValue(true);
        disposable = Observable.<Context>just(this.getApplication())
                .observeOn(Schedulers.io())
                .map(context -> {
                    return StreamSupport.stream(AppInfoProvider.getLaunchApps(context))
                            .map(info -> new App(
                                    info.getName(),
                                    info.getIcon(),
                                    info.getPackageName()))
                            .collect(Collectors.toList());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apps -> {
                    appAdapter.setNewData(apps);
                    isLoading.setValue(false);
                });
    }

    @Override
    protected void onCleared() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}