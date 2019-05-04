package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.blankj.utilcode.util.FileUtils;
import com.zlm.hp.lyrics.LyricsReader;

import org.kexie.android.dng.media.viewmodel.entity.MediaDetails;
import org.kexie.android.dng.player.media.music.IjkMusicPlayer;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java8.util.Optional;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MusicPlayerViewModel 
        extends AndroidViewModel 
        implements LifecycleObserver {

    private SharedPreferences sharedPreferences;
    private Disposable disposable;
    private IjkMusicPlayer player;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable positionUpdater = new Runnable() {
        @Override
        public void run() {
            position.setValue((int) player.position());
            handler.postDelayed(this, 500);
        }
    };

    public MutableLiveData<Integer> position = new MutableLiveData<>(0);
    public MutableLiveData<MediaDetails> mediaDetails = new MutableLiveData<>();
    public PublishSubject<Optional<LyricsReader>> onNewReader = PublishSubject.create();

    public MusicPlayerViewModel(@NonNull Application application) {
        super(application);
        player = IjkMusicPlayer.newInstance(application);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        dispose();
        handler.removeCallbacks(positionUpdater);
        player.pause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        player.resume();
        handler.removeCallbacks(positionUpdater);
        handler.post(positionUpdater);
    }

    public void play(String path) {
        dispose();
        Single<IMediaPlayer> objectSingle = Single.create(emitter -> {
            player.source(path);
            player.setOnCompletionListener(emitter::onSuccess);
        });
        disposable = Single.just(path)
                .observeOn(Schedulers.io())
                .map(File::new)
                .map(file -> {
                    String noExt = FileUtils.getFileNameNoExtension(file);
                    File[] files = file.getParentFile()
                            .listFiles(pathname -> {
                                String noExt2 = FileUtils.getFileNameNoExtension(pathname);
                                if (noExt.equals(noExt2)) {
                                    return FileUtils.getFileExtension(pathname)
                                            .equalsIgnoreCase("lrc");
                                }
                                return false;
                            });
                    return files.length < 1 ? Optional.<File>empty() : Optional.of(files[0]);
                })
                .map(file -> {
                    try {
                        if (file.isPresent()) {
                            LyricsReader reader = new LyricsReader();
                            reader.loadLrc(file.get());
                            return Optional.of(reader);
                        } else {
                            return Optional.<LyricsReader>empty();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<LyricsReader>empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(lyricsReader -> onNewReader.onNext(lyricsReader))
                .zipWith(objectSingle, (lyricsReader, player) -> new Object())
                .subscribe(o -> {
                    player.start();
                    handler.post(positionUpdater);
                });
    }

    public boolean playing() {
        return player.playing();
    }

    public long duration() {
        return player.duration();
    }

    public LiveData<Integer> sessionId() {
        return player.sessionId();
    }

    public void seekTo(long ms) {
        player.seekTo(ms);
    }

    private void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    protected void onCleared() {
        dispose();
        player.destroy();
        player = null;
    }
}
