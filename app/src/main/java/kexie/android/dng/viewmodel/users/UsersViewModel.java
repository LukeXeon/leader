package kexie.android.dng.viewmodel.users;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kexie.android.dng.entity.users.SimpleUser;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class UsersViewModel extends AndroidViewModel
{
    Gson gson = new Gson();
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    private final MutableLiveData<Drawable> headImage = new MutableLiveData<>();
    private final MutableLiveData<SimpleUser> simpleUser = new MutableLiveData<>();
    private final MutableLiveData<Map<String, View.OnClickListener>> actions = new MutableLiveData<>();

    public UsersViewModel(@NonNull Application application)
    {
        super(application);
        Request request = new Request.Builder()
                .url("http://172.20.10.5:8080/navigator/device/user/detail/").get().build();
        initActions();
      }

    @Override
    protected void onCleared()
    {
        super.onCleared();
    }

    public MutableLiveData<Map<String, View.OnClickListener>> getActions()
    {
        return actions;
    }

    public MutableLiveData<Drawable> getHeadImage()
    {
        return headImage;
    }

    public MutableLiveData<SimpleUser> getSimpleUser()
    {
        return simpleUser;
    }

    private void initActions()
    {
        actions.setValue(new HashMap<String, View.OnClickListener>()
        {
            {
                put("返回", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Activity activity = (Activity) v.getContext();
                        activity.finish();
                    }
                });
                put("刷新", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                    }
                });
            }
        });
    }
}
