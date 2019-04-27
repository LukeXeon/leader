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
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.TTSService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


@Route(path = PR.ai.tts_service)
public class TTSServiceImpl
        implements TTSService,
        ServiceConnection {

    private static final int MAX_QUEUE_SIZE = 10;

    private Messenger remote;

    private Context context;

    private Queue<String> queue;

    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        remote = new Messenger(service);
        if (queue != null) {
            Message message = obtainMessage(TTSRemoteService.ACTION_SEND);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(TTSRemoteService.LIST_SEND_KEY, new ArrayList<>(queue));
            queue = null;
            try {
                remote.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Logger.d("tts link");
    }

    @Override
    public synchronized void onServiceDisconnected(ComponentName name) {
        remote = null;
        queue = null;
        rebind();
        Logger.d("tts unlink");
    }

    private void rebind() {
        Intent intent = new Intent(context, TTSRemoteService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void init(Context context) {
        this.context = context.getApplicationContext();
        rebind();
    }

    @Override
    public synchronized void send(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (remote == null) {
            if (queue == null) {
                queue = new LinkedList<>();
            }
            if (queue.size() > MAX_QUEUE_SIZE) {
                queue.poll();
            }
            queue.add(text);
            return;
        }
        Message message = obtainMessage(TTSRemoteService.ACTION_SEND);
        Bundle bundle = new Bundle();
        bundle.putString(TTSRemoteService.SINGLE_SEND_KEY, text);
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
        queue = null;
        Message message = obtainMessage(TTSRemoteService.ACTION_STOP);
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
