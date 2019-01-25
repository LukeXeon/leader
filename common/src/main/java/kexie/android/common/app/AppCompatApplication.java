package kexie.android.common.app;

import android.support.annotation.CallSuper;
import android.support.multidex.MultiDexApplication;


import me.jessyan.autosize.AutoSize;
import xiaofei.library.hermeseventbus.HermesEventBus;

public class AppCompatApplication extends MultiDexApplication
{
    private static boolean mainCreate = true;

    @Override
    public synchronized final void onCreate()
    {
        super.onCreate();
        AutoSize.initCompatMultiProcess(this);
        HermesEventBus.getDefault().init(this);
        if (mainCreate)
        {
            mainCreate = false;
            onCreateMainProcess();
        } else
        {
            onCreateSubProcess();
        }
    }

    @CallSuper
    protected void onCreateSubProcess()
    {

    }

    @CallSuper
    protected void onCreateMainProcess()
    {
        registerActivityLifecycleCallbacks(new EventBusHandler());
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        HermesEventBus.getDefault().destroy();
    }
}
