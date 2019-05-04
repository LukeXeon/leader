package org.kexie.android.dng.player.media.music;

import android.content.Context;
import android.media.AudioManager;

import org.kexie.android.dng.player.BuildConfig;

import java.io.IOException;

import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.core.math.MathUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

@MainThread
public final class IjkMusicPlayer {

    private IMediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private MutableLiveData<Integer> mSessionId = new MutableLiveData<>();
    private String mPath;
    private long mMark = 0;

    public static IjkMusicPlayer newInstance(Context context) {
        return new IjkMusicPlayer(context);
    }

    private IjkMusicPlayer(Context context) {
        //mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    private void openInternal() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        }
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (mPath != null) {
            if (BuildConfig.DEBUG) {
                IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
            }
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(mPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnPreparedListener(iMediaPlayer ->
            {
                iMediaPlayer.seekTo(mMark);
                mSessionId.setValue(iMediaPlayer.getAudioSessionId());
            });
            mMediaPlayer.prepareAsync();
        }
    }

    private void releaseInternal() {
        if (mMediaPlayer != null) {
            if (BuildConfig.DEBUG) {
                IjkMediaPlayer.native_profileEnd();
            }
            mAudioManager.abandonAudioFocus(null);
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public LiveData<Integer> sessionId() {
        return mSessionId;
    }

    public void source(String path) {
        mPath = path;
        if (mMediaPlayer != null) {
            releaseInternal();
        }
        openInternal();
        mSessionId.setValue(mMediaPlayer.getAudioSessionId());
    }

    public void start() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                seekTo(0);
                mMediaPlayer.start();
            } else {
                mMediaPlayer.start();
            }
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mMark = mMediaPlayer.getCurrentPosition();
            releaseInternal();
            mSessionId.setValue(0);
        } else {
            mMark = 0;
        }
    }

    public void seekTo(long ms) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(ms);
        } else {
            mMark = Math.max(0, ms);
        }
    }

    public void resume() {
        if (mMediaPlayer == null) {
            openInternal();
        }
    }

    public void destroy() {
        releaseInternal();
        mSessionId.setValue(0);
    }

    public long duration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : -1;
    }

    public long position() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : -1;
    }

    public void volume(@FloatRange(from = 0, to = 1) float percent) {
        percent = MathUtils.clamp(percent, 0, 1f);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (maxVolume * percent), 0);
    }

}
