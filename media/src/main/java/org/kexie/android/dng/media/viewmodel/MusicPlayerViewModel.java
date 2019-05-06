package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;

import com.blankj.utilcode.util.FileUtils;
import com.zlm.hp.lyrics.LyricsReader;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.model.MediaInfoLoader;
import org.kexie.android.dng.media.viewmodel.entity.MusicDetails;
import org.kexie.android.dng.media.widget.MusicQuickAdapter;
import org.kexie.android.dng.player.media.music.IjkMusicPlayer;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
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

public class MusicPlayerViewModel
        extends AndroidViewModel {

    public IjkMusicPlayer musicPlayer;
    public MutableLiveData<MusicDetails> details = new MutableLiveData<>();
    public MutableLiveData<Integer> volume = new MutableLiveData<>();
    public GenericQuickAdapter<MusicDetails> adapter = new MusicQuickAdapter();

    private Lifecycle lifecycle;
    private AudioManager audioManager;
    private Observer<Float> observer = new Observer<Float>() {
        @Override
        public void onChanged(Float value) {
            float percent = MathUtils.clamp(value, 0, 1f);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (maxVolume * percent), 0);
        }
    };

    public MusicPlayerViewModel(@NonNull Application application, Lifecycle lifecycle) {
        super(application);
        musicPlayer = IjkMusicPlayer.newInstance(application, lifecycle);
        audioManager = (AudioManager) application
                .getSystemService(Context.AUDIO_SERVICE);
        this.lifecycle = lifecycle;

        LiveData<Float> liveData = Transformations.map(volume, input -> (float) input / 100f);
        liveData.observeForever(observer);
        lifecycle.addObserver((LifecycleEventObserver) (source, event) -> {
            if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                liveData.removeObserver(observer);
            }
        });

        initMusicList();
    }

    public Observable<Optional<LyricsReader>> playNewTask(String path) {
        Observable<Boolean> prepared = musicPlayer.onSourcePrepared();
        musicPlayer.setNewSource(path);
        Observable<Optional<LyricsReader>> lrc = Observable.just(path)
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
                    return files.length >= 1
                            ? Optional.of(files[0])
                            : Optional.<File>empty();
                })
                .map(fileOptional -> {
                    if (fileOptional.isPresent()) {
                        File file = fileOptional.get();
                        LyricsReader reader = new LyricsReader();
                        try {
                            reader.loadLrc(file);
                            return Optional.of(reader);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return Optional.<LyricsReader>empty();
                })
                .observeOn(AndroidSchedulers.mainThread());
        return Observable.zip(prepared, lrc, (aBoolean, readerOptional) -> readerOptional);
    }

    private void initMusicList() {
        Single.<Context>just(getApplication())
                .observeOn(Schedulers.io())
                .map(MediaInfoLoader::getMusicInfos)
                .map(mediaInfos -> StreamSupport.stream(mediaInfos)
                        .map(mediaInfo -> new MusicDetails(
                                mediaInfo.uri,
                                mediaInfo.drawable,
                                mediaInfo.title,
                                mediaInfo.singer))
                        .collect(Collectors.toList()))
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(lifecycle, Lifecycle.Event.ON_DESTROY)))
                .subscribe(musicDetails -> adapter.setNewData(musicDetails));
    }
}
