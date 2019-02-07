package kexie.android.dng.entity;

import android.graphics.drawable.Drawable;

public class LiteUserInfo
{
    public final Drawable headImage;
    public final String username;

    public LiteUserInfo(Drawable headImage, String username)
    {
        this.headImage = headImage;
        this.username = username;
    }
}
