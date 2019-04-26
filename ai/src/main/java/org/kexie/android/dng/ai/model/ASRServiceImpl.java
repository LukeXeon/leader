package org.kexie.android.dng.ai.model;

import android.content.Context;
import android.os.Looper;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.ASRService;

import java.util.Collections;
import java.util.Map;

import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

//多进程
@Route(path = PR.ai.asr_service,name = "语音识别服务")
public class ASRServiceImpl implements ASRService
{
    private static final int BACK_TRACK_IN_MS = 1500;
    private static final int ERROR_NONE = 0;
    private static final int NORMAL_VID = 1536;
    private static final String WEAK_UP_BIN = "assets:///WakeUp.bin";
    private static final String WEAK_UP_TEXT = "嘿领航员";

    private EventManager mEventManager;
    private Gson mGson = new Gson();
    private MutableLiveData<Status> mStatus = new MutableLiveData<>(Status.Initialization);
    private MutableLiveData<Integer> mCurrentVolume = new MutableLiveData<>(0);
    private Observable<String> mWeakUpResult;
    private Observable<String> mPartialResult;
    private Observable<String> mFinalResult;

    @Override
    public void init(Context context) {
        context = context.getApplicationContext();
        Map<String, EventListener> eventHandlers = new ArrayMap<>();
        // 引擎准备就绪，可以开始说话
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_READY,
                (name, params, data, offset, length) -> mStatus.setValue(Status.Speaking));
        // 检测到用户的已经停止说话
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_END,
                (name, params, data, offset, length) -> {
                    mStatus.setValue(Status.Recognition);
                    mCurrentVolume.setValue(0);
                });
        // 取消
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_CANCEL,
                (name, params, data, offset, length) -> {
                    mStatus.setValue(Status.Idle);
                    mCurrentVolume.setValue(0);
                });
        // 实时音量
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME,
                (name, params, data, offset, length) -> {
                    VolumeResult volumeResult
                            = mGson.fromJson(params, VolumeResult.class);
                    if (!Status.Speaking.equals(mStatus.getValue())) {
                        return;
                    }
                    mCurrentVolume.setValue(volumeResult.volumePercent);
                });
        // 引擎完成
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_FINISH,
                (name, params, data, offset, length) -> mStatus.setValue(Status.Idle));
        // 结果处理
        PublishSubject<String> push = PublishSubject.create();
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                (name, params, data, offset, length) -> push.onNext(params));
        Observable<SpeakerResult> result = push
                .doOnNext(Logger::d)
                .map(params -> mGson.fromJson(params, SpeakerResult.class))
                .filter(speakerResult -> !speakerResult.hasError())
                .filter(speakerResult -> speakerResult.results != null)
                .filter(speakerResult -> speakerResult.results.length != 0);
        // 临时识别结果, 长语音模式需要从此消息中取出结果
        mPartialResult = result.filter(SpeakerResult::isPartialResult)
                .map(speakerResult -> speakerResult.results[0]);
        // 最终结果
        mFinalResult = result.filter(SpeakerResult::isFinalResult)
                .map(speakerResult -> speakerResult.results[0]);

        // WeakUp事件
        Map<String, EventListener> eventHandlers2 = new ArrayMap<>();
        eventHandlers2.put(SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR,
                (name, params, data, offset, length) -> Logger.d(params));
        PublishSubject<String> push2 = PublishSubject.create();
        eventHandlers2.put(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS,
                (name, params, data, offset, length) -> push2.onNext(params));
        mWeakUpResult = push2
                .doOnNext(Logger::d)
                .map(params -> mGson.fromJson(params, WeakUpResult.class))
                .filter(weakUpResult -> !weakUpResult.hasError())
                .filter(weakUpResult -> WEAK_UP_TEXT.equals(weakUpResult.word))
                .map(weakUpResult -> weakUpResult.word);

        // 异步初始化

        mEventManager = EventManagerFactory.create(context, "asr", true);
        mEventManager.registerListener((name, params, data, offset, length) -> {
            EventListener eventListener = eventHandlers.get(name);
            if (eventListener != null) {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });
        EventManager mWeakUpManager = EventManagerFactory.create(context, "wp", true);
        mWeakUpManager.registerListener((name, params, data, offset, length) -> {
            Logger.d(name);
            if (Status.Initialization.equals(mStatus.getValue())
                    && SpeechConstant.CALLBACK_EVENT_WAKEUP_READY.equals(name)) {
                mStatus.setValue(Status.Idle);
                return;
            }
            EventListener eventListener = eventHandlers2.get(name);
            if (eventListener != null) {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });
        mWeakUpManager.send(SpeechConstant.WAKEUP_START,
                mGson.toJson(Collections.singletonMap(SpeechConstant.WP_WORDS_FILE, WEAK_UP_BIN)),
                null, 0, 0);
        Logger.d(getClass().getName() + " start");
    }

    @Override
    public boolean beginTransaction()
    {
        // 必须主线程
        assertMainThread();
        // 必须待机态
        if (!Status.Idle.equals(mStatus.getValue()))
        {
            return false;
        }
        mEventManager.send(SpeechConstant.ASR_START, mGson.toJson(
                new ArrayMap<String, Object>(4)
                {
                    {
                        put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
                        put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
                        put(SpeechConstant.PID, NORMAL_VID);
                        put(SpeechConstant.AUDIO_MILLS,
                                System.currentTimeMillis() - BACK_TRACK_IN_MS);
                    }
                }), null, 0, 0);
        mStatus.setValue(Status.Prepare);
        return true;
    }

    @Override
    public void endTransaction() {
        mEventManager.send(SpeechConstant.ASR_CANCEL,
                "{}", null, 0, 0);
    }

    private static void assertMainThread()
    {
        if (!Looper.getMainLooper().equals(Looper.myLooper()))
        {
            throw new AssertionError("must run at main thread");
        }
    }

    @Override
    public LiveData<Status> getCurrentStatus()
    {
        return mStatus;
    }

    @Override
    public LiveData<Integer> getCurrentVolume()
    {
        return mCurrentVolume;
    }

    @Override
    public Observable<String> getWeakUpResult()
    {
        return mWeakUpResult;
    }

    @Override
    public Observable<String> getPartialResult()
    {
        return mPartialResult;
    }

    @Override
    public Observable<String> getFinalResult()
    {
        return mFinalResult;
    }

    private static final class WeakUpResult
    {
        @SerializedName("word")
        private String word;
        @SerializedName("desc")
        private String desc;
        @SerializedName("error")
        private int errorCode;

        private boolean hasError()
        {
            return errorCode != ERROR_NONE;
        }
    }

    private static final class VolumeResult
    {
        @SerializedName("volume-percent")
        private int volumePercent = -1;
        @SerializedName("volume")
        private int volume = -1;
    }

    private static final class SpeakerResult
    {
        private static final String FINAL_TAG = "final_result";
        private static final String PARTIAL_TAG = "partial_result";
        private static final String NLU_TAG = "nlu_result";

        @SerializedName("desc")
        private String desc;
        @SerializedName("result_type")
        private String resultType;
        @SerializedName("error")
        private int error = -1;
        @SerializedName("sub_error")
        private int subError = -1;
        @SerializedName("results_recognition")
        private String[] results;

        @SuppressWarnings("All")
        private boolean hasError()
        {
            return error != ERROR_NONE;
        }

        private boolean isFinalResult()
        {
            return FINAL_TAG.equals(resultType);
        }

        private boolean isPartialResult()
        {
            return PARTIAL_TAG.equals(resultType);
        }

        private boolean isNluResult()
        {
            return NLU_TAG.equals(resultType);
        }
    }
}
