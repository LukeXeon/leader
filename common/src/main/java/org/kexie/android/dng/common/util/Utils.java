package org.kexie.android.dng.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Utils {
    private Utils() {
        throw new AssertionError();
    }

    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static UUID uuid;

    public static String getSha1Code(Context context) {
        try {
            @SuppressWarnings("deprecation") @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuilder hexString = new StringBuilder();
            for (byte aPublicKey : publicKey) {
                String appendString = Integer.toHexString(0xFF & aPublicKey)
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void hideSystemUi(Window window) {
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
                    if (Build.VERSION.SDK_INT >= 19) {
                        uiOptions |= 0x00001000;
                    } else {
                        uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                    }
                    window.getDecorView().setSystemUiVisibility(uiOptions);
                });
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static synchronized UUID getHardwareId(Context context) {
        SpeechRecognizer speechRecognizer;
        context = context.getApplicationContext();
        if (uuid == null) {
            final SharedPreferences prefs = context
                    .getSharedPreferences(PREFS_FILE, 0);
            final String id = prefs.getString(PREFS_DEVICE_ID,
                    null);
            if (id != null) {
                uuid = UUID.fromString(id);
            } else {
                final String androidId = Settings.Secure
                        .getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId
                            .getBytes(StandardCharsets.UTF_8));
                } else {
                    final String deviceId = ((TelephonyManager)
                            Objects.requireNonNull(context
                                    .getSystemService(Context
                                            .TELEPHONY_SERVICE)))
                            .getDeviceId();
                    uuid = deviceId != null
                            ? UUID.nameUUIDFromBytes(deviceId
                            .getBytes(StandardCharsets.UTF_8))
                            : UUID.randomUUID();
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
