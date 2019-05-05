// IjkMusicPlayer.aidl
package org.kexie.android.dng.player.media.music;

import org.kexie.android.dng.player.media.music.IMusicPlayerClient;


interface IMusicPlayerService {
    void seekTo(long ms);
    void pause(boolean fromComponent);
    void resume(boolean fromComponent);
    void register(IMusicPlayerClient client);
    void setInterval(long ms);
    void setNewSource(String path);
    void destroy(IMusicPlayerClient client);
    boolean isPlaying();
}
