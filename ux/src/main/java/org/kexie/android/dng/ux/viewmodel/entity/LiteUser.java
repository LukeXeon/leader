package org.kexie.android.dng.ux.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class LiteUser
{
    public final Drawable headImage;
    public final String username;
    public final String carNumber;

    public LiteUser(Drawable headImage,
                    String username,
                    String carNumber)
    {
        this.headImage = headImage;
        this.username = username;
        this.carNumber = carNumber;
    }
}
