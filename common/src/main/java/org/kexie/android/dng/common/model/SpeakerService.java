package org.kexie.android.dng.common.model;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import io.reactivex.Observable;

public interface SpeakerService extends IProvider
{
    enum Status
    {
        NONE,//无状态
        READY,//准备好
        SPEAKING,//用户说话中
        RECOGNITION,//识别中
        FINISHED,//完成
        LONG_SPEECH_FINISHED,//长语音完成
        STOPPED,//停止
    }

    interface OnAwakeCallback
    {
        boolean OnHandleAwake(String text);
    }

    void addOnAwakeCallback(LifecycleOwner owner, OnAwakeCallback listener);

    void removeOnAwakeCallback(OnAwakeCallback listener);

    void start();

    Observable<String> partialResult();

    Observable<String> finalResult();

    Observable<Integer> currentVolume();

    LiveData<Status> currentStatus();

}
