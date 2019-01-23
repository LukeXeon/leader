package kexie.android.dng.viewmodel.desktop;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kexie.android.dng.R;
import kexie.android.dng.entity.desktop.Function;
import kexie.android.dng.entity.desktop.User;
import kexie.android.common.util.ZoomTransformation;
import okhttp3.OkHttpClient;

public class DesktopViewModel
        extends AndroidViewModel implements LifecycleObserver
{

    private static final List<Pair<String, Integer>> SUPPORT_FUNCTIONS
            = new ArrayList<Pair<String, Integer>>()
    {
        {
            add(Pair.create("天气", R.mipmap.image_weather));
            add(Pair.create("多媒体", R.mipmap.image_media));
            add(Pair.create("APPS", R.mipmap.image_apps));
        }
    };


    private final MutableLiveData<User> userInfo = new MutableLiveData<>();
    private final MutableLiveData<String> time = new MutableLiveData<>();
    private final MutableLiveData<List<Function>> functions = new MutableLiveData<>();
    private Timer updateTimer;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();


    public DesktopViewModel(Application application)
    {
        super(application);
        initFunctions();
        initDefaultUserInfo();
    }

    private void initDefaultUserInfo()
    {
        Glide.with(getApplication())
                .load(R.mipmap.image_head_man)
                .listener(new RequestListener<Drawable>()
                {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource)
                    {
                        new BitmapDrawable(BitmapFactory
                                .decodeResource(getApplication().getResources(),
                                        R.mipmap.image_head_man));
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource)
                    {
                        User user = new User(resource,
                                "未登陆");
                        userInfo.setValue(user);
                        return true;
                    }
                }).submit();
    }

    private void startTimer()
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

    private void endTimer()
    {
        updateTimer.cancel();
    }

    private void initFunctions()
    {
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>()
                {
                    @Override
                    public void accept(Object o) throws Exception
                    {
                        ZoomTransformation transformation
                                = new ZoomTransformation(250);
                        List<FutureTarget<Drawable>> targets
                                = new ArrayList<>(SUPPORT_FUNCTIONS.size());
                        List<Function> desktopFunctions
                                = new ArrayList<>(SUPPORT_FUNCTIONS.size());
                        for (int i = 0; i < SUPPORT_FUNCTIONS.size(); i++)
                        {
                            targets.add(Glide.with(getApplication())
                                    .load(SUPPORT_FUNCTIONS.get(i).second)
                                    .apply(RequestOptions.bitmapTransform(transformation))
                                    .submit());
                        }
                        for (int i = 0; i < SUPPORT_FUNCTIONS.size(); i++)
                        {
                            desktopFunctions.add(
                                    new Function(
                                            targets.get(i).get(),
                                            SUPPORT_FUNCTIONS.get(i).first));
                        }
                        functions.postValue(desktopFunctions);
                    }
                });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause()
    {
        endTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume()
    {
        startTimer();
    }

    public MutableLiveData<List<Function>> getFunctions()
    {
        return functions;
    }

    public MutableLiveData<String> getTime()
    {
        return time;
    }

    public MutableLiveData<User> getUserInfo()
    {
        return userInfo;
    }

    @Override
    protected void onCleared()
    {
    }
}
