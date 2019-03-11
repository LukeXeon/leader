package org.kexie.android.dng.host;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

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
        AutoSizeConfig.getInstance()
                .setCustomFragment(true);

        ARouter.init(this);
        Glide.with(this)
                .load(R.mipmap.image_car_anim)
                .preload();
    }
}
