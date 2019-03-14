package org.kexie.android.dng.asr.viewmodel;

import android.app.Application;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.SpeakerService;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class SpeakerViewModel extends AndroidViewModel
{
    @Autowired(name = PR.asr.service)
    SpeakerService speakerService;

    public SpeakerViewModel(@NonNull Application application)
    {
        super(application);
        ARouter.getInstance().inject(this);
        speakerService.getWeakUp().subscribe(s -> {

        });
    }
}
