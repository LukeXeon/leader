package org.kexie.android.dng.common.contract;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.annotation.AnyThread;

public interface TTS extends IProvider
{
    @AnyThread
    void send(String text);

    @AnyThread
    void stop();
}
