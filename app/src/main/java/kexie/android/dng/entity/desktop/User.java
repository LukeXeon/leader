package kexie.android.dng.entity.desktop;

import android.graphics.drawable.Drawable;

public class User
{
    public final Drawable headImage;
    public final String username;

    public User(Drawable headImage, String username)
    {
        this.headImage = headImage;
        this.username = username;
    }
}
