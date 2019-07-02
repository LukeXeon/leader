package org.kexie.android.dng.host.view;

import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.model.ASRService;
import org.kexie.android.dng.host.R;
import org.kexie.android.dng.host.databinding.ActivityHostBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import java8.util.stream.IntStreams;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = Module.host.host)
public final class HostActivity extends AppCompatActivity {

    private ActivityHostBinding binding;

    @Autowired(name = Module.ai.asr_service)
    ASRService asrService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.hideSystemUi(getWindow());
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        binding.setOnBack(RxUtils.debounce(
                View.OnClickListener.class,
                getLifecycle(),
                v -> {
                    Logger.d(getSupportFragmentManager().getBackStackEntryCount());
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        onBackPressed();
                    }
                }));

        binding.setOnHome(
                RxUtils.debounce(
                        View.OnClickListener.class,
                        getLifecycle(),
                        v -> {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            int size = getSupportFragmentManager().getBackStackEntryCount();
                            if (size == 0) {
                                return;
                            }
                            IntStreams.iterate(0, i -> i < size, i -> i + 1)
                                    .forEach(i -> fragmentManager.popBackStackImmediate());
                        }));
        binding.setOnSpeak(RxUtils.debounce(
                View.OnClickListener.class,
                getLifecycle(),
                v -> {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag(Module.ai.speaker);
                    if (fragment == null) {
                        fragment = (Fragment) ARouter.getInstance()
                                .build(Module.ai.speaker)
                                .navigation();
                        fragmentManager.beginTransaction()
                                .add(R.id.fragment_container, fragment, Module.ai.speaker)
                                .addToBackStack(null)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commitAllowingStateLoss();
                    }
                }));

        ARouter.getInstance().inject(this);

        asrService.getWeakUpResult()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag(Module.ai.speaker);
                    if (fragment == null) {
                        fragment = (Fragment) ARouter.getInstance()
                                .build(Module.ai.speaker)
                                .withBoolean("weakUp", true)
                                .navigation();
                        fragmentManager.beginTransaction()
                                .add(R.id.fragment_container, fragment, Module.ai.speaker)
                                .addToBackStack(null)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commitAllowingStateLoss();
                    }
                });


        Fragment fragment = (Fragment) ARouter.getInstance()
                .build(Module.ux.login)
                .navigation();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment, Module.ux.desktop)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemUtil.hideSystemUi(getWindow());
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        }
    }
}
