package org.kexie.android.dng.ux.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Mr.小世界 on 2018/11/21.
 */

public class JsonWallpapers
{
    @SerializedName("images")
    public List<JsonWallpaper> wallpapers;

    public static class JsonWallpaper
    {
        @SerializedName("url")
        public String part;//这里的链接还需要加上微软的http://cn.bing.com在前面
    }
}
