package org.kexie.android.dng.host.app;

import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.kexie.android.common.util.SystemUtil;
import org.kexie.android.dng.host.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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

        Fragment fragment = (Fragment) ARouter.getInstance()
                .build("/ux/main")
                .navigation();

        Logger.d(fragment);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

}
