package org.kexie.android.dng.common.contract;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

public interface LBS extends IProvider {

    @MainThread
    Session use();

    interface Session {

        @WorkerThread
        IPoint lastLocation() throws Exception;

        @MainThread
        void close();
    }

    interface IPoint {
        //Y
        double getLatitude();

        //x
        double getLongitude();
    }
}
