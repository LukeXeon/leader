package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.kexie.android.common.util.ZoomTransformation;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.viewmodel.entity.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.StreamSupport;
import mapper.Request;


public class DesktopViewModel
        extends AndroidViewModel
        implements LifecycleObserver
{

    private final static class FunctionInfo
    {
        private final String name;
        private final int iconRes;
        private final String uri;

        private FunctionInfo(String name, int iconRes, String uri)
        {
            this.iconRes = iconRes;
            this.name = name;
            this.uri = uri;
        }
    }

    private static final int BORDER_SIZE = 250;
    private final MutableLiveData<String> time = new MutableLiveData<>();
    private final Map<Function, String> functionJumpTo = new ArrayMap<>();
    private final PublishSubject<Request> onJumpTo = PublishSubject.create();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();
    private Timer updateTimer;

    public DesktopViewModel(Application application)
    {
        super(application);
    }

    public void requestJumpBy(Function function)
    {
        String uri = functionJumpTo.get(function);
        if (uri != null)
        {
            onJumpTo.onNext(new Request.Builder().uri(uri).build());
        } else
        {
            onErrorMessage.onNext("跳转到" + function.name + "失败");
        }
    }

    private Observable<List<Function>>
    loadFunction(List<FunctionInfo> functionRes)
    {
        return Observable.just(functionRes)
                .observeOn(Schedulers.io())
                .map(raw -> {
                    functionJumpTo.clear();
                    ZoomTransformation zoomTransformation
                            = new ZoomTransformation(BORDER_SIZE);
                    StreamSupport.stream(raw)
                            .map(info -> Pair.create(info, Glide.with(this.getApplication())
                                    .load(info.iconRes)
                                    .apply(RequestOptions
                                            .bitmapTransform(zoomTransformation))
                                    .submit()))
                            .forEach(pair -> {
                                try
                                {
                                    Drawable resource = pair.second.get();
                                    Function function = new Function(
                                            pair.first.name,
                                            resource);
                                    functionJumpTo.put(function, pair.first.uri);
                                } catch (Exception e)
                                {
                                    throw Exceptions.propagate(e);
                                }
                            });
                    return new ArrayList<>(functionJumpTo.keySet());
                });
    }

    public Observable<List<Function>> getDefaultFunction()
    {
        return loadFunction(new ArrayList<FunctionInfo>()
        {
            {
                add(functionBy("天气", R.mipmap.image_weather, "dng/ux/weather"));
                add(functionBy("多媒体", R.mipmap.image_media, "dng/media/browse"));
                add(functionBy("APPS", R.mipmap.image_apps, "dng/ux/apps"));
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    private static FunctionInfo functionBy(String name, int icon, String path)
    {
        return new FunctionInfo(name, icon, path);
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

    public Observable<Request> getOnJumpTo()
    {
        return onJumpTo;
    }

    public LiveData<String> getTime()
    {
        return time;
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage;
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage;
    }

}
