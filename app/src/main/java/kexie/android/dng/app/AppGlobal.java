package kexie.android.dng.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;


public class AppGlobal extends MultiDexApplication
{
    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static UUID uuid;

    private synchronized void init()
    {
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public synchronized UUID getUuid()
    {
        if (uuid == null)
        {
            final SharedPreferences prefs = this.getSharedPreferences(PREFS_FILE, 0);
            final String id = prefs.getString(PREFS_DEVICE_ID, null);
            if (id != null)
            {
                uuid = UUID.fromString(id);
            } else
            {
                final String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                try
                {
                    if (!"9774d56d682e549c".equals(androidId))
                    {
                        uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                    } else
                    {
                        final String deviceId = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                        uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                    }
                } catch (UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e);
                }
                // Write the value out to the prefs file
                prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
            }
        }
        return uuid;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        init();
    }
}
