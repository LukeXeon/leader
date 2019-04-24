package org.kexie.android.dng.media.widget;

import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.Screen;

import org.kexie.android.dng.player.media.IjkPlayerView;

public final class FloatPlayerManager
        extends ViewStateAdapter
        implements IjkPlayerView.OnBackListener {

    private IjkPlayerView player;

    public static void transform(IjkPlayerView player) {
        new FloatPlayerManager(player);
    }

    private FloatPlayerManager(IjkPlayerView player) {
        this.player = player;
        player.setOnClickBackListener(this);
        FloatWindow.with(player.getContext().getApplicationContext())
                .setWidth(Screen.width, 0.6f)
                .setHeight(Screen.height, 0.6f)
                .setX(100)
                .setY(Screen.height, 0.3f)
                .setView(player)
                .setViewStateListener(this)
                .build();
        FloatWindow.get().show();
    }

    @Override
    public void onHide() {
        if (player != null) {
            player.onPause();
        }
    }

    @Override
    public void onShow() {
        if (player != null) {
            player.onResume();
        }
    }

    @Override
    public void onDismiss() {
        if (player != null) {
            player.onDestroy();
            player = null;
        }
    }

    @Override
    public void onBack() {
        if (player != null) {
            FloatWindow.destroy();
        }
    }
}
