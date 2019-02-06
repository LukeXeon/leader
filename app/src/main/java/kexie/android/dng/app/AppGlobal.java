package kexie.android.dng.app;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.UUID;

import kexie.android.navi.NetworkMonitoringService;


public class AppGlobal extends MultiDexApplication
{
    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static UUID uuid;

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public synchronized UUID getUuid()
    {
        if (uuid == null)
        {
            final SharedPreferences prefs = this
                    .getSharedPreferences(PREFS_FILE, 0);
            final String id = prefs.getString(PREFS_DEVICE_ID,
                    null);
            if (id != null)
            {
                uuid = UUID.fromString(id);
            } else
            {
                final String androidId = Settings.Secure
                        .getString(this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                try
                {
                    if (!"9774d56d682e549c".equals(androidId))
                    {
                        uuid = UUID.nameUUIDFromBytes(androidId
                                .getBytes("utf8"));
                    } else
                    {
                        final String deviceId = ((TelephonyManager)
                                Objects.requireNonNull(this
                                        .getSystemService(Context
                                                .TELEPHONY_SERVICE)))
                                .getDeviceId();
                        uuid = deviceId != null
                                ? UUID.nameUUIDFromBytes(deviceId
                                .getBytes("utf8"))
                                : UUID.randomUUID();
                    }
                } catch (UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e);
                }
                // Write the value out to the prefs file
                prefs.edit()
                        .putString(PREFS_DEVICE_ID, uuid.toString())
                        .apply();
            }
        }
        return uuid;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
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
