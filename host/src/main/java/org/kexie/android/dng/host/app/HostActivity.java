package org.kexie.android.dng.host.app;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import org.kexie.android.common.util.SystemUtil;
import org.kexie.android.dng.host.R;
import org.kexie.android.mapper.FragmentIntent;
import org.kexie.android.mapper.Mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

public final class HostActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SystemUtil.hideSystemUi(getWindow());
        DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container,
                        Mapper.mapping(new FragmentIntent.Builder()
                                .uri(Uri.parse("dng/ux/main"))
                                .build()))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
    }

}
