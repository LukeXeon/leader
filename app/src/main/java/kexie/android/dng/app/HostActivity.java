package kexie.android.dng.app;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import kexie.android.dng.R;
import kexie.android.dng.view.DesktopFragment;

public final class HostActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container,
                        new DesktopFragment())
                .commit();
    }
}
