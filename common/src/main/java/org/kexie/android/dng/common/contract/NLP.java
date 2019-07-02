package org.kexie.android.dng.common.contract;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public interface NLP extends IProvider {
    Object NO_OP = null;

    @Nullable
    @WorkerThread
    Object process(String text);

}
