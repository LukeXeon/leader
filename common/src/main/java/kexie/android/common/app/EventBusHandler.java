package kexie.android.common.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import xiaofei.library.hermeseventbus.HermesEventBus;

public final class EventBusHandler implements Application.ActivityLifecycleCallbacks
{
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState)
    {
        try
        {
            HermesEventBus.getDefault().register(activity);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityStarted(Activity activity)
    {

    }

    @Override
    public void onActivityResumed(Activity activity)
    {

    }

    @Override
    public void onActivityPaused(Activity activity)
    {

    }

    @Override
    public void onActivityStopped(Activity activity)
    {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState)
    {

    }

    @Override
    public void onActivityDestroyed(Activity activity)
    {
        try
        {
            HermesEventBus.getDefault().unregister(activity);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
