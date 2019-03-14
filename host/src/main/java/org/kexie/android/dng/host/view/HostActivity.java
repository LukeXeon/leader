package org.kexie.android.dng.host.view;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.RxOnClick;
import org.kexie.android.dng.common.model.SpeakerService;
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

    //@Autowired(name = PR.asr.service)
    SpeakerService speakerService;

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
            int size = fragmentManager.getFragments().size();
            if (size == 1)
            {
                return;
            }
            IntStreams.iterate(1, i -> i < size - 1, i -> i + 1)
                    .forEach(i -> fragmentManager.popBackStackImmediate());
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

        speakerService = (SpeakerService) ARouter.getInstance()
                .build(PR.asr.service)
                .navigation();



        speakerService.getWeakUp()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(Logger::d);

//        Fragment fragment = (Fragment) ARouter.getInstance()
//                .build(PR.ux.desktop)
//                .navigation();
//
//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.fragment_container, fragment, PR.ux.desktop)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .commit();
    }
}
