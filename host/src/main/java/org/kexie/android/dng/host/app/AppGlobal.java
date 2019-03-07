package org.kexie.android.dng.host.app;

import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.host.BuildConfig;

import androidx.multidex.MultiDexApplication;


public class AppGlobal extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        if (BuildConfig.DEBUG)
        {
            Logger.addLogAdapter(new AndroidLogAdapter());
            ARouter.openLog();     // Print log
            ARouter.openDebug();
        }
        ARouter.init(this);
    }
}
