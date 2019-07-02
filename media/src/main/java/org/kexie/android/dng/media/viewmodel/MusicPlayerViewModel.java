package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.kexie.android.danmakux.model.Lyric;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.model.MediaInfoLoader;
import org.kexie.android.dng.media.model.entity.MusicInfo;
import org.kexie.android.dng.media.viewmodel.beans.MusicDetail;
import org.kexie.android.dng.media.widget.MusicQuickAdapter;
import org.kexie.android.dng.player.media.music.IjkMusicPlayer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class MusicPlayerViewModel extends AndroidViewModel {

    public IjkMusicPlayer musicPlayer;
    public MutableLiveData<MusicDetail> current = new MutableLiveData<>();
    public MutableLiveData<Integer> volume = new MutableLiveData<>();
    public MutableLiveData<List<Lyric>> lyrics = new MutableLiveData<>();
    public GenericQuickAdapter<MusicDetail> details = new MusicQuickAdapter();

    private AudioManager audioManager;
    private HandlerThread workerThread;
    private Handler worker;
    private Handler main;

    public MusicPlayerViewModel(@NonNull Application application) {
        super(application);
        workerThread = new HandlerThread("music");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        audioManager = (AudioManager) application
                .getSystemService(Context.AUDIO_SERVICE);
        volume.observeForever(value -> {
            float percent = MathUtils.clamp(value, 0, 1f);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (maxVolume * percent), 0);
        });
        main = new Handler(Looper.getMainLooper());
        initMusicList();
    }

    public void install(Lifecycle lifecycle) {

    }

    public void play(String path) {

    }

    @Override
    protected void onCleared() {
        musicPlayer.destroy();
        workerThread.quit();
        worker.removeCallbacksAndMessages(null);
        main.removeCallbacksAndMessages(null);
    }

    private void initMusicList() {
        worker.post(()-> {
            List<MusicInfo> musicList = MediaInfoLoader.getMusicInfos(getApplication());
            List<MusicDetail> musicDetails = StreamSupport.stream(musicList)
                    .map(mediaInfo -> new MusicDetail(
                            mediaInfo.uri,
                            mediaInfo.drawable,
                            mediaInfo.title,
                            mediaInfo.singer))
                    .collect(Collectors.toList());
            main.post(() -> details.setNewData(musicDetails));
        });
    }

    public int getVolume() {
        float max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return Math.min(100, Math.round(current / max * 100));
    }
}
