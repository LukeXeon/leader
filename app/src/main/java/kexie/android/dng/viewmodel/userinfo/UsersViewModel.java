package kexie.android.dng.viewmodel.userinfo;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import kexie.android.dng.entity.users.SimpleUser;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class UsersViewModel extends AndroidViewModel
{
    private Gson gson = new Gson();
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    private final MutableLiveData<Drawable> headImage = new MutableLiveData<>();
    private final MutableLiveData<SimpleUser> simpleUser = new MutableLiveData<>();

    public UsersViewModel(@NonNull Application application)
    {
        super(application);
        Request request = new Request.Builder()
                .url("http://172.20.10.5:8080/navigator/device/user/detail/")
                .get()
                .build();
    }

    @Override
    protected void onCleared()
    {
        super.onCleared();
    }

    public MutableLiveData<Drawable> getHeadImage()
    {
        return headImage;
    }

    public MutableLiveData<SimpleUser> getSimpleUser()
    {
        return simpleUser;
    }

}
