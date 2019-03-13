package org.kexie.android.dng.asr.model;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.SpeakerService;

@Route(path = PR.asr.service)
public class SpeakerServiceImpl implements SpeakerService
{
    EventManager weakUp;
    EventManager asr;

    @Override
    public void init(Context context)
    {
        asr = EventManagerFactory.create(context, "asr");
        weakUp = EventManagerFactory.create(context, "weakUp");
    }
}
