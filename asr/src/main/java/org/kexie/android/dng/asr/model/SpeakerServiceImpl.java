package org.kexie.android.dng.asr.model;

import android.content.Context;

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

    private final static int ERROR_NONE = 0;
    private static final int backTrackInMs = 1500;

    private EventManager weakUp;
    private EventManager asr;

    private MutableLiveData<Status> currentStatus = new MutableLiveData<>(Status.NONE);
    private PublishSubject<Integer> currentVolume = PublishSubject.create();
    private PublishSubject<String> finalResult = PublishSubject.create();
    private PublishSubject<String> partialResult = PublishSubject.create();

    private Gson gson = new Gson();

    private final CopyOnWriteArrayList<LifecycleOnAwakeCallbackWrapper> onAwakeListeners =
            new CopyOnWriteArrayList<>();

    @Override
    public void init(Context context)
    {
        asr = EventManagerFactory.create(context, "asr");
        weakUp = EventManagerFactory.create(context, "weakUp");
        initAwake();
        initAsr();
    }

    private void initAwake()
    {
        weakUp.send(SpeechConstant.WAKEUP_START,
                gson.toJson(Collections.singletonMap(SpeechConstant.WP_WORDS_FILE,
                        "assets:///WakeUp.bin")),
                null, 0, 0);
        Map<String, EventListener> actions = new ArrayMap<String, EventListener>()
        {
            {
                put(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS,
                        (name, params, data, offset, length) -> {
                            AwakeResult awakeResult = gson.fromJson(params, AwakeResult.class);
                            for (OnAwakeCallback callback : onAwakeListeners)
                            {
                                if (callback.OnHandleAwake(awakeResult.hasError() ? null
                                        : awakeResult.word))
                                {
                                    return;
                                }
                            }
                        });
                put(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS,
                        (name, params, data, offset, length) -> {
                            AwakeResult awakeResult = gson.fromJson(params, AwakeResult.class);

                        });
            }
        };
        weakUp.registerListener((name, params, data, offset, length) -> {
            EventListener eventListener = actions.get(name);
            if (eventListener != null)
            {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });
    }

    private void initAsr()
    {
        Map<String, EventListener> actions = new ArrayMap<String, EventListener>()
        {
            {
                put(SpeechConstant.CALLBACK_EVENT_ASR_READY,
                        (name, params, data, offset, length) -> {
                            // 引擎准备就绪，可以开始说话
                            currentStatus.setValue(Status.READY);
                        });

                put(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN,
                        (name, params, data, offset, length) -> {
                            // 检测到用户的已经开始说话

                        });

                put(SpeechConstant.CALLBACK_EVENT_ASR_END,
                        (name, params, data, offset, length) -> {
                            // 检测到用户的已经停止说话

                        });

                put(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                        (name, params, data, offset, length) -> {
                            // 临时识别结果, 长语音模式需要从此消息中取出结果
                            SpeakerResult speakerResult = gson.fromJson(params, SpeakerResult.class);
                            if (speakerResult.isFinalResult())
                            {

                            }else if (speakerResult.isPartialResult())
                            {

                            }else if (speakerResult.isNluResult())
                            {

                            }
                        });

                put(SpeechConstant.CALLBACK_EVENT_ASR_FINISH,
                        (name, params, data, offset, length) -> {
                            // 识别结束， 最终识别结果或可能的错误
                            SpeakerResult speakerResult = gson.fromJson(params, SpeakerResult.class);

                        });

                put(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME,
                        (name, params, data, offset, length) -> {
                            // 实时音量
                            VolumeResult volumeResult = gson.fromJson(params, VolumeResult.class);
                            currentVolume.onNext(volumeResult.volumePercent);
                        });

                put(SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH,
                        (name, params, data, offset, length) -> {
                            // 长语音识别结束
                            SpeakerResult speakerResult = gson.fromJson(params, SpeakerResult.class);
                        });
            }
        };
        asr.registerListener((name, params, data, offset, length) -> {
            EventListener eventListener = actions.get(name);
            if (eventListener != null)
            {
                eventListener.onEvent(name, params, data, offset, length);
            }
        });
    }

    @Override
    public void removeOnAwakeCallback(OnAwakeCallback listener)
    {
        Iterator<LifecycleOnAwakeCallbackWrapper> iterator =
                onAwakeListeners.iterator();
        LifecycleOnAwakeCallbackWrapper callbackToRemove = null;
        while (iterator.hasNext())
        {
            LifecycleOnAwakeCallbackWrapper callback = iterator.next();
            if (callback.onAwakeCallback.equals(listener))
            {
                callbackToRemove = callback;
                break;
            }
        }
        if (callbackToRemove != null)
        {
            callbackToRemove.lifecycle.removeObserver(callbackToRemove);
            onAwakeListeners.remove(callbackToRemove);
        }
    }

    @Override
    public void start()
    {
        cancel();
        asr.send(SpeechConstant.ASR_START, gson.toJson(new ArrayMap<String, Object>()
        {
            {
                put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
                put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
                put(SpeechConstant.PID, 1536);
                put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);
            }
        }), null, 0, 0);
    }

    @Override
    public Observable<String> partialResult()
    {
        return partialResult;
    }

    @Override
    public Observable<String> finalResult()
    {
        return finalResult;
    }

    @Override
    public Observable<Integer> currentVolume()
    {
        return currentVolume;
    }

    @Override
    public LiveData<Status> currentStatus()
    {
        return currentStatus;
    }

    private void cancel()
    {
        asr.send(SpeechConstant.ASR_CANCEL,
                "{}", null, 0, 0);
    }

    @Override
    public void addOnAwakeCallback(LifecycleOwner owner, OnAwakeCallback listener)
    {
        Lifecycle lifecycle = owner.getLifecycle();
        if (lifecycle.getCurrentState() == Lifecycle.State.DESTROYED)
        {
            return;
        }
        LifecycleOnAwakeCallbackWrapper wrapper = new LifecycleOnAwakeCallbackWrapper(lifecycle, listener);
        lifecycle.addObserver(wrapper);
        onAwakeListeners.add(0, wrapper);
    }

    private final class LifecycleOnAwakeCallbackWrapper
            implements OnAwakeCallback,
            LifecycleEventObserver
    {
        private final Lifecycle lifecycle;
        private final OnAwakeCallback onAwakeCallback;

        private LifecycleOnAwakeCallbackWrapper(Lifecycle lifecycle,
                                                OnAwakeCallback onAwakeCallback)
        {
            this.lifecycle = lifecycle;
            this.onAwakeCallback = onAwakeCallback;
            lifecycle.addObserver(this);
        }

        @Override
        public boolean OnHandleAwake(String text)
        {
            if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            {
                return onAwakeCallback.OnHandleAwake(text);
            }
            return false;
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source,
                                   @NonNull Lifecycle.Event event)
        {
            if (event == Lifecycle.Event.ON_DESTROY)
            {
                synchronized (onAwakeListeners)
                {
                    lifecycle.removeObserver(this);
                    onAwakeListeners.remove(this);
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
            return errorCode != ERROR_NONE;
        }
    }

    private static final class VolumeResult
    {
        @SerializedName("currentVolume-percent")
        private int volumePercent = -1;
        @SerializedName("currentVolume")
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
