package kexie.android.dng.viewmodel.desktop;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kexie.android.dng.R;
import kexie.android.dng.entity.desktop.Function;
import kexie.android.dng.entity.desktop.User;
import kexie.android.dng.model.FunctionFactory;
import okhttp3.OkHttpClient;


public class DesktopViewModel
        extends AndroidViewModel
        implements LifecycleObserver
{
    private static final int BORDER_SIZE = 250;

    private final Executor singleTask = Executors.newSingleThreadExecutor();
    private final MutableLiveData<User> userInfo = new MutableLiveData<>();
    private final MutableLiveData<String> time = new MutableLiveData<>();
    private Timer updateTimer;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    public DesktopViewModel(Application application)
    {
        super(application);
        initDefault();
    }

    private void initDefault()
    {
        singleTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    User user = new User(Glide.with(getApplication())
                            .load(R.mipmap.image_head_man)
                            .submit().get(),
                            "未登陆");
                    userInfo.postValue(user);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
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

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void endTimer()
    {
        updateTimer.cancel();
    }

    public LiveData<List<Function>> getFunctions()
    {
        final MutableLiveData<List<Function>> liveData = new MutableLiveData<>();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FunctionFactory factory
                            = new FunctionFactory(getApplication(),
                            BORDER_SIZE);
                    liveData.postValue(factory.getDefault());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
        return liveData;
    }

    public LiveData<String> getTime()
    {
        return time;
    }

    public LiveData<User> getUserInfo()
    {
        return userInfo;
    }

    @Override
    protected void onCleared()
    {
    }
}
