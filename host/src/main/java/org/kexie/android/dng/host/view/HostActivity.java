package org.kexie.android.dng.host.view;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.RxOnClick;
import org.kexie.android.dng.common.model.ASRService;
import org.kexie.android.dng.common.widget.SystemUtil;
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

@Route(path = PR.host.host)
public final class HostActivity extends AppCompatActivity
{

    private ActivityHostBinding binding;

    @Autowired(name = PR.ai.asr_service)
    ASRService ASRService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SystemUtil.hideSystemUi(getWindow());
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_host);
        binding.setOnBack(new RxOnClick(this, v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            {
                onBackPressed();
            }
        }));
        binding.setOnHome(new RxOnClick(this, v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int size = getSupportFragmentManager().getBackStackEntryCount();
            if (size == 0)
            {
                return;
            }
            IntStreams.iterate(0, i -> i < size, i -> i + 1)
                    .forEach(i -> fragmentManager.popBackStackImmediate());
        }));
        binding.setOnSpeak(new RxOnClick(this, v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(PR.ai.speaker);
            if (fragment == null)
            {
                fragment = (Fragment) ARouter.getInstance()
                        .build(PR.ai.speaker)
                        .navigation();
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, PR.ai.speaker)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        }));

        ARouter.getInstance().inject(this);

        ASRService.getWeakUpResult()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag(PR.ai.speaker);
                    if (fragment == null)
                    {
                        fragment = (Fragment) ARouter.getInstance()
                                .build(PR.ai.speaker)
                                .withBoolean("weakUp", true)
                                .navigation();
                        fragmentManager.beginTransaction()
                                .add(R.id.fragment_container, fragment, PR.ai.speaker)
                                .addToBackStack(null)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                    }
                });


        Fragment fragment = (Fragment) ARouter.getInstance()
                .build(PR.ux.desktop)
                .navigation();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment, PR.ux.desktop)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SystemUtil.hideSystemUi(getWindow());
    }

    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            super.onBackPressed();
        }
    }
}
