package org.kexie.android.dng.media.widget;

import android.content.Context;

import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.Screen;

import org.kexie.android.dng.media.R;
import org.kexie.android.dng.player.media.IjkPlayerView;

public final class FloatPlayerManager
        extends ViewStateAdapter
        implements IjkPlayerView.OnBackListener {

    private IjkPlayerView player;
    private Context context;

    public static void transform(IjkPlayerView player) {
        new FloatPlayerManager(player);
    }

    private FloatPlayerManager(IjkPlayerView player) {
        this.player = player;
        player.setOnClickBackListener(this);
        context = player.getContext().getApplicationContext();
        FloatWindow.with(context)
                .setWidth(Screen.width, 0.6f)
                .setHeight(Screen.height, 0.6f)
                .setX(100)
                .setTag(context.getString(R.string.window_key))
                .setY(Screen.height, 0.3f)
                .setView(player)
                .setViewStateListener(this)
                .build();
        FloatWindow.get(context.getString(R.string.window_key)).show();
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
            FloatWindow.destroy(context.getString(R.string.window_key));
        }
    }
}
