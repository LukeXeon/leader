package org.kexie.android.dng.ai.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.ai.BR;
import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.ai.viewmodel.beans.TextMessage;
import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.contract.NLP;
import org.kexie.android.dng.common.contract.TTS;
import org.kexie.android.dng.common.util.LiveEvent;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class SiriViewModel
        extends AndroidViewModel
        implements ASR.Handler {
    //data hold
    public final GenericQuickAdapter<TextMessage> messages
            = new GenericQuickAdapter<>(R.layout.item_message, BR.message);

    public final LiveEvent<Runnable> action = new LiveEvent<>();
    public final LiveEvent<Integer> scroll = new LiveEvent<>();
    public final LiveEvent<String> part = new LiveEvent<>();
    public final LiveEvent<Integer> volume = new LiveEvent<>();
    public final MutableLiveData<Integer> status = new MutableLiveData<>();

    private final Handler worker;
    private final HandlerThread workerThread;
    private final NLP nlp;
    public final ASR asr;
    private final TTS tts;

    public SiriViewModel(@NonNull Application application) {
        super(application);
        nlp = (NLP) ARouter.getInstance().build(Module.Ai.nlp).navigation(application);
        asr = (ASR) ARouter.getInstance().build(Module.Ai.asr).navigation(application);
        tts = (TTS) ARouter.getInstance().build(Module.Ai.tts).navigation(application);
        tts.stop();
        asr.stop();
        asr.addHandler(this);
        workerThread = new HandlerThread(toString());
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
    }

    @Override
    protected void onCleared() {
        tts.stop();
        asr.stop();
        worker.removeCallbacksAndMessages(null);
        workerThread.quit();
        worker.removeCallbacksAndMessages(null);
        asr.addHandler(this);
    }

    @Override
    public void onStatusUpdate(int status) {
        this.status.setValue(status);
    }

    @Override
    public void onVolumeUpdate(int value) {
        this.volume.post(value);
    }

    @Override
    public void onWeakUp(@NonNull String text) {
        asr.begin();
    }

    @Override
    public void onResult(boolean isFinal, @NonNull String text) {
        if (isFinal) {
            messages.addData(new TextMessage(TextMessage.TYPE_USER, text));
            scroll.post(messages.getHeaderLayoutCount());
            worker.post(() -> {
                Object result = nlp.process(text);
                if (result instanceof String) {
                    tts.send((String) result);
                } else if (result instanceof Runnable) {
                    action.post((Runnable) result);
                }
            });
        } else {
            part.post(text);
        }
    }
}