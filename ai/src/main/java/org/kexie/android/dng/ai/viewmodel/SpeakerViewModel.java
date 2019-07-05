package org.kexie.android.dng.ai.viewmodel;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.util.LiveEvent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class SpeakerViewModel
        extends AndroidViewModel
        implements ASR.Handler {

    public final LiveEvent<String> finish = new LiveEvent<>();
    public final LiveEvent<String> part = new LiveEvent<>();
    public final LiveEvent<Integer> volume = new LiveEvent<>();
    public final LiveEvent<Integer> status = new LiveEvent<>();
    private final ASR asr;

    public SpeakerViewModel(@NonNull Application application) {
        super(application);
        asr = (ASR) ARouter.getInstance().build(Module.Ai.asr).navigation();
        asr.addHandler(this);
        asr.begin();
    }

    @Override
    protected void onCleared() {
        asr.removeHandler(this);
        asr.stop();
    }

    @Override
    public void onStatusUpdate(int status) {
        this.status.post(status);
    }

    @Override
    public void onVolumeUpdate(int value) {
        volume.post(value);
    }

    @Override
    public void onWeakUp(@NonNull String text) {

    }

    @Override
    public void onResult(boolean isFinal, @NonNull String text) {
        if (isFinal) {
            finish.post(text);
        } else {
            part.post(text);
        }
    }
}
