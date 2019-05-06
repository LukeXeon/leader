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
import android.text.TextUtils;

import org.kexie.android.dng.player.BuildConfig;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public final class IjkMusicPlayerService extends Service {
    private static final int MIN_INTERVAL = 500;

    private final RemoteCallbackList<IMusicPlayerClient> mClientCallback = new RemoteCallbackList<>();
    private final IMusicPlayerService.Stub mBinder = new IMusicPlayerService.Stub() {
        @Override
        public void seekTo(long ms) {
            mMainThread.post(() -> {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(ms);
                }
            });
        }

        @Override
        public void pause(boolean fromComponent) {
            mMainThread.post(() -> {
                mMainThread.removeCallbacks(mPositionUpdater);
                if (mMediaPlayer != null) {
                    if (fromComponent) {
                        mMediaPlayer.pause();
                        mMarker = mMediaPlayer.getCurrentPosition();
                        release();
                    } else {
                        mMediaPlayer.pause();
                    }
                }
            });
        }

        @Override
        public void resume(boolean fromComponent) {
            mMainThread.post(() -> {
                if (fromComponent) {
                    if (mMediaPlayer != null) {
                        release();
                    }
                    if (!TextUtils.isEmpty(mPath)) {
                        open(mPath);
                    }
                } else {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.start();
                        mMainThread.post(mPositionUpdater);
                    }
                }
            });
        }

        @Override
        public void register(IMusicPlayerClient client) {
            mMainThread.post(() -> mClientCallback.register(client));
        }

        @Override
        public void setInterval(long ms) {
            mMainThread.post(() -> mInterval = Math.max(MIN_INTERVAL, ms));
        }

        @Override
        public void setNewSource(String path) {
            mMainThread.post(() -> {
                if (mMediaPlayer != null) {
                    release();
                }
                mPath = path;
                mMarker = 0;
                if (!TextUtils.isEmpty(mPath)) {
                    open(mPath);
                }
            });
        }

        @Override
        public void destroy(IMusicPlayerClient client) {
            mMainThread.post(() -> {
                release();
                mMarker = 0;
                mPath = null;
                mClientCallback.unregister(client);
            });
        }

        @Override
        public boolean isPlaying() {
            IMediaPlayer mediaPlayer = mMediaPlayer;
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }
    };
    private final Handler mMainThread = new Handler(Looper.getMainLooper());
    private final Runnable mPositionUpdater = new Runnable() {
        @Override
        public void run() {
            beginInvoke(callback -> callback.onNewPosition(mMediaPlayer == null
                    ? 0 : mMediaPlayer.getCurrentPosition()));
            mMainThread.postDelayed(this, mInterval);
        }
    };

    private AudioManager mAudioManager;
    private String mPath;
    private long mMarker = 0;
    private long mInterval = MIN_INTERVAL;
    private volatile IMediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private interface ClientInvoker {
        void invoke(IMusicPlayerClient client) throws RemoteException;
    }

    private void beginInvoke(ClientInvoker invoker) {
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

    private void open(String path) {
        mAudioManager.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (BuildConfig.DEBUG) {
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        }
        if (path != null) {
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
                beginInvoke(callback -> callback.onPrepared(id,
                        iMediaPlayer.getDuration()));
            });
            mMediaPlayer.setOnCompletionListener(iMediaPlayer
                    -> beginInvoke(IMusicPlayerClient::onPlayCompleted));
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
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
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
