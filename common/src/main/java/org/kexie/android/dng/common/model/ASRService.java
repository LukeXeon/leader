package org.kexie.android.dng.common.model;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.lifecycle.LiveData;
import io.reactivex.Observable;

public interface ASRService extends IProvider
{
    enum Status
    {
        //Initialization-->Idle-->Prepare-->Speaking-->Recognition-->Idle
        Initialization,//初始化中
        Idle,//空闲中
        Prepare,//准备中
        Speaking,//正在听
        Recognition;//识别中
    }

    LiveData<Status> getCurrentStatus();

    LiveData<Integer> getCurrentVolume();

    Observable<String> getWeakUpResult();

    Observable<String> getPartialResult();

    Observable<String> getFinalResult();

    boolean beginTransaction();

    void endTransaction();
}
