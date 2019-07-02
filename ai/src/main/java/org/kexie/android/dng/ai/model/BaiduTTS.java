package org.kexie.android.dng.ai.model;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.contract.TTS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import androidx.annotation.Nullable;


@Route(path = Module.Ai.tts)
public class BaiduTTS
        implements TTS,
        ServiceConnection {

    private static final int MAX_QUEUE_SIZE = 10;

    private Messenger remote;

    private Context context;

    private Queue<String> queue;

    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        remote = new Messenger(service);
        if (queue != null) {
            Message message = obtainMessage(RemoteService.ACTION_SEND);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(RemoteService.LIST_SEND_KEY, new ArrayList<>(queue));
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
        Intent intent = new Intent(context, RemoteService.class);
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
        Message message = obtainMessage(RemoteService.ACTION_SEND);
        Bundle bundle = new Bundle();
        bundle.putString(RemoteService.SINGLE_SEND_KEY, text);
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
        Message message = obtainMessage(RemoteService.ACTION_STOP);
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

    public static class RemoteService extends Service {

        public static final int ACTION_STOP = 10001;
        public static final int ACTION_SEND = 10002;
        public static final String SINGLE_SEND_KEY = "single_send_key";
        public static final String LIST_SEND_KEY = "list_send_key";

        private Messenger messenger;

        @Override
        public void onCreate() {
            super.onCreate();
            SpeechSynthesizer speechSynthesizer = SpeechSynthesizer.getInstance();
            Context context = getApplicationContext();
            speechSynthesizer.setContext(context);
            speechSynthesizer.setAppId(getMetadata(context, R.string.bd_app_id));
            speechSynthesizer.setApiKey(getMetadata(context, R.string.bd_api_key),
                    getMetadata(context, R.string.bd_secret_key));
            // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
            // 设置合成的音量，0-9 ，默认 5
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
            // 设置合成的语速，0-9 ，默认 5
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
            // 设置合成的语调，0-9 ，默认 5
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE,
                    SpeechSynthesizer.MIX_MODE_DEFAULT);

            // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
            // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            speechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);
            int result = speechSynthesizer.initTts(TtsMode.ONLINE);
            messenger = new Messenger(new Handler(Looper.getMainLooper(), msg -> {
                switch (msg.what) {
                    case ACTION_SEND: {
                        Bundle bundle = msg.getData();
                        String s = bundle.getString(SINGLE_SEND_KEY);
                        if (!TextUtils.isEmpty(s)) {
                            speechSynthesizer.speak(s);
                        } else {
                            ArrayList<String> list = bundle.getStringArrayList(LIST_SEND_KEY);
                            if (list != null && !list.isEmpty()) {
                                for (String text : list) {
                                    if (!TextUtils.isEmpty(text)) {
                                        speechSynthesizer.speak(text);
                                    }
                                }
                            }
                        }
                        return true;
                    }
                    case ACTION_STOP: {
                        speechSynthesizer.stop();
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }));
            Logger.d(result + "init " + getClass().getSimpleName());
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return messenger.getBinder();
        }

        private static String getMetadata(Context context, int id) {
            try {
                ApplicationInfo applicationInfo = context
                        .getPackageManager()
                        .getApplicationInfo(context.getPackageName(),
                                PackageManager.GET_META_DATA);
                Object object = applicationInfo.metaData.get(context.getString(id));
                Logger.d(object);
                return String.valueOf(object);
            } catch (PackageManager.NameNotFoundException e) {
                throw new AssertionError(e);
            }
        }
    }
}
