package org.kexie.android.common.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public final class RequestFragment
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
