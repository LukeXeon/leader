package kexie.android.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.UUID;

public final class SystemUtil
{
    private SystemUtil()
    {
        throw new AssertionError();
    }

    public static void hide(Window window)
    {
        window.getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        window.getDecorView().setOnSystemUiVisibilityChangeListener(
                visibility -> {
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            //布局位于状态栏下方
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            //全屏
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            //隐藏导航栏
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    if (Build.VERSION.SDK_INT >= 19)
                    {
                        uiOptions |= 0x00001000;
                    } else
                    {
                        uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                    }
                    window.getDecorView().setSystemUiVisibility(uiOptions);
                });
    }

    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static UUID uuid;

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public synchronized UUID getUuid(Context context)
    {
        context = context.getApplicationContext();
        if (uuid == null)
        {
            final SharedPreferences prefs = context
                    .getSharedPreferences(PREFS_FILE, 0);
            final String id = prefs.getString(PREFS_DEVICE_ID,
                    null);
            if (id != null)
            {
                uuid = UUID.fromString(id);
            } else
            {
                final String androidId = Settings.Secure
                        .getString(context.getContentResolver(),
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
                                Objects.requireNonNull(context
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
}
