package org.kexie.android.dng.host.view;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.util.Utils;
import org.kexie.android.dng.host.R;
import org.kexie.android.dng.host.databinding.ActivityHostBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java8.util.stream.IntStreams;

@Route(path = Module.Host.host)
public final class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.hideSystemUi(getWindow());
        ActivityHostBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        binding.setOnBack(v -> {
            Logger.d(getSupportFragmentManager().getBackStackEntryCount());
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                onBackPressed();
            }
        });

        binding.setOnHome(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int size = getSupportFragmentManager().getBackStackEntryCount();
            if (size == 0) {
                return;
            }
            IntStreams.iterate(0, i -> i < size, i -> i + 1)
                    .forEach(i -> fragmentManager.popBackStackImmediate());
        });
        binding.setOnSpeak(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(Module.Ai.siri);
            if (fragment == null) {
                fragment = (Fragment) ARouter.getInstance()
                        .build(Module.Ai.siri)
                        .navigation();
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, Module.Ai.siri)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commitAllowingStateLoss();
            }
        });

        ASR asr = (ASR) ARouter.getInstance().build(Module.Ai.asr).navigation();

        asr.addHandler(new ASR.WeakUpHandler(){
            @Override
            public void onWeakUp(@NonNull String text) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag(Module.Ai.siri);
                if (fragment == null) {
                    fragment = (Fragment) ARouter.getInstance()
                            .build(Module.Ai.siri)
                            .withBoolean("weakUp", true)
                            .navigation();
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, fragment, Module.Ai.siri)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commitAllowingStateLoss();
                }
            }
        });


        Fragment fragment = (Fragment) ARouter.getInstance()
                .build(Module.Media.browser)
                .navigation();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment, Module.Ux.desktop)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideSystemUi(getWindow());
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        }
    }
}
