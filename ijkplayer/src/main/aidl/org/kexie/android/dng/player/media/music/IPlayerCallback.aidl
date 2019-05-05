// IPlayerCallback.aidl
package org.kexie.android.dng.player.media.music;

// Declare any non-default types here with import statements

interface IPlayerCallback {
    void onNewFft(in byte[] fft);
    void onPrepared(int audioSessionId,long duration);
    void onPlayCompleted();
    void onNewPosition(long ms);
}
