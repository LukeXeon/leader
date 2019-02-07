package kexie.android.common.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
@SuppressLint("ValidFragment")
final class RequestFragment
        extends Fragment
{
    static void createByFirstActivity(Context context,
                                      String[] permission)
    {
        Bundle bundle = new Bundle();
        bundle.putStringArray(RequestFragment.class.getCanonicalName(), permission);
        final RequestFragment fragment = new RequestFragment();
        fragment.setArguments(bundle);
        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(
                new EmptyActivityLifecycleCallbacks()
                {
                    @Override
                    public void onActivityCreated(Activity activity,
                                                  Bundle savedInstanceState)
                    {
                        if (activity instanceof AppCompatActivity)
                        {
                            AppCompatActivity appCompatActivity
                                    = (AppCompatActivity) activity;
                            Application application = (Application)
                                    appCompatActivity.getApplicationContext();
                            FragmentManager fragmentManager
                                    = appCompatActivity.getSupportFragmentManager();
                            FragmentTransaction transaction
                                    = fragmentManager.beginTransaction();
                            transaction.add(fragment, UUID.randomUUID().toString());
                            transaction.commit();
                            application.unregisterActivityLifecycleCallbacks(this);
                        }
                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        String[] requestedPermissions = bundle
                .getStringArray(RequestFragment.class.getCanonicalName());
        assert requestedPermissions != null;
        requestPermissions(requestedPermissions,
                Objects.requireNonNull(getContext())
                        .getApplicationInfo().uid);
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );
        Context application = Objects.requireNonNull(getContext())
                .getApplicationContext();
        if (requestCode == application.getApplicationInfo().uid)
        {
            getFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
        }
    }
}
