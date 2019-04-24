package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.viewmodel.entity.User;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

public class InfoViewModel extends AndroidViewModel {
    private Gson gson = new Gson();

    public final MutableLiveData<User> user = new MutableLiveData<>();

    private Disposable disposable;

    public InfoViewModel(@NonNull Application application) {
        super(application);
        loadDefault();
    }

    private void loadDefault() {
        disposable = Observable.<Context>just(getApplication())
                .observeOn(Schedulers.io())
                .map(context -> {
                    try {
                        Drawable drawable = Glide.with(context)
                                .load(R.mipmap.image_head_man)
                                .submit()
                                .get();
                        return new User.Builder()
                                .idCard("xxxxxxxxxxxxxxxxxx")
                                .carNumber("xxxxx")
                                .name("未登录")
                                .phone("未登录")
                                .username("未登录")
                                .headImage(drawable)
                                .verified(false)
                                .build();
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user::setValue);
    }

    @Override
    protected void onCleared() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
