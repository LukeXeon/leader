package org.kexie.android.common.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public final class PermissionsInitializer
        extends ContentProvider
{
    @Override
    public boolean onCreate()
    {

        init(getContext());
        return true;
    }

    @NonNull
    public static String[] getDefinedPermissions(Context application)
    {
        try
        {
            PackageInfo packageInfo = application.getPackageManager()
                    .getPackageInfo(application.getPackageName(),
                            PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null)
            {
                return packageInfo.requestedPermissions;
            } else
            {
                return new String[0];
            }
        } catch (PackageManager.NameNotFoundException e)
        {
            throw new AssertionError(e);
        }
    }

    @NonNull
    public static String[] getDeniedPermissions(Context application)
    {
        String[] requestedPermissions = getDefinedPermissions(application);
        List<String> requestedPermissionsList = new ArrayList<>();
        if (requestedPermissions.length != 0)
        {
            for (String permission : requestedPermissions)
            {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat
                        .checkSelfPermission(application, permission))
                {
                    requestedPermissionsList.add(permission);
                    // 进入到这里代表没有权限.
                }
            }
        }
        return requestedPermissionsList.toArray(new String[0]);
    }

    public static void init(Context application)
    {
        String[] requestedPermissions = getDeniedPermissions(application);
        if (requestedPermissions.length != 0)
        {
            RequestFragment.createByFirstActivity(application,
                    requestedPermissions);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder)
    {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs)
    {
        return 0;
    }
}
