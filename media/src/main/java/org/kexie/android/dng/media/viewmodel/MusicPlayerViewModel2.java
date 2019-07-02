package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;

import com.blankj.utilcode.util.FileUtils;
import com.orhanobut.logger.Logger;

import org.kexie.android.danmakux.converter.LyricParser;
import org.kexie.android.danmakux.model.Lyric;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.model.MediaInfoLoader;
import org.kexie.android.dng.media.viewmodel.beans.MusicDetail;
import org.kexie.android.dng.media.widget.MusicQuickAdapter;
import org.kexie.android.dng.player.media.music.IjkMusicPlayer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java8.util.Optional;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

public class MusicPlayerViewModel2
        extends AndroidViewModel {

    public IjkMusicPlayer musicPlayer;
    public MutableLiveData<MusicDetail> details = new MutableLiveData<>();
    public MutableLiveData<Integer> volume = new MutableLiveData<>();
    public MutableLiveData<List<Lyric>> lyrics = new MutableLiveData<>();
    public GenericQuickAdapter<MusicDetail> adapter = new MusicQuickAdapter();

    private Lifecycle lifecycle;
    private AudioManager audioManager;

    private final class VolumeObserver implements Observer<Float>,
            LifecycleEventObserver {

        private final LiveData<Float> liveData;

        private VolumeObserver(LiveData<Float> liveData) {
            this.liveData = liveData;
        }

        @Override
        public void onChanged(Float value) {
            float percent = MathUtils.clamp(value, 0, 1f);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (maxVolume * percent), 0);
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                liveData.removeObserver(this);
            }
        }
    }

    public MusicPlayerViewModel2(@NonNull Application application, Lifecycle lifecycle) {
        super(application);
        musicPlayer = IjkMusicPlayer.newInstance(application, lifecycle);
        audioManager = (AudioManager) application
                .getSystemService(Context.AUDIO_SERVICE);
        this.lifecycle = lifecycle;
        LiveData<Float> liveData = Transformations.map(volume, input -> (float) input / 100f);
        liveData.observeForever(new VolumeObserver(liveData));
        initMusicList();
    }

    public void play(String path) {
        Observable<Boolean> prepared = musicPlayer.onSourcePrepared();
        musicPlayer.setNewSource(path);
        Observable<List<Lyric>> lrc = Observable.just(path)
                .observeOn(Schedulers.io())
                .map(File::new)
                .map(file -> {
                    String noExt = FileUtils.getFileNameNoExtension(file);
                    File[] files = file.getParentFile()
                            .listFiles(pathname -> {
                                String ext = FileUtils.getFileExtension(pathname);
                                String noExt2 = FileUtils.getFileNameNoExtension(pathname);
                                return ext.equalsIgnoreCase("lrc")
                                        && noExt.equals(noExt2);
                            });
                    return files != null && files.length >= 1
                            ? Optional.of(files[0])
                            : Optional.<File>empty();
                })
                .map(fileOptional -> fileOptional.isPresent()
                        ? LyricParser.loadFile(fileOptional.get())
                        : Collections.emptyList());
        Observable.zip(prepared, lrc, (aBoolean, lrcData) -> lrcData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(Logger::d)
                .as(autoDisposable(from(lifecycle, Lifecycle.Event.ON_PAUSE)))
                .subscribe(lyricData -> lyrics.setValue(lyricData));
    }

    private void initMusicList() {
        Single.<Context>just(getApplication())
                .observeOn(Schedulers.io())
                .map(MediaInfoLoader::getMusicInfos)
                .map(mediaInfos -> StreamSupport.stream(mediaInfos)
                        .map(mediaInfo -> new MusicDetail(
                                mediaInfo.uri,
                                mediaInfo.drawable,
                                mediaInfo.title,
                                mediaInfo.singer))
                        .collect(Collectors.toList()))
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(lifecycle, Lifecycle.Event.ON_DESTROY)))
                .subscribe(musicDetails -> adapter.setNewData(musicDetails));
    }

    public int getVolume() {
        float max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return Math.min(100, Math.round(current / max * 100));
    }

    public static String getProgressTime(int msecTotal) {
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format(Locale.CHINA, "%02d:%02d", minute, second);
    }
}
