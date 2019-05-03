package org.kexie.android.dng.player.media.music;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.orhanobut.logger.Logger;

import org.kexie.android.dng.player.BuildConfig;
import org.kexie.android.dng.player.media.vedio.IjkVideoView;
import org.kexie.android.dng.player.media.vedio.MediaPlayerParams;

import java.lang.ref.WeakReference;

import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.core.math.MathUtils;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public final class IjkMusicPlayer {

    private WeakReference<Activity> mAttach;

    private IjkVideoView mAudioOnlyView;

    private Visualizer mVisualizer;

    @MainThread
    public static IjkMusicPlayer newInstance(Activity attach) {
        return new IjkMusicPlayer(attach);
    }

    @SuppressLint("RtlHardcoded")
    private IjkMusicPlayer(Activity attach) {
        this.mAttach = new WeakReference<>(attach);
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        }
        mAudioOnlyView = new IjkVideoView(attach);
        ViewGroup decorView = (ViewGroup) attach.getWindow().getDecorView();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
        params.leftMargin = -1;
        params.topMargin = -1;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        Logger.d(decorView.getClass().getSuperclass());
        mAudioOnlyView.setLayoutParams(params);
        decorView.addView(mAudioOnlyView);
        mAudioOnlyView.setOnInfoListener((mediaPlayer, state, noUse) -> {
            switch (state) {
                case MediaPlayerParams.STATE_PREPARED: {
                    if (mVisualizer != null) {
                        mVisualizer.release();
                    }
                    mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
                    int max = Visualizer.getMaxCaptureRate();
                    mVisualizer.setDataCaptureListener(
                            new Visualizer.OnDataCaptureListener() {
                                @Override
                                public void onWaveFormDataCapture(Visualizer visualizer,
                                                                  byte[] waveform,
                                                                  int samplingRate) {
                                    Logger.d(waveform);
                                }

                                @Override
                                public void onFftDataCapture(Visualizer visualizer,
                                                             byte[] fft,
                                                             int samplingRate) {

                                }
                            },
                            max / 2,
                            true,
                            false);
                    Logger.d(mediaPlayer.getAudioSessionId());
                    mVisualizer.setEnabled(true);
                    Logger.d(mVisualizer.getEnabled());
                }
                break;
                case MediaPlayerParams.STATE_COMPLETED:
                case MediaPlayerParams.STATE_PAUSED: {
                    if (mVisualizer != null) {
                        mVisualizer.release();
                        mVisualizer = null;
                    }
                }
                break;
            }
            return false;
        });
    }

    @MainThread
    public void setNewSource(String file) {
        mAudioOnlyView.setVideoURI(Uri.parse(file));
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
            mAudioOnlyView.seekTo(ms);
        }
    }

    @MainThread
    public void pause() {
        if (mAttach != null) {
            mAudioOnlyView.pause();
        }
    }

    @MainThread
    public int position() {
        return mAttach == null ? -1 : mAudioOnlyView.getCurrentPosition();
    }

    @MainThread
    public int duration() {
        return mAttach == null ? -1 : mAudioOnlyView.getDuration();
    }

    @MainThread
    public void destroy() {
        if (mAttach != null) {
            mAudioOnlyView.setOnInfoListener(null);
            mAudioOnlyView.setOnInfoListener(null);
            mAudioOnlyView.destroy();
            if (BuildConfig.DEBUG) {
                IjkMediaPlayer.native_profileEnd();
            }
            mAttach = null;
            mAudioOnlyView = null;
        }

    }

    @MainThread
    public void resume() {
        if (mAttach != null) {
            mAudioOnlyView.resume();
        }
    }

    @MainThread
    public void start() {
        if (mAttach != null && mAttach.get() != null) {
            if (mAudioOnlyView.isPlaying()) {
                seekTo(0);
                pause();
            } else {
                mAudioOnlyView.start();
            }
        }
    }
}
