package org.kexie.android.dng.player.media.music;

import android.app.Activity;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class IjkMusicPlayer {

    private Activity mAttach;

    public static IjkMusicPlayer newInstance(Activity attach) {
        return new IjkMusicPlayer(attach);
    }

    private IjkMusicPlayer(Activity attach) {
        this.mAttach = attach;
        if (attach instanceof LifecycleOwner) {
            ((LifecycleOwner) attach)
                    .getLifecycle()
                    .addObserver((LifecycleEventObserver)
                            (source, event) -> {
                                if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                                    destroy();
                                }
                            });
        }

    }

    public IjkMusicPlayer init() {

        return this;
    }

    public void seekTo(long ms) {

    }

    public void pause() {


    }

    public void destroy() {

    }

    public void resume() {

    }
}
