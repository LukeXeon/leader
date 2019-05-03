package org.kexie.android.dng.player.media.music;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;

import com.orhanobut.logger.Logger;

import org.kexie.android.dng.player.BuildConfig;
import org.kexie.android.dng.player.media.vedio.IjkVideoView;

import java.lang.ref.WeakReference;

import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.core.math.MathUtils;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public final class IjkMusicPlayer {

    private WeakReference<Activity> mAttach;

    private IjkVideoView mAudioOnlyPlayer;

    private Visualizer mVisualizer;

    private Visualizer.OnDataCaptureListener mWaveformCallback = new Visualizer.OnDataCaptureListener() {
        public void onWaveFormDataCapture(Visualizer visualizer,
                                          byte[] bytes, int samplingRate) {
            Logger.d(bytes);
        }

        public void onFftDataCapture(Visualizer visualizer,
                                     byte[] fft, int samplingRate) {
            Logger.d(fft);
        }
    };

    @MainThread
    public static IjkMusicPlayer newInstance(Activity attach) {
        return new IjkMusicPlayer(attach);
    }


    private IjkMusicPlayer(Activity attach) {
        this.mAttach = new WeakReference<>(attach);
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        }
        mAudioOnlyPlayer = new IjkVideoView(attach);
        mAudioOnlyPlayer.setRender(IjkVideoView.RENDER_NONE);

    }

    @MainThread
    public void setNewSource(String file) {
        mAudioOnlyPlayer.setVideoURI(Uri.parse(file));
        seekTo(0);
    }

    @MainThread
    void setVolume(@FloatRange(from = 0f, to = 1f) float percent) {
        percent = MathUtils.clamp(percent, 0, 1f);
        Activity activity;
        AudioManager audioManager;
        if ((activity = mAttach.get()) != null && (audioManager = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE)) != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (maxVolume * percent), 0);
        } else {
            destroy();
        }
    }

    @MainThread
    public void seekTo(int ms) {
        if (mAttach != null) {
            mAudioOnlyPlayer.seekTo(ms);
        }
    }

    @MainThread
    public void pause() {
        if (mAttach != null) {
            mAudioOnlyPlayer.pause();
            mVisualizer.release();
            mVisualizer = null;
        }
    }

    @MainThread
    public int position() {
        return mAttach == null ? -1 : mAudioOnlyPlayer.getCurrentPosition();
    }

    @MainThread
    public int duration() {
        return mAttach == null ? -1 : mAudioOnlyPlayer.getDuration();
    }

    @MainThread
    public void destroy() {
        if (mAttach != null) {
            mVisualizer.release();
            mAudioOnlyPlayer.destroy();
            if (BuildConfig.DEBUG) {
                IjkMediaPlayer.native_profileEnd();
            }
            mVisualizer = null;
            mAttach = null;
            mAudioOnlyPlayer = null;
        }

    }

    @MainThread
    public void resume() {
        if (mAttach != null) {
            mAudioOnlyPlayer.resume();
            mVisualizer = new Visualizer(mAudioOnlyPlayer.getAudioSessionId());
            int maxCR = Visualizer.getMaxCaptureRate();
            mVisualizer.setCaptureSize(256);
            mVisualizer.setDataCaptureListener(
                    mWaveformCallback,
                    maxCR / 2,
                    false,
                    true);
        }
    }

    @MainThread
    public void start() {
        if (mAttach != null) {
            if (mAudioOnlyPlayer.isPlaying()) {
                seekTo(0);
                pause();
            } else {
                mAudioOnlyPlayer.start();
                mVisualizer = new Visualizer(mAudioOnlyPlayer.getAudioSessionId());
                int maxCR = Visualizer.getMaxCaptureRate();
                mVisualizer.setCaptureSize(256);
                mVisualizer.setDataCaptureListener(
                        mWaveformCallback,
                        maxCR / 2,
                        false,
                        true);
            }
        }
    }
}
