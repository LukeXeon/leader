package kexie.android.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import com.blankj.utilcode.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class T
{
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private static final LruCache<Integer, BitmapDrawable> MIPMAP_CACHE
            = new LruCache<Integer, BitmapDrawable>((int) Runtime.getRuntime()
            .maxMemory() / 32)
    {
        @Override
        protected int sizeOf(Integer key, BitmapDrawable value)
        {
            return value.getBitmap().getByteCount();
        }
    };

    static void init(Context context)
    {
        context = context.getApplicationContext();
        Utils.init(context);
        T.context = context;
    }

    public static Context context()
    {
        return Objects.requireNonNull(context);
    }

    private T()
    {
        throw new AssertionError();
    }

    public static boolean isEmpty(List list)
    {
        return list == null || list.size() == 0;
    }

    public static boolean isEmpty(Map map)
    {
        return map == null || map.size() == 0;
    }

    public static Drawable mipmap(int res)
    {
        BitmapDrawable drawable = MIPMAP_CACHE.get(res);
        if (drawable == null)
        {
            Resources resources = context().getResources();
            drawable = new BitmapDrawable(resources,
                    BitmapFactory.decodeResource(resources, res));
            MIPMAP_CACHE.put(res, drawable);
        }
        return drawable;
    }
}
