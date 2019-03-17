package org.kexie.android.dng.ai.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.model.TTSService;

@Route(path = PR.ai.tts_service)
public class TTSServiceImpl implements TTSService
{
    private SpeechSynthesizer speechSynthesizer;

    private HandlerThread worker = new HandlerThread(toString());
    private Handler handler;

    @Override
    public void init(Context context)
    {
        worker.start();
        handler = new Handler(worker.getLooper());
        handler.post(() -> {
            speechSynthesizer = SpeechSynthesizer.getInstance();
            speechSynthesizer.setContext(context);
            speechSynthesizer.setAppId(getMetadata(context, R.string.bd_app_id));
            speechSynthesizer.setApiKey(getMetadata(context, R.string.bd_api_key),
                    getMetadata(context, R.string.bd_secret_key));
            // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "2");
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
            Logger.d(result + "init " + getClass().getSimpleName());
        });
    }

    private static String getMetadata(Context context, int id)
    {
        try
        {
            ApplicationInfo applicationInfo = context
                    .getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Object object = applicationInfo.metaData.get(context.getString(id));
            Logger.d(object);
            return String.valueOf(object);
        } catch (PackageManager.NameNotFoundException e)
        {
            throw new AssertionError(e);
        }
    }

    @Override
    public void send(String text)
    {
        handler.post(() -> {
            Logger.d(text);
            speechSynthesizer.speak(text);
        });
    }

    @Override
    public void stop()
    {
        handler.post(speechSynthesizer::stop);
    }
}
