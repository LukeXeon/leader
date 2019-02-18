package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;

import org.kexie.android.dng.ux.model.WallpaperProvider;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherViewModel extends AndroidViewModel
{
    public WeatherViewModel(@NonNull Application application)
    {
        super(application);
    }

    private Observable<Drawable> loadWallpaper()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("")
                .client(new OkHttpClient())
                .build();
        return retrofit.create(WallpaperProvider.class)
                .getWallpaperInfo()
                .subscribeOn(Schedulers.io())
                .map(jsonWallpapers -> "http://cn.bing.com"
                        + jsonWallpapers.wallpapers.get(0).part)
                .map(uri -> {
                    try
                    {
                        return Glide.with(getApplication()).load(uri).submit().get();
                    } catch (Exception e)
                    {
                        throw Exceptions.propagate(e);
                    }
                });
    }
}
