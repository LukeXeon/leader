package org.kexie.android.dng.host.app;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.widget.SHA1Util;
import org.kexie.android.dng.host.BuildConfig;
import org.kexie.android.dng.host.R;

import androidx.multidex.MultiDexApplication;
import me.jessyan.autosize.AutoSizeConfig;


public final class AppGlobal extends MultiDexApplication
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

        DoraemonKit.install(this);

        AutoSizeConfig.getInstance()
                .setCustomFragment(true);

        Glide.with(this)
                .load(R.mipmap.image_car_anim)
                .preload();

        Logger.d(SHA1Util.getSHA1(this));

    }
}
