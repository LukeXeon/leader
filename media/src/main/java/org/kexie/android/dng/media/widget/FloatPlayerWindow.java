package org.kexie.android.dng.media.widget;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.Screen;

import org.kexie.android.dng.media.R;
import org.kexie.android.dng.player.media.IjkPlayerView;

public final class FloatPlayerWindow
        extends ViewStateAdapter
        implements IjkPlayerView.OnBackListener {

    private IjkPlayerView player;
    private Context context;

    public static void transform(IjkPlayerView player) {
        new FloatPlayerWindow(player);
    }

    private FloatPlayerWindow(IjkPlayerView player) {
        this.player = player;
        player.setOnClickBackListener(this);
        View root = View.inflate(
                player.getContext(),
                R.layout.window_float_player,
                null);
        context = player.getContext()
                .getApplicationContext();
        FrameLayout container = root.findViewById(R.id.container);
        container.addView(player);
        FloatWindow.with(context)
                .setWidth(Screen.width, 0.6f)
                .setHeight(Screen.height, 0.7f)
                .setTag(context.getString(R.string.window_key))
                .setX(Screen.width, 0.6f)
                .setY(Screen.height, 0.3f)
                .setView(root)
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
