package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.ux.model.AppInfoProvider;
import org.kexie.android.dng.ux.viewmodel.entity.LiteAppInfo;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.AndroidViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.StreamSupport;

public class AppsViewModel extends AndroidViewModel
{
    private final ExecutorService singletTask = Executors.newSingleThreadExecutor();
    private final Map<LiteAppInfo, String> appInfos = new ArrayMap<>();
    private GenericQuickAdapter<LiteAppInfo> adapter;
    private final PublishSubject<Intent> onJumpTo = PublishSubject.create();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();
    private Future task;

    public AppsViewModel(@NonNull Application application)
    {
        super(application);
    }

    public void setAdapter(GenericQuickAdapter<LiteAppInfo> adapter)
    {
        this.adapter = adapter;
    }

    public void loadAppInfo()
    {
        if (task != null && !task.isCancelled())
        {
            task.cancel(true);
        }
        task = singletTask.submit(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            appInfos.clear();
            handler.post(() -> {
                adapter.getData().clear();
                adapter.notifyDataSetChanged();
            });
            StreamSupport.stream(AppInfoProvider.getLaunchApps(getApplication()))
                    .forEach(x -> {
                        LiteAppInfo appInfo = new LiteAppInfo(x.getName(), x.getIcon());
                        appInfos.put(appInfo, x.getPackageName());
                        handler.post(() -> adapter.addData(appInfo));
                    });
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

    @Override
    protected void onCleared()
    {
        if (task != null && !task.isCancelled())
        {
            task.cancel(true);
        }
    }
}