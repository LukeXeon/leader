// IjkMusicPlayer.aidl
package org.kexie.android.dng.player.media.music;

import org.kexie.android.dng.player.media.music.IPlayerCallback;

// Declare any non-default types here with import statements

interface IMusicPlayer {
    void seekTo(long ms);
    void pause(boolean fromComponent);
    void resume(boolean fromComponent);
    void register(IPlayerCallback callback);
    void setInterval(long ms);
    void setNewSource(String path);
    void destroy();
    boolean isPlaying();
}
