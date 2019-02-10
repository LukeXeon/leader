package org.kexie.android.common.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class ZoomTransformation extends BitmapTransformation
{
    private int mBorderWidth;

    public ZoomTransformation(int borderWidth)
    {
        mBorderWidth = borderWidth;
    }

    protected Bitmap transform(@NonNull BitmapPool pool,
                               @NonNull Bitmap source,
                               int outWidth,
                               int outHeight)
    {
        Bitmap result = pool.get(source.getWidth() + mBorderWidth,
                source.getHeight() + mBorderWidth,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, null,
                new Rect(mBorderWidth / 2,
                        mBorderWidth / 2,
                        mBorderWidth / 2 + source.getWidth(),
                        mBorderWidth / 2 + source.getHeight()), null);
        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest)
    {
        messageDigest.update(getClass().getName().getBytes());
    }
}