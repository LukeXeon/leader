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

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.SpeakerService;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

@Route(path = PR.asr.service)
public class SpeakerServiceImpl implements SpeakerService
{

    private static final int sErrorNone = 0;
    //private static final int sBackTrackInMs = 1500;
    private static final int sNormalVID = 1536;
    private static final String sWeakUpBin = "assets:///WakeUp.bin";

    private EventManager mAsrManager;
    private EventManager mWeakUpManager;
    private Gson mGson = new Gson();
    private final CopyOnWriteArrayList<LifecycleOnWakeUpCallbackWrapper>
            mOnWeakUpListeners = new CopyOnWriteArrayList<>();
    private MutableLiveData<Status> mStatus = new MutableLiveData<>(Status.Initialization);
    private MutableLiveData<Integer> mCurrentVolume = new MutableLiveData<>(0);
    private PublishSubject<String> mPartialResult = PublishSubject.create();
    private PublishSubject<String> mFinalResult = PublishSubject.create();

    @Override
    public void init(Context context)
    {
        mAsrManager = EventManagerFactory.create(context, "asr");
        mWeakUpManager = EventManagerFactory.create(context, "weakUp");
        Map<String, EventListener> eventHandlers = new ArrayMap<String, EventListener>()
        {
            {
                // 引擎准备就绪，可以开始说话
                put(SpeechConstant.CALLBACK_EVENT_ASR_READY,
                        (name, params, data, offset, length)
                                -> mStatus.setValue(Status.Speaking));
                // 检测到用户的已经停止说话
                put(SpeechConstant.CALLBACK_EVENT_ASR_END,
                        (name, params, data, offset, length) -> {
                            mStatus.setValue(Status.Recognition);
                            mCurrentVolume.setValue(0);
                        });
                // 最终结果
                put(SpeechConstant.CALLBACK_EVENT_ASR_FINISH,
                        (name, params, data, offset, length) -> {
                            SpeakerResult speakerResult
                                    = mGson.fromJson(params, SpeakerResult.class);
                            if (speakerResult.results != null
                                    && speakerResult.results.length != 0
                                    && speakerResult.isFinalResult())
                            {
                                mFinalResult.onNext(speakerResult.results[0]);
                            }
                            mStatus.setValue(Status.Idle);
                        });
                // 临时识别结果, 长语音模式需要从此消息中取出结果
                put(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                        (name, params, data, offset, length) -> {
                            SpeakerResult speakerResult
                                    = mGson.fromJson(params, SpeakerResult.class);
                            if (speakerResult.results != null
                                    && speakerResult.results.length != 0
                                    && speakerResult.isPartialResult())
                            {
                                mPartialResult.onNext(speakerResult.results[0]);
                            }
                        });
                // 实时音量
                put(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME,
                        (name, params, data, offset, length) -> {
                            VolumeResult volumeResult
                                    = mGson.fromJson(params, VolumeResult.class);
                            mCurrentVolume.setValue(volumeResult.volumePercent);
                        });
            }
        };
        mAsrManager.registerListener((name, params, data, offset, length) -> {
            EventListener eventListener = eventHandlers.get(name);
            if (eventListener != null)
            {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });
        mWeakUpManager.registerListener((name, params, data, offset, length) -> {
            if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name))
            {
                AwakeResult awakeResult = mGson.fromJson(params, AwakeResult.class);
                for (OnWakeUpCallback callback : mOnWeakUpListeners)
                {
                    if (callback.handleWeakUp(awakeResult.hasError() ? null
                            : awakeResult.word))
                    {
                        return;
                    }
                }
            }
        });
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
                mGson.toJson(Collections
                        .singletonMap(SpeechConstant.WP_WORDS_FILE, sWeakUpBin)),
                null, 0, 0);

    }

    @Override
    public boolean start()
    {
        if (!Looper.getMainLooper().equals(Looper.myLooper()))
        {
            throw new AssertionError("must run at main thread");
        }
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
    public void removeOnWeakUpCallback(OnWakeUpCallback listener)
    {
        Iterator<LifecycleOnWakeUpCallbackWrapper> iterator =
                mOnWeakUpListeners.iterator();
        LifecycleOnWakeUpCallbackWrapper callbackToRemove = null;
        while (iterator.hasNext())
        {
            LifecycleOnWakeUpCallbackWrapper callback = iterator.next();
            if (callback.onWakeUpCallback.equals(listener))
            {
                callbackToRemove = callback;
                break;
            }
        }
        if (callbackToRemove != null)
        {
            callbackToRemove.lifecycle.removeObserver(callbackToRemove);
            mOnWeakUpListeners.remove(callbackToRemove);
        }
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
    public Observable<String> getPartialResult()
    {
        return mPartialResult;
    }

    @Override
    public Observable<String> getFinalResult()
    {
        return mFinalResult;
    }

    @Override
    public void addOnWeakUpCallback(LifecycleOwner owner, OnWakeUpCallback listener)
    {
        Lifecycle lifecycle = owner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED)
        {
            return;
        }
        LifecycleOnWakeUpCallbackWrapper wrapper
                = new LifecycleOnWakeUpCallbackWrapper(lifecycle, listener);
        lifecycle.addObserver(wrapper);
        mOnWeakUpListeners.add(0, wrapper);
    }

    private final class LifecycleOnWakeUpCallbackWrapper
            implements OnWakeUpCallback,
            LifecycleEventObserver
    {
        private final Lifecycle lifecycle;
        private final OnWakeUpCallback onWakeUpCallback;

        private LifecycleOnWakeUpCallbackWrapper(Lifecycle lifecycle,
                                                 OnWakeUpCallback onWakeUpCallback)
        {
            this.lifecycle = lifecycle;
            this.onWakeUpCallback = onWakeUpCallback;
            lifecycle.addObserver(this);
        }

        @Override
        public boolean handleWeakUp(String text)
        {
            if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            {
                return onWakeUpCallback.handleWeakUp(text);
            }
            return false;
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source,
                                   @NonNull Lifecycle.Event event)
        {
            if (event == Lifecycle.Event.ON_DESTROY)
            {
                synchronized (mOnWeakUpListeners)
                {
                    lifecycle.removeObserver(this);
                    mOnWeakUpListeners.remove(this);
                }
            }
        }
    }
    
    private static final class AwakeResult
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
