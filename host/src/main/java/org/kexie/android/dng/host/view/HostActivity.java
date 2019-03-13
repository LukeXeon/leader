package org.kexie.android.dng.host.view;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.RxOnClick;
import org.kexie.android.dng.common.widget.SystemUtil;
import org.kexie.android.dng.host.R;
import org.kexie.android.dng.host.databinding.ActivityHostBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java8.util.stream.IntStreams;

@Route(path = PR.host.host)
public final class HostActivity extends AppCompatActivity
{

    private ActivityHostBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SystemUtil.hideSystemUi(getWindow());
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        binding.setOnBack(new RxOnClick(this, v -> {
            if (getSupportFragmentManager().getFragments().size() > 1)
            {
                onBackPressed();
            }
        }));
        binding.setOnHome(new RxOnClick(this, v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int size = fragmentManager.getFragments().size() - 1;
            IntStreams.iterate(1, i -> i < size, i -> i + 1)
                    .forEach(i -> onBackPressed());
        }));
        binding.setOnSpeak(new RxOnClick(this, v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(PR.asr.speaker);
            if (fragment == null)
            {
                fragment = (Fragment) ARouter.getInstance()
                        .build(PR.asr.speaker)
                        .navigation();
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, PR.asr.speaker)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            } else
            {
                fragmentManager.beginTransaction()
                        .show(fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        }));

        Fragment fragment = (Fragment) ARouter.getInstance()
                .build(PR.ux.desktop)
                .navigation();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment, PR.ux.desktop)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
