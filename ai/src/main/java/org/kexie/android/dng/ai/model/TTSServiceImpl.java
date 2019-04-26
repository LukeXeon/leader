package org.kexie.android.dng.ai.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.TTSService;

@Route(path = PR.ai.tts_service)
public class TTSServiceImpl implements TTSService {

    private Messenger remote;

    @Override
    public void init(Context context) {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, SpeechService.class);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized (TTSServiceImpl.class) {
                    remote = new Messenger(service);
                }
                Logger.d("tts link");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Logger.d("tts unlink");
            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public synchronized void send(String text) {
        if (remote == null) {
            return;
        }
        Message message = obtainMessage(SpeechService.ACTION_SEND);
        Bundle bundle = new Bundle();
        bundle.putString(SpeechService.SEND_KEY, text);
        message.setData(bundle);
        try {
            remote.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void stop() {
        if (remote == null) {
            return;
        }
        Message message = obtainMessage(SpeechService.ACTION_STOP);
        try {
            remote.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static Message obtainMessage(int what) {
        Message message = Message.obtain();
        message.what = what;
        return message;
    }
}
