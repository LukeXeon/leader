package org.kexie.android.dng.weather.model;

import org.kexie.android.dng.weather.model.entity.JsonWallpapers;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface WallpaperProvider
{
    @GET("/HPImageArchive.aspx?format=js&idx=0&n=1")
    Observable<JsonWallpapers> getWallpaperInfo();
}
