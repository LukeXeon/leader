package org.kexie.android.dng.media.widget;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.Screen;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.view.VideoPlayerFragment;
import org.kexie.android.dng.player.media.IjkPlayerView;

import androidx.fragment.app.Fragment;

public final class WindowPlayer
        extends ViewStateAdapter
        implements View.OnClickListener,
        IjkPlayerView.OnBackListener {

    private IjkPlayerView player;

    public WindowPlayer(IjkPlayerView player) {
        this.player = player;
        player.setOnClickBackListener(this);
        View root = View.inflate(
                player.getContext(),
                R.layout.window_float_player,
                null);
        Context context = player.getContext();
        FrameLayout container = root.findViewById(R.id.container);
        container.addView(player);
        FloatWindow.with(context.getApplicationContext())
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
    public void onClick(View v) {
        Context context = player.getContext().getApplicationContext();
        ARouter.getInstance()
                .build(PR.media.video)
                .withBoolean(context.getString(R.string.is_restart), true)
                .navigation(context);
    }

    public Fragment transformToFragment() {
        player.mFloatWindow.setVisibility(View.VISIBLE);
        player.setOnClickBackListener(null);
        Fragment fragment = VideoPlayerFragment.restart(player);
        player = null;
        return fragment;
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
            FloatWindow.destroy(player.getResources().getString(R.string.window_key));
        }
    }
}
