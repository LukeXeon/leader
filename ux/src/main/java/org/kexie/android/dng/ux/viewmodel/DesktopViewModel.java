package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.logger.Logger;

import org.kexie.android.common.util.ZoomTransformation;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.model.entity.FunctionInfo;
import org.kexie.android.dng.ux.viewmodel.entity.Function;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java8.util.Objects;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;


public class DesktopViewModel
        extends AndroidViewModel
        implements LifecycleObserver
{

    private static final int BORDER_SIZE = 250;

    public final MutableLiveData<String> time = new MutableLiveData<>();

    public final MutableLiveData<List<Function>> functions = new MutableLiveData<>();

    public final PublishSubject<String> onError = PublishSubject.create();

    public final PublishSubject<String> onSuccess = PublishSubject.create();

    private Timer updateTimer;

    public DesktopViewModel(Application application)
    {
        super(application);
        loadDefaultFunctions();
    }

    private Observable<List<Function>>
    loadFunction(List<FunctionInfo> functionRes)
    {
        return Observable.just(functionRes)
                .observeOn(Schedulers.io())
                .map(raw -> {
                    ZoomTransformation zoomTransformation
                            = new ZoomTransformation(BORDER_SIZE);
                    return StreamSupport.stream(raw)
                            .map(info -> Pair.create(info,
                                    Glide.with(this.getApplication())
                                            .load(info.iconRes)
                                            .apply(RequestOptions.bitmapTransform(zoomTransformation))
                                            .submit()))
                            .map(x -> {
                                try
                                {
                                    assert x.first != null;
                                    assert x.second != null;
                                    Logger.d(x.first.name);
                                    return new Function(x.first.name, x.second.get(), x.first.uri);
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                    return null;
                                }
                            }).filter(Objects::nonNull)
                            .collect(Collectors.toList());
                });
    }

    private void loadDefaultFunctions()
    {
        loadFunction(new LinkedList<FunctionInfo>()
        {
            {
                add(FunctionInfo.from("天气", R.mipmap.image_weather, "dng/ux/weather"));
                add(FunctionInfo.from("多媒体", R.mipmap.image_media, "dng/media/browse"));
                add(FunctionInfo.from("APPS", R.mipmap.image_apps, "dng/ux/apps"));
            }
        }).subscribe(new Observer<List<Function>>()
        {
            @Override
            public void onSubscribe(Disposable d)
            {

            }

            @Override
            public void onNext(List<Function> functions)
            {
                DesktopViewModel.this.functions.postValue(functions);
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void startTimer()
    {
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                time.postValue(TimeUtils.getNowString());
            }
        }, 0, 1000);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void endTimer()
    {
        updateTimer.cancel();
    }

}
