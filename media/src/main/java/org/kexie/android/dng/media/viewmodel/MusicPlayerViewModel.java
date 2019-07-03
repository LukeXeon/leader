package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;

import com.blankj.utilcode.util.FileUtils;

import org.kexie.android.danmakux.converter.LyricParser;
import org.kexie.android.danmakux.model.Lyric;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.model.MediaInfoLoader;
import org.kexie.android.dng.media.model.beans.MusicInfo;
import org.kexie.android.dng.media.viewmodel.beans.MusicDetail;
import org.kexie.android.dng.media.widget.MusicQuickAdapter;
import org.kexie.android.dng.player.media.music.IjkMusicViewModel;

import java.io.File;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class MusicPlayerViewModel extends IjkMusicViewModel {

    public MutableLiveData<MusicDetail> current = new MutableLiveData<>();
    public MutableLiveData<Integer> volumePercent = new MutableLiveData<>();
    public MutableLiveData<List<Lyric>> lyricSet = new MutableLiveData<>();
    public GenericQuickAdapter<MusicDetail> details = new MusicQuickAdapter();

    private HandlerThread workerThread;
    private Handler worker;

    public MusicPlayerViewModel(@NonNull Application application) {
        super(application);
        workerThread = new HandlerThread("music");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        volumePercent.observeForever(value -> {
            float percent = MathUtils.clamp(value, 0, 1f);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (maxVolume * percent), 0);
        });
        initMusicList();
    }

    @Override
    public void setNewSource(String path) {
        super.setNewSource(path);
        worker.post(() -> {
            File file = new File(path);
            String noExt = FileUtils.getFileNameNoExtension(file);
            File[] files = file.getParentFile()
                    .listFiles(pathname -> {
                        String ext = FileUtils.getFileExtension(pathname);
                        String noExt2 = FileUtils.getFileNameNoExtension(pathname);
                        return ext.equalsIgnoreCase("lrc")
                                && noExt.equals(noExt2);
                    });
            if (files != null && files.length >= 1) {
                List<Lyric> lyrics = LyricParser.loadFile(files[0]);
                lyricSet.postValue(lyrics);
            } else {
                lyricSet.postValue(Collections.emptyList());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
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
