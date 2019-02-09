package kexie.android.dng.view;

import android.os.Bundle;

import com.orhanobut.logger.Logger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import kexie.android.common.util.SystemUtil;
import kexie.android.dng.R;

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
