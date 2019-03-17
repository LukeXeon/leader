package org.kexie.android.dng.common.model;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface TTSService extends IProvider
{
    void send(String text);

    void stop();
}
