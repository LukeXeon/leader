package org.kexie.android.dng.player.media.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

@MainThread
@SuppressWarnings("WeakerAccess")
public final class IjkMusicPlayer {
    private static final Looper MAIN_LOOPER = Looper.getMainLooper();

    private final PublishSubject<Boolean> OnPlayCompleted = PublishSubject.create();
    private final PublishSubject<Boolean> mOnSourcePrepared = PublishSubject.create();
    private final MutableLiveData<byte[]> mFft = new MutableLiveData<>();
    private final MutableLiveData<Long> mDuration = new MutableLiveData<>();
    private final MutableLiveData<Long> mPosition = new MutableLiveData<>();
    private final MutableLiveData<Integer> mSessionId = new MutableLiveData<>();
    private final IPlayerCallback.Stub mCallback = new IPlayerCallback.Stub() {
        @Override
        public void onNewFft(byte[] fft) {
            mFft.postValue(fft);
        }

        @Override
        public void onPrepared(int audioSessionId, long duration) {
            mSessionId.postValue(audioSessionId);
            mDuration.postValue(duration);
            mOnSourcePrepared.onNext(Boolean.TRUE);
        }

        @Override
        public void onPlayCompleted() {
            OnPlayCompleted.onNext(Boolean.TRUE);
        }

        @Override
        public void onNewPosition(long ms) {
            mPosition.postValue(ms);
        }
    };
    private final LifecycleObserver mLifecycleCallback = new LifecycleEventObserver() {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            try {
                switch (event) {
                    case ON_RESUME: {
                        mService.resume(true);
                    }
                    break;
                    case ON_PAUSE: {
                        mService.pause(true);
                    }
                    break;
                    case ON_DESTROY: {
                        destroy();
                    }
                    break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IMusicPlayer.Stub.asInterface(service);
            try {
                mService.register(mCallback);
                if (!TextUtils.isEmpty(mPendingSource)) {
                    mService.setNewSource(mPendingSource);
                }
                if (mHolder != null) {
                    mHolder.addObserver(mLifecycleCallback);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mHolder != null) {
                mHolder.removeObserver(mLifecycleCallback);
            }
            mService = null;
            if (!mIsFinish) {
                Intent intent = new Intent(mAppContext, IjkMusicPlayerService.class);
                mAppContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }
    };

    private boolean mIsFinish = false;
    private String mPendingSource;
    private Context mAppContext;
    private Lifecycle mHolder;
    private IMusicPlayer mService;

    public static IjkMusicPlayer newInstance(Context context, Lifecycle lifecycle) {
        return new IjkMusicPlayer(context.getApplicationContext(), lifecycle);
    }

    private IjkMusicPlayer(Context context, Lifecycle lifecycle) {
        this.mAppContext = context;
        this.mHolder = lifecycle;
        Intent intent = new Intent(mAppContext, IjkMusicPlayerService.class);
        mAppContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void assetInternal() {
        if (!MAIN_LOOPER.equals(Looper.myLooper()) && !mIsFinish) {
            throw new IllegalStateException();
        }
    }

    public Observable<Boolean> onPlayCompleted() {
        assetInternal();
        return OnPlayCompleted.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> onSourcePrepared() {
        assetInternal();
        return mOnSourcePrepared.observeOn(AndroidSchedulers.mainThread());
    }

    public LiveData<byte[]> getFft() {
        assetInternal();
        return mFft;
    }

    public LiveData<Long> getPosition() {
        assetInternal();
        return mPosition;
    }

    public LiveData<Integer> getAudioSessionId() {
        assetInternal();
        return mSessionId;
    }

    public LiveData<Long> getDuration() {
        assetInternal();
        return mDuration;
    }

    public void seekTo(long ms) {
        assetInternal();
        if (mService != null) {
            try {
                mService.seekTo(ms);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        assetInternal();
        if (mService != null) {
            try {
                mService.pause(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        assetInternal();
        if (mService != null) {
            try {
                mService.resume(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setInterval(long ms) {
        assetInternal();
        if (mService != null) {
            try {
                mService.setInterval(ms);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setNewSource(String path) {
        assetInternal();
        if (mService != null) {
            try {
                mService.setNewSource(path);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mPendingSource = path;
        }
    }

    public void destroy() {
        assetInternal();
        mPendingSource = null;
        if (mService != null) {
            try {
                mIsFinish = true;
                mService.destroy();
                mAppContext.unbindService(mConnection);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPlaying() {
        assetInternal();
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
