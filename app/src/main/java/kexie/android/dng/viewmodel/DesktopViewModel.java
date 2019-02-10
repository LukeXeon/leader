package kexie.android.dng.viewmodel;

import android.app.Application;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import kexie.android.dng.R;
import kexie.android.dng.entity.LiteUserInfo;
import okhttp3.OkHttpClient;


public class DesktopViewModel
        extends AndroidViewModel
        implements LifecycleObserver
{
    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<LiteUserInfo> userInfo = new MutableLiveData<>();
    private final MutableLiveData<String> time = new MutableLiveData<>();
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

    public LiveData<String> getTime()
    {
        return time;
    }

    public LiveData<LiteUserInfo> getUserInfo()
    {
        return userInfo;
    }

    @Override
    protected void onCleared()
    {
    }
}
