package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.model.AppInfoLoader;
import org.kexie.android.dng.ux.model.beans.AppInfo;
import org.kexie.android.dng.ux.viewmodel.beans.App;

import java.util.List;

import java8.util.Objects;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class AppsViewModel extends AndroidViewModel {

    private BroadcastReceiver broadcastReceiver;

    private Handler mainWorker = new Handler(Looper.getMainLooper());

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public final GenericQuickAdapter<App> appAdapter = new GenericQuickAdapter<>(R.layout.item_app, BR.app);

    public AppsViewModel(@NonNull Application application) {
        super(application);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Uri uri = intent.getData();
                if (uri == null) {
                    return;
                }
                String packageName = uri.getSchemeSpecificPart();
                if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                    AppInfo appInfo = AppInfoLoader.getLaunchApp(getApplication(), packageName);
                    if (appInfo != null) {
                        App app = new App(
                                appInfo.getName(),
                                appInfo.getIcon(),
                                appInfo.getPackageName(),
                                appInfo.isSysApp());
                        List<App> apps = appAdapter.getData();
                        apps.add(app);
                        appAdapter.setNewData(StreamSupport.stream(apps)
                                .sorted()
                                .collect(Collectors.toList()));
                    }
                }
                if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                    int index = -1;
                    List<App> apps = appAdapter.getData();
                    for (int i = 0; i < apps.size(); i++) {
                        App app = apps.get(i);
                        if (Objects.equals(packageName, app.packageName)) {
                            index = i;
                        }
                    }
                    if (index > 0) {
                        appAdapter.remove(index);
                    }
                }
            }
        };
        getApplication().registerReceiver(broadcastReceiver, filter);
        load();
    }

    @MainThread
    private void load() {
        isLoading.setValue(true);
        new Thread(() -> {
            List<App> list = StreamSupport.stream(AppInfoLoader
                    .getLaunchApps(getApplication()))
                    .map(info -> new App(
                            info.getName(),
                            info.getIcon(),
                            info.getPackageName(),
                            info.isSysApp()))
                    .sorted()
                    .collect(Collectors.toList());
            mainWorker.post(() -> {
                appAdapter.setNewData(list);
                isLoading.setValue(false);
            });
        }).start();
    }

    @Override
    protected void onCleared() {
        getApplication().unregisterReceiver(broadcastReceiver);
    }
}