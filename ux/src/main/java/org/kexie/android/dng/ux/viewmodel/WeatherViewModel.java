package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;

import org.kexie.android.dng.ux.model.WallpaperProvider;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java8.util.Objects;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherViewModel extends AndroidViewModel
{
    public final MutableLiveData<Drawable> wallpaper = new MutableLiveData<>();

    public WeatherViewModel(@NonNull Application application)
    {
        super(application);
        loadWallpaper();
    }

    private void loadWallpaper()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://cn.bing.com")
                .client(new OkHttpClient())
                .build();
        retrofit.create(WallpaperProvider.class)
                .getWallpaperInfo()
                .observeOn(Schedulers.io())
                .map(jsonWallpapers -> "http://cn.bing.com"
                        + jsonWallpapers.wallpapers.get(0).part)
                .map(uri -> {
                    try
                    {
                        return Glide.with(getApplication()).load(uri).submit().get();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        return null;
                    }
                }).filter(Objects::nonNull)
                .subscribe(new Observer<Drawable>()
                {
                    @Override
                    public void onSubscribe(Disposable d)
                    {

                    }

                    @Override
                    public void onNext(Drawable drawable)
                    {
                        wallpaper.postValue(drawable);
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
}
