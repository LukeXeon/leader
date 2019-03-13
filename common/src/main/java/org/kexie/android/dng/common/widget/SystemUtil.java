package org.kexie.android.dng.common.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

    public static void hideSystemUi(Window window)
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

    /**
     * 根据图片的Uri获取图片的绝对路径(适配多种API)
     *
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String uri2Path(Context context, Uri uri)
    {
        String scheme = uri.getScheme();
        if (scheme == null || ContentResolver.SCHEME_FILE.equals(scheme))
        {
            return uri.getPath();
        }
        if (Build.VERSION.SDK_INT < 11)
        {
            return getRealPathFromUri_BelowApi11(context, uri);
        }
        if (Build.VERSION.SDK_INT < 19)
        {
            return getRealPathFromUri_Api11To18(context, uri);
        } else
        {
            return getRealPathFromUri_AboveApi19(context, uri);
        }
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromUri_AboveApi19(Context context, Uri uri)
    {
        String filePath = null;
        String wholeID = DocumentsContract.getDocumentId(uri);

        // 使用':'分割
        String[] ids = wholeID.split(":");
        String id = null;
        if (ids == null)
        {
            return null;
        }
        if (ids.length > 1)
        {
            id = ids[1];
        } else
        {
            id = ids[0];
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media._ID + "=?";
        String[] selectionArgs = {id};

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//
                projection, selection, selectionArgs, null);
        int columnIndex = cursor.getColumnIndex(projection[0]);
        if (cursor.moveToFirst()) filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    /**
     * 适配api11-api18,根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_Api11To18(Context context, Uri uri)
    {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null)
        {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_BelowApi11(Context context, Uri uri)
    {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }
}
