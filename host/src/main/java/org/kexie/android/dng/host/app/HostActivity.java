package org.kexie.android.dng.host.app;

import android.os.Bundle;

import com.orhanobut.logger.Logger;

import org.kexie.android.common.util.SystemUtil;
import org.kexie.android.dng.host.R;
import org.kexie.android.dng.ux.view.DesktopFragment;

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
        Logger.d("test");
        SystemUtil.hideSystemUi(getWindow());
        DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new DesktopFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
