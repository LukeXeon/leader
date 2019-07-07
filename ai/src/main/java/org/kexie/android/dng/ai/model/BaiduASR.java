package org.kexie.android.dng.ai.model;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.math.MathUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;

import java.util.Collections;
import java.util.LinkedList;

@Route(path = Module.Ai.asr)
public class BaiduASR implements ASR, EventListener {

    private static final int ERROR_NONE = 0;
    private static final int NORMAL_VID = 1536;
    private static final String WEAK_UP_BIN = "assets:///WakeUp.bin";
    private static final String WEAK_UP_TEXT = "嘿领航员";

    private EventManager eventManager;
    private final Gson gson = new Gson();
    private final Session session = new Session();

    @Override
    public void addHandler(@NonNull Handler handler) {
        session.add(handler);
    }

    @Override
    public void removeHandler(@NonNull Handler handler) {
        session.remove(handler);
    }

    @Override
    public void begin(int ms) {
        // 必须主线程
        assertMainThread();
        // 必须待机态
        if (IDLE != session.status) {
            Logger.d("asr no idle");
        }
        ArrayMap<String, Object> map = new ArrayMap<>(4);
        map.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        map.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        map.put(SpeechConstant.PID, NORMAL_VID);
        map.put(SpeechConstant.AUDIO_MILLS,
                System.currentTimeMillis() - MathUtils.clamp(ms, 0, WEAK_UP_BACK_TRACK_IN_MS));
        eventManager.send(SpeechConstant.ASR_START, gson.toJson(map), null, 0, 0);
        session.onStatusUpdate(PREPARE);
    }

    @Override
    public void stop() {
        eventManager.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
    }

    @Override
    public void init(Context context) {
        context = context.getApplicationContext();
        EventManager weakUpManager = EventManagerFactory.create(context, "wp", true);
        weakUpManager.registerListener(this);
        eventManager = EventManagerFactory.create(context, "asr", true);
        eventManager.registerListener(this);
        weakUpManager.send(SpeechConstant.WAKEUP_START,
                gson.toJson(Collections.singletonMap(SpeechConstant.WP_WORDS_FILE, WEAK_UP_BIN)),
                null, 0, 0);
        Logger.d(getClass().getName() + " start");
    }

    private static void assertMainThread() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new AssertionError("must run at navigator thread");
        }
    }

    @Override
    public int getStatus() {
        return session.status;
    }

    @Override
    public int getVolume() {
        return session.volume;
    }

    @Override
    public void onEvent(String name, String pram, byte[] data, int offset, int length) {
        //       Logger.d(name);
        if (session.status == INITIALIZATION
                && SpeechConstant.CALLBACK_EVENT_WAKEUP_READY.equals(name)) {
            session.status = IDLE;
            return;
        }
        switch (name) {
            // 引擎准备就绪，可以开始说话
            case SpeechConstant.CALLBACK_EVENT_ASR_READY: {
                session.onStatusUpdate(SPEAKING);
            }
            break;
            // 检测到用户的已经停止说话,开始识别
            case SpeechConstant.CALLBACK_EVENT_ASR_END: {
                session.onStatusUpdate(RECOGNITION);
                session.onVolumeUpdate(0);
            }
            break;
            // 实时音量
            case SpeechConstant.CALLBACK_EVENT_ASR_VOLUME: {
                VolumeResult volumeResult = gson.fromJson(pram, VolumeResult.class);
                if (SPEAKING != session.status) {
                    return;
                }
                session.onVolumeUpdate(volumeResult.volumePercent);
            }
            break;
            // 取消
            case SpeechConstant.CALLBACK_EVENT_ASR_CANCEL: {
                session.onStatusUpdate(IDLE);
                session.onVolumeUpdate(0);
                session.onCancel();
            }
            break;
            // 引擎完成
            case SpeechConstant.CALLBACK_EVENT_ASR_FINISH: {
                session.onStatusUpdate(IDLE);
                session.onVolumeUpdate(0);
            }
            break;
            // 结果处理
            case SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL: {
                SpeakerResult speakerResult = gson.fromJson(pram, SpeakerResult.class);
                if (!speakerResult.hasError()
                        && speakerResult.results != null
                        && speakerResult.results.length > 0) {
                    session.onResult(!speakerResult.isPartialResult(), speakerResult.results[0]);
                }
            }
            break;
            case SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR: {
                Logger.d(pram);
            }
            break;
            case SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS: {
                WeakUpResult weakUpResult = gson.fromJson(pram, WeakUpResult.class);
                if (!weakUpResult.hasError() && WEAK_UP_TEXT.equals(weakUpResult.word)) {
                    session.onWeakUp(WEAK_UP_TEXT);
                }
            }
            break;
        }
    }

    private static final class Session implements Handler {

        LinkedList<Handler> handlers = new LinkedList<>();

        int status = INITIALIZATION;
        int volume = 0;

        void add(Handler handler) {
            handlers.add(handler);
        }

        void remove(Handler handler) {
            handlers.remove(handler);
        }

        @Override
        public void onStatusUpdate(int status) {
            this.status = status;
            for (Handler handler : handlers) {
                handler.onStatusUpdate(status);
            }
        }

        @Override
        public void onVolumeUpdate(int value) {
            this.volume = value;
            for (Handler handler : handlers) {
                handler.onVolumeUpdate(value);
            }
        }

        @Override
        public void onWeakUp(@NonNull String text) {
            for (Handler handler : handlers) {
                handler.onWeakUp(text);
            }
        }

        @Override
        public void onResult(boolean isFinal, @NonNull String text) {
            for (Handler handler : handlers) {
                handler.onResult(isFinal, text);
            }
        }

        @Override
        public void onCancel() {
            for (Handler handler : handlers) {
                handler.onCancel();
            }
        }

    }

    private static final class WeakUpResult {
        @SerializedName("word")
        String word;
        @SerializedName("desc")
        String desc;
        @SerializedName("error")
        int errorCode;

        boolean hasError() {
            return errorCode != ERROR_NONE;
        }
    }

    private static final class VolumeResult {
        @SerializedName("volume-percent")
        int volumePercent = -1;
        @SerializedName("volume")
        int volume = -1;
    }

    private static final class SpeakerResult {
        private static final String FINAL_TAG = "final_result";
        private static final String PARTIAL_TAG = "partial_result";
        private static final String NLU_TAG = "nlu_result";

        @SerializedName("desc")
        String desc;
        @SerializedName("result_type")
        String resultType;
        @SerializedName("error")
        int error = -1;
        @SerializedName("sub_error")
        int subError = -1;
        @SerializedName("results_recognition")
        String[] results;

        boolean hasError() {
            return error != ERROR_NONE;
        }

        boolean isFinalResult() {
            return FINAL_TAG.equals(resultType);
        }

        boolean isPartialResult() {
            return PARTIAL_TAG.equals(resultType);
        }

        boolean isNluResult() {
            return NLU_TAG.equals(resultType);
        }
    }
}