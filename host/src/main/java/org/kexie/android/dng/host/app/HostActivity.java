package org.kexie.android.dng.host.app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.kexie.android.common.util.SystemUtil;
import org.kexie.android.dng.host.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import mapper.Mapper;
import mapper.Request;

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
                        Mapper.getOn(this, new Request.Builder()
                                .uri("dng/navi/navi")
                                .build()), "dng/ux/main")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
    }

}
