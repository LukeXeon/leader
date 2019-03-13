package org.kexie.android.dng.common.model;

import com.alibaba.android.arouter.facade.template.IProvider;

import androidx.lifecycle.LifecycleOwner;

public interface SpeakerService extends IProvider
{
    interface OnAwakeCallback
    {
        boolean OnHandleAwake(String text);
    }

    void addOnAwakeCallback(LifecycleOwner owner, OnAwakeCallback listener);

    void removeOnAwakeCallback(OnAwakeCallback listener);

    void listening();

}
