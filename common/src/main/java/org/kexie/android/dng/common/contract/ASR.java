package org.kexie.android.dng.common.contract;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

public interface ASR extends IProvider {
    //Initialization-->Idle-->Prepare-->Speaking-->Recognition-->Idle
    //初始化中
    int INITIALIZATION = 0;
    //空闲中
    int IDLE = 1;
    //准备中
    int PREPARE = 2;
    //正在听
    int SPEAKING = 3;
    //识别中
    int RECOGNITION = 4;

    @IntDef({INITIALIZATION, IDLE, PREPARE, SPEAKING, RECOGNITION})
    @interface Status {
    }

    void addHandler(@NonNull Handler handler);

    void removeHandler(@NonNull Handler handler);

    @Status
    int getStatus();

    @IntRange(from = 0, to = 100)
    int getVolume();

    boolean begin();

    void stop();

    interface Handler {
        @MainThread
        void onStatusUpdate(@Status int status);

        @MainThread
        void onVolumeUpdate(@IntRange(from = 0, to = 100) int value);

        @MainThread
        void onWeakUp(@NonNull String text);

        @MainThread
        void onResult(boolean isFinal, @NonNull String text);

    }
}
