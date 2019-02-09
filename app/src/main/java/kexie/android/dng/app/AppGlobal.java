package kexie.android.dng.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import androidx.multidex.MultiDexApplication;
import kexie.android.navi.NetworkMonitoringService;


public class AppGlobal extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());
        bindService(new Intent(this,
                        NetworkMonitoringService.class),
                new ServiceConnection()
                {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service)
                    {

                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name)
                    {

                    }
                }, BIND_AUTO_CREATE);
    }
}
