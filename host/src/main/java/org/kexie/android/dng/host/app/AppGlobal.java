package org.kexie.android.dng.host.app;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import androidx.multidex.MultiDexApplication;


public class AppGlobal extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());

    }
}
