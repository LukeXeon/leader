package kexie.android.dng.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kexie.android.dng.R;
import kexie.android.dng.entity.UserInfo;

public class UsersViewModel extends AndroidViewModel
{
    private Gson gson = new Gson();
    private Executor singleTask
            = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Drawable> headImage = new MutableLiveData<>();
    private final MutableLiveData<UserInfo> simpleUser = new MutableLiveData<>();

    public UsersViewModel(@NonNull Application application)
    {
        super(application);
        init();
    }

    private void init()
    {
        singleTask.execute(() -> {
            try
            {
                Drawable drawable = Glide.with((Context) getApplication())
                        .load(R.mipmap.image_head_man)
                        .submit()
                        .get();
                UserInfo user = new UserInfo.Builder()
                        .idCard("xxxxxxxxxxxxxxxxxx")
                        .carNumber("xxxxx")
                        .name("未登录")
                        .phone("未登录")
                        .username("未登录")
                        .verified(false)
                        .build();
                headImage.postValue(drawable);
                simpleUser.postValue(user);
            } catch (ExecutionException
                    | InterruptedException e)
            {
                e.printStackTrace();
            }
        });
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

    public MutableLiveData<UserInfo> getSimpleUser()
    {
        return simpleUser;
    }

}
