package org.kexie.android.dng.ux.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class SimpleUserInfo
{
    public final Drawable headImage;
    public final String username;
    public final String carNumber;

    public SimpleUserInfo(Drawable headImage, String username, String carNumber)
    {
        this.headImage = headImage;
        this.username = username;
        this.carNumber = carNumber;
    }
}
