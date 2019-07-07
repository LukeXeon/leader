package org.kexie.android.dng.common.contract;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.template.IProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ASR extends IProvider {
    int WEAK_UP_BACK_TRACK_IN_MS = 1500;
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

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INITIALIZATION, IDLE, PREPARE, SPEAKING, RECOGNITION})
    @interface Status {
    }

    void addHandler(@NonNull Handler handler);

    void removeHandler(@NonNull Handler handler);

    @Status
    int getStatus();

    @IntRange(from = 0, to = 100)
    int getVolume();

    void begin(int ms);

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

        @MainThread
        void onCancel();
    }

    abstract class WeakUpHandler implements Handler {

        @Override
        public void onCancel() {

        }

        @Override
        public final void onStatusUpdate(int status) {

        }

        @Override
        public final void onVolumeUpdate(int value) {

        }

        @Override
        public abstract void onWeakUp(@NonNull String text);

        @Override
        public final void onResult(boolean isFinal, @NonNull String text) {

        }
    }
}
