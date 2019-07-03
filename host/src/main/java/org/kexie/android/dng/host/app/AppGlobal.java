package org.kexie.android.dng.host.app;

import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.util.Utils;
import org.kexie.android.dng.host.BuildConfig;

import androidx.multidex.MultiDexApplication;
import cn.jpush.android.api.JPushInterface;
import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;


public final class AppGlobal extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        if (BuildConfig.DEBUG)
        {
            JPushInterface.setDebugMode(true);
            Logger.addLogAdapter(new AndroidLogAdapter());
            ARouter.openLog();     // Print log
            ARouter.openDebug();
        }

        JPushInterface.init(this);

        ARouter.init(this);

        //DoraemonKit.install(this);

        AutoSizeConfig.getInstance()
                .setCustomFragment(true);
        AutoSize.initCompatMultiProcess(this);

        Logger.d(Utils.getSha1Code(this));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
