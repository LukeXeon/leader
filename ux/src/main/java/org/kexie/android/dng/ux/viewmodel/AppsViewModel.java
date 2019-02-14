package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.Intent;

import org.kexie.android.dng.ux.model.AppInfoProvider;
import org.kexie.android.dng.ux.model.entity.AppInfo;
import org.kexie.android.dng.ux.viewmodel.entity.LiteAppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class AppsViewModel extends AndroidViewModel
{
    private final ExecutorService singletTask = Executors.newSingleThreadExecutor();
    private final Map<LiteAppInfo, String> packageNames = new ConcurrentHashMap<>();
    private final MutableLiveData<List<LiteAppInfo>> appInfo = new MutableLiveData<>();
    private final PublishSubject<Intent> onJumpTo = PublishSubject.create();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();
    private Future task;

    public AppsViewModel(@NonNull Application application)
    {
        super(application);
    }

    public void loadAppInfo()
    {
        if (task != null && !task.isCancelled())
        {
            task.cancel(true);
        }
        task = singletTask.submit(() -> {
            packageNames.clear();
            packageNames.putAll(StreamSupport
                    .stream(AppInfoProvider.getLaunchApps(getApplication()))
                    .collect(Collectors.toMap(
                            appInfo -> new LiteAppInfo(appInfo.getName(), appInfo.getIcon()),
                            AppInfo::getPackageName)));
            appInfo.postValue(new ArrayList<>(packageNames.keySet()));
        });
    }

    public void requestJumpBy(LiteAppInfo liteAppInfo)
    {
        String packageName = packageNames.get(liteAppInfo);
        if (packageName != null)
        {
            Intent intent = getApplication()
                    .getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            if (intent != null)
            {
                onJumpTo.onNext(intent);
                onSuccessMessage.onNext("正在打开");
                return;
            }
        }
        onErrorMessage.onNext(String.format("%s跳转失败", liteAppInfo.name));
    }

    public Observable<Intent> getOnJumpTo()
    {
        return onJumpTo;
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage;
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage;
    }

    public LiveData<List<LiteAppInfo>> getAppInfo()
    {
        return appInfo;
    }

    @Override
    protected void onCleared()
    {
        if (task != null && !task.isCancelled())
        {
            task.cancel(true);
        }
    }
}