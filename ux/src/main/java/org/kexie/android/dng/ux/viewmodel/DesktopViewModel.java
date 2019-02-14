package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.kexie.android.common.util.ZoomTransformation;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.viewmodel.entity.Function;
import org.kexie.android.dng.ux.viewmodel.entity.LiteUserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
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
import okhttp3.OkHttpClient;


public class DesktopViewModel
        extends AndroidViewModel
        implements LifecycleObserver
{

    private final static class FunctionInfo
    {
        private final String name;
        private final int iconRes;
        private final Uri uri;

        private FunctionInfo(String name, int iconRes, Uri uri)
        {
            this.iconRes = iconRes;
            this.name = name;
            this.uri = uri;
        }
    }

    private static final int BORDER_SIZE = 250;
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<LiteUserInfo> userInfo = new MutableLiveData<>();
    private final MutableLiveData<String> time = new MutableLiveData<>();
    private final Map<Function, Uri> functionJumpTo = new ArrayMap<>();
    private final PublishSubject<Uri> onJumpTo = PublishSubject.create();
    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();
    private Timer updateTimer;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    public DesktopViewModel(Application application)
    {
        super(application);
        init();
    }

    private void init()
    {
        singleTask.execute(() -> {
            try
            {
                LiteUserInfo user = new LiteUserInfo(
                        Glide.with(getApplication())
                                .load(R.mipmap.image_head_man)
                                .submit().get(),
                        "未登陆");
                userInfo.postValue(user);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public void requestJumpBy(Function function)
    {
        Uri uri = functionJumpTo.get(function);
        if (uri != null)
        {
            onJumpTo.onNext(uri);
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
                            .map(info -> Glide.with(this.getApplication())
                                    .load(info.iconRes)
                                    .apply(RequestOptions
                                            .bitmapTransform(zoomTransformation))
                                    .listener(listenerBy(info))
                                    .submit())
                            .forEach(futureTarget -> {
                                try
                                {
                                    futureTarget.get();
                                } catch (Exception e)
                                {
                                    throw Exceptions.propagate(e);
                                }
                            });
                    return new ArrayList<>(functionJumpTo.keySet());
                });
    }

    private RequestListener<Drawable> listenerBy(FunctionInfo info)
    {
        return new RequestListener<Drawable>()
        {
            @Override
            public boolean onLoadFailed(
                    @Nullable GlideException e,
                    Object model,
                    Target<Drawable> target,
                    boolean isFirstResource)
            {
                Resources resources = getApplication()
                        .getResources();
                Function function = new Function(info.name,
                        new BitmapDrawable(resources,
                                BitmapFactory.decodeResource(
                                        resources,
                                        info.iconRes)));
                functionJumpTo.put(function, info.uri);
                return true;
            }

            @Override
            public boolean onResourceReady(
                    Drawable resource,
                    Object model,
                    Target<Drawable> target,
                    DataSource dataSource,
                    boolean isFirstResource)
            {
                Function function = new Function(
                        info.name,
                        resource);
                functionJumpTo.put(function, info.uri);
                return true;
            }
        };
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
        return new FunctionInfo(name, icon, Uri.parse(path));
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

    public Observable<Uri> getOnJumpTo()
    {
        return onJumpTo;
    }

    public LiveData<String> getTime()
    {
        return time;
    }

    public LiveData<LiteUserInfo> getUserInfo()
    {
        return userInfo;
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
