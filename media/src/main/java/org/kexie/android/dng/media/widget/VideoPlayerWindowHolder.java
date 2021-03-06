package org.kexie.android.dng.media.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.IFloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.Screen;

import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.view.VideoPlayerFragment;
import org.kexie.android.dng.player.vedio.IjkPlayerView;

import androidx.fragment.app.Fragment;

public class VideoPlayerWindowHolder
        extends ViewStateAdapter
        implements View.OnClickListener {

    private IjkPlayerView player;
    private View root;

    VideoPlayerWindowHolder(IjkPlayerView player) {
        this.player = player;
        root = View.inflate(
                player.getContext(),
                R.layout.window_video_player,
                null);
        root.findViewById(R.id.transform)
                .setOnClickListener(this);
        root.findViewById(R.id.close)
                .setOnClickListener(this);
        FrameLayout container = root.findViewById(R.id.container);
        container.addView(player);
        Context context = player.getContext();
        FloatWindow.with(context.getApplicationContext())
                .setWidth(Screen.width, 0.5f)
                .setHeight(Screen.height, 0.6f)
                .setTag(context.getString(R.string.window_key))
                .setX(Screen.width, 0)
                .setY(Screen.height, 0)
                .setDesktopShow(true)
                .setMoveType(MoveType.active)
                .setView(root)
                .setViewStateListener(this)
                .build();
        FloatWindow.get(context.getString(R.string.window_key)).show();
    }

    @Override
    public void onPositionUpdate(int x, int y) {
        Resources resources = player.getResources();
        int width = root.getMeasuredWidth();
        int height = root.getMeasuredHeight();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        IFloatWindow floatWindow = FloatWindow.get(resources.getString(R.string.window_key));
        if (x < -width / 2) {
            floatWindow.updateX(-width / 2);
        } else if (x + width / 2 > displayMetrics.widthPixels) {
            floatWindow.updateX(displayMetrics.widthPixels - width / 2);
        }
        if (y < 0) {
            floatWindow.updateY(0);
        } else if (y + height / 4 > displayMetrics.heightPixels) {
            floatWindow.updateY(displayMetrics.heightPixels - height / 4);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close) {
            if (player != null) {
                FloatWindow.destroy(player.getResources().getString(R.string.window_key));
            }
        } else if (id == R.id.transform) {
            Context context = player.getContext().getApplicationContext();
            Intent intent = new Intent(context, VideoPlayerActivityHolder.class);
            intent.putExtra(context.getString(R.string.is_form_window), true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    Fragment transformToFragment() {
        player.setFloatClickListener(null);
        FrameLayout container = (FrameLayout) player.getParent();
        container.removeView(player);
        Fragment fragment = VideoPlayerFragment.formWindow(player);
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
        root = null;
    }
}
