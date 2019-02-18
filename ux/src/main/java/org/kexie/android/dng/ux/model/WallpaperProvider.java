package org.kexie.android.dng.ux.model;

import org.kexie.android.dng.ux.model.entity.JsonWallpapers;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface WallpaperProvider
{
    @GET("http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1")
    Observable<JsonWallpapers> getWallpaperInfo();
}
