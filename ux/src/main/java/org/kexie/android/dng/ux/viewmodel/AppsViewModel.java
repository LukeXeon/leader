package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.util.ArrayMap;

import org.kexie.android.common.util.Collectors2;
import org.kexie.android.dng.ux.model.AppInfoProvider;
import org.kexie.android.dng.ux.model.entity.AppInfo;
import org.kexie.android.dng.ux.viewmodel.entity.LiteAppInfo;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.AndroidViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.StreamSupport;

public class AppsViewModel extends AndroidViewModel
{
    private final ExecutorService singletTask = Executors.newSingleThreadExecutor();
    private final Map<LiteAppInfo, String> appInfos = new ArrayMap<>();
    private final ObservableArrayList<LiteAppInfo> liteAppInfos = new ObservableArrayList<>();
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
            Map<LiteAppInfo, String> map = StreamSupport.stream(AppInfoProvider.getLaunchApps(getApplication()))
                    .collect(Collectors2.toLinkedHashMap(appInfo -> new LiteAppInfo(appInfo.getName(),
                                    appInfo.getIcon()),
                            AppInfo::getPackageName));
            appInfos.clear();
            appInfos.putAll(map);
            StreamSupport.stream(map.keySet())
                    .forEach(liteAppInfos::add);
        });
    }

    public void requestJumpBy(LiteAppInfo liteAppInfo)
    {
        String packageName = appInfos.get(liteAppInfo);
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

    public ObservableList<LiteAppInfo> getAppInfos()
    {
        return liteAppInfos;
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