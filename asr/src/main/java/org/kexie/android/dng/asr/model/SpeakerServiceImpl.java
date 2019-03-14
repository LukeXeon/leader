package org.kexie.android.dng.asr.model;

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
import org.kexie.android.dng.common.model.SpeakerService;

import java.util.Collections;
import java.util.Map;

import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;

@Route(path = PR.asr.service,name = "语音识别服务")
public class SpeakerServiceImpl implements SpeakerService
{
    //private static final int sBackTrackInMs = 1500;
    private static final int sErrorNone = 0;
    private static final int sNormalVID = 1536;
    private static final String sWeakUpBin = "assets:///WakeUp.bin";
    private static final String sWeakUpText = "嘿领航员";

    private EventManager mAsrManager;
    private EventManager mWeakUpManager;
    private Gson mGson = new Gson();
    private MutableLiveData<Status> mStatus = new MutableLiveData<>(Status.Initialization);
    private MutableLiveData<Integer> mCurrentVolume = new MutableLiveData<>(0);
    private Observable<String> mWeakUp;
    private Observable<String> mPartialResult;
    private Observable<String> mFinalResult;

    @Override
    public void init(Context context)
    {
        Logger.d(getClass().getName() + " start");

        mAsrManager = EventManagerFactory.create(context, "asr");
        mWeakUpManager = EventManagerFactory.create(context, "wp");

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
        // 实时音量
        eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME,
                (name, params, data, offset, length) -> {
                    VolumeResult volumeResult
                            = mGson.fromJson(params, VolumeResult.class);
                    mCurrentVolume.setValue(volumeResult.volumePercent);
                });
        // 临时识别结果, 长语音模式需要从此消息中取出结果
        mPartialResult = Observable.create(emitter ->
                eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                        (name, params, data, offset, length) -> {
                            SpeakerResult speakerResult
                                    = mGson.fromJson(params, SpeakerResult.class);
                            if (!speakerResult.hasError()
                                    && speakerResult.results != null
                                    && speakerResult.results.length != 0
                                    && speakerResult.isPartialResult())
                            {
                                emitter.onNext(speakerResult.results[0]);
                            }
                        }));
        // 最终结果
        mFinalResult = Observable.create(emitter ->
                eventHandlers.put(SpeechConstant.CALLBACK_EVENT_ASR_FINISH,
                        (name, params, data, offset, length) -> {
                            SpeakerResult speakerResult
                                    = mGson.fromJson(params, SpeakerResult.class);
                            if (!speakerResult.hasError()
                                    && speakerResult.results != null
                                    && speakerResult.results.length != 0
                                    && speakerResult.isFinalResult())
                            {
                                emitter.onNext(speakerResult.results[0]);
                            }
                            mStatus.setValue(Status.Idle);
                        }));

        mAsrManager.registerListener((name, params, data, offset, length) -> {
            EventListener eventListener = eventHandlers.get(name);
            if (eventListener != null)
            {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });

        // WeakUp事件
        Map<String, EventListener> eventHandlers2 = new ArrayMap<>();
        eventHandlers2.put(SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR,
                (name, params, data, offset, length) -> Logger.d(params));
        mWeakUp = Observable.create(emitter ->
                eventHandlers2.put(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS,
                        (name, params, data, offset, length) -> {
                            WeakUpResult weakUpResult
                                    = mGson.fromJson(params, WeakUpResult.class);
                            if (!weakUpResult.hasError() && sWeakUpText.equals(weakUpResult.word))
                            {
                                emitter.onNext(weakUpResult.word);
                            }
                        }));
        mWeakUpManager.registerListener((name, params, data, offset, length) -> {
            EventListener eventListener = eventHandlers2.get(name);
            if (eventListener != null)
            {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });

        // 初始化WeakUp
        mWeakUpManager.registerListener(new EventListener()
        {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length)
            {

                if (SpeechConstant.CALLBACK_EVENT_WAKEUP_READY.equals(name))
                {
                    mStatus.setValue(Status.Idle);
                    mWeakUpManager.unregisterListener(this);
                }
            }
        });
        mWeakUpManager.send(SpeechConstant.WAKEUP_START,
                mGson.toJson(Collections.singletonMap(SpeechConstant.WP_WORDS_FILE, sWeakUpBin)),
                null, 0, 0);
    }

    @Override
    public boolean start()
    {
        // 必须主线程
        if (!Looper.getMainLooper().equals(Looper.myLooper()))
        {
            throw new AssertionError("must run at main thread");
        }
        // 必须待机态
        if (!Status.Idle.equals(mStatus.getValue()))
        {
            return false;
        }
        mAsrManager.send(SpeechConstant.ASR_START, mGson.toJson(
                new ArrayMap<String, Object>(4)
                {
                    {
                        put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
                        put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
                        put(SpeechConstant.PID, sNormalVID);
                        //put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - sBackTrackInMs);
                    }
                }), null, 0, 0);
        mStatus.setValue(Status.Prepare);
        return true;
    }

    @Override
    public LiveData<Status> getStatus()
    {
        return mStatus;
    }

    @Override
    public LiveData<Integer> getCurrentVolume()
    {
        return mCurrentVolume;
    }

    @Override
    public Observable<String> getWeakUp()
    {
        return mWeakUp;
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
            return errorCode != sErrorNone;
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
            return error != sErrorNone;
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
