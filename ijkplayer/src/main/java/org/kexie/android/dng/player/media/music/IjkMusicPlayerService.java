package org.kexie.android.dng.player.media.music;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import org.kexie.android.dng.player.BuildConfig;


import java.io.IOException;

import androidx.core.math.MathUtils;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public final class IjkMusicPlayerService extends Service {
    private static final int MIN_INTERVAL = 500;

    private final RemoteCallbackList<IPlayerCallback> mClientCallback = new RemoteCallbackList<>();
    private final IMusicPlayer.Stub mBinder = new IMusicPlayer.Stub() {
        @Override
        public void seekTo(long ms) {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(ms);
            }
        }

        @Override
        public void pause(boolean fromComponent) {
            mMainThread.removeCallbacks(mPositionUpdater);
            if (mMediaPlayer != null) {
                if (fromComponent) {
                    mMarker = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.pause();
                    release();
                } else {
                    mMediaPlayer.pause();
                }
            }
        }

        @Override
        public void resume(boolean fromComponent) {
            if (fromComponent) {
                if (mMediaPlayer != null) {
                    release();
                }
                open();
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.start();
                    mMainThread.post(mPositionUpdater);
                }
            }
        }

        @Override
        public void register(IPlayerCallback callback) {
            mClientCallback.register(callback);
        }

        @Override
        public void setInterval(long ms) {
            mInterval = Math.max(MIN_INTERVAL, ms);
        }

        @Override
        public void setVolume(float percent) {
            percent = MathUtils.clamp(percent, 0, 1f);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    (int) (maxVolume * percent), 0);
        }

        @Override
        public void setNewSource(String path) {
            mPath = path;
            mMarker = 0;
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
                release();
            }
            open();
        }

        @Override
        public void destroy() {
            release();
        }

        @Override
        public boolean isPlaying() {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        }
    };
    private final Handler mMainThread = new Handler(Looper.getMainLooper());
    private final Runnable mPositionUpdater = new Runnable() {
        @Override
        public void run() {
            beginInvoke(callback -> callback.onNewPosition(mMediaPlayer != null
                    ? 0 : mMediaPlayer.getCurrentPosition()));
            mMainThread.postDelayed(this, mInterval);
        }
    };

    private AudioManager mAudioManager;
    private String mPath;
    private long mMarker = 0;
    private long mInterval = 500;
    private IMediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private interface CallbackInvoker {
        void invoke(IPlayerCallback callback) throws RemoteException;
    }

    private void beginInvoke(CallbackInvoker invoker) {
        int size = mClientCallback.beginBroadcast();
        for (int i = 0; i < size; ++i) {
            try {
                invoker.invoke(mClientCallback.getBroadcastItem(i));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mClientCallback.finishBroadcast();
    }

    private void open() {
        mMainThread.removeCallbacks(mPositionUpdater);
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
        }
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        }
        if (mPath != null) {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(iMediaPlayer -> {
                int id = iMediaPlayer.getAudioSessionId();
                if (id != 0) {
                    mVisualizer = new Visualizer(id);
                    mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                    mVisualizer.setDataCaptureListener(
                            new Visualizer.OnDataCaptureListener() {
                                @Override
                                public void onWaveFormDataCapture(Visualizer visualizer,
                                                                  byte[] waveform,
                                                                  int samplingRate) {

                                }

                                @Override
                                public void onFftDataCapture(Visualizer visualizer,
                                                             byte[] fft,
                                                             int samplingRate) {
                                    beginInvoke(callback -> callback.onNewFft(fft));
                                }
                            },
                            Math.round(Visualizer.getMaxCaptureRate() / 2f),
                            false,
                            true);
                    mVisualizer.setEnabled(true);
                }
                mMainThread.post(mPositionUpdater);
                if (mMarker > 0) {
                    iMediaPlayer.seekTo(mMarker);
                }
                beginInvoke(callback -> callback.onPrepared(id, iMediaPlayer.getDuration()));
            });
            mMediaPlayer.setOnCompletionListener(iMediaPlayer
                    -> beginInvoke(IPlayerCallback::onPlayCompleted));
            mMediaPlayer.setOnSeekCompleteListener(iMediaPlayer
                    -> beginInvoke(callback ->
                    callback.onNewPosition(iMediaPlayer.getCurrentPosition())));
            try {
                mMediaPlayer.setDataSource(mPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.prepareAsync();
        }
    }

    private void release() {
        mMainThread.removeCallbacks(mPositionUpdater);
        if (mMediaPlayer != null) {
            if (BuildConfig.DEBUG) {
                IjkMediaPlayer.native_profileEnd();
            }
            mAudioManager.abandonAudioFocus(null);
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
