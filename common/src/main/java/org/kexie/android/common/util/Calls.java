package org.kexie.android.common.util;

/**
 * Created by Mr.小世界 on 2018/10/13.
 */

public final class Calls
{
    private Calls()
    {
    }

    public static <T> T safeCall(Call<T> call)
    {
        if (call != null)
        {
            return call.call();
        }
        return null;
    }

    public static <T> void safeCallback(Callback<T> callback, T value)
    {
        if (callback != null)
        {
            callback.onResult(value);
        }
    }

    public static <T,R> R safeFunc(Func<T,R> func,T value)
    {
        if (func != null)
        {
            return func.call(value);
        }
        return null;
    }

}
