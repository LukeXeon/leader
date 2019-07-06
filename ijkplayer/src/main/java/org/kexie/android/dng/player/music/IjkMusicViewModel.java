package org.kexie.android.dng.player.music;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.text.TextUtils;

import org.kexie.android.dng.common.util.LiveEvent;
import org.kexie.android.dng.player.BuildConfig;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkMusicViewModel
        extends AndroidViewModel
        implements LifecycleObserver {
    private static final int MIN_INTERVAL = 500;

    public final LiveEvent<Boolean> onComplete = new LiveEvent<>();
    public final MutableLiveData<Long> duration = new MutableLiveData<>();
    public final MutableLiveData<String> source = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    protected AudioManager audioManager;
    protected Handler main;

    private final LiveFft fft = new LiveFft();
    private final LivePosition position = new LivePosition();
    private long marker = 0;
    private long interval = MIN_INTERVAL;
    private IMediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private final Runnable positionUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                position.setValue(mediaPlayer.getCurrentPosition());
            }
            main.postDelayed(this, interval);
        }
    };
    private final IMediaPlayer.OnPreparedListener onPreparedListener
            = mediaPlayer -> {
        int id = mediaPlayer.getAudioSessionId();
        if (id != 0) {
            visualizer = new Visualizer(id);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(fft,
                    Math.round(Visualizer.getMaxCaptureRate() / 2f),
                    false,
                    true);
            visualizer.setEnabled(true);
        }
        main.post(positionUpdater);
        if (marker > 0) {
            mediaPlayer.seekTo(marker);
        }
        duration.setValue(mediaPlayer.getDuration());
    };
    private final IMediaPlayer.OnCompletionListener onCompletionListener
            = mediaPlayer -> {
        position.postValue(mediaPlayer.getDuration());
        onComplete.post(Boolean.TRUE);
    };

    public IjkMusicViewModel(@NonNull Application application) {
        super(application);
        audioManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public LiveData<byte[]> getFft() {
        return fft;
    }

    public LiveData<Long> getPosition() {
        return position;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setNewSource(String path) {
        if (mediaPlayer != null) {
            release();
        }
        source.setValue(path);
        marker = 0;
        if (!TextUtils.isEmpty(path)) {
            open(path);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            marker = mediaPlayer.getCurrentPosition();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        pause();
        release();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        if (mediaPlayer != null) {
            release();
        }
        String path = source.getValue();
        if (!TextUtils.isEmpty(path)) {
            open(path);
        }
    }

    public void seekTo(long ms) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(ms);
        }
    }

    public void start() {
        String path;
        if (mediaPlayer != null) {
            mediaPlayer.start();
            main.removeCallbacksAndMessages(positionUpdater);
            main.post(positionUpdater);
        } else if (!TextUtils.isEmpty((path = source.getValue()))) {
            open(path);
        }
    }

    private void release() {
        main.removeCallbacks(positionUpdater);
        if (mediaPlayer != null) {
            if (BuildConfig.DEBUG) {
                IjkMediaPlayer.native_profileEnd();
            }
            audioManager.abandonAudioFocus(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
            visualizer = null;
        }
    }

    private void open(String path) {
        audioManager.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        }
        if (path != null) {
            mediaPlayer = new IjkMediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnSeekCompleteListener(position);
            try {
                mediaPlayer.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }
    }

    @Override
    protected void onCleared() {
        release();
        main.removeCallbacksAndMessages(null);
    }

    private static final class LivePosition
            extends MutableLiveData<Long>
            implements IMediaPlayer.OnSeekCompleteListener {

        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
            postValue(iMediaPlayer.getCurrentPosition());
        }
    }

    private static final class LiveFft
            extends LiveData<byte[]>
            implements Visualizer.OnDataCaptureListener {
        @Override
        public void onWaveFormDataCapture(Visualizer visualizer,
                                          byte[] waveform,
                                          int samplingRate) {

        }

        @Override
        public void onFftDataCapture(Visualizer visualizer,
                                     byte[] fft,
                                     int samplingRate) {
            setValue(fft);
        }
    }
}