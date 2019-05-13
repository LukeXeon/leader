package org.kexie.android.dng.media.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.kexie.android.danmakux.utils.FileUtils;
import org.kexie.android.dng.common.widget.RxUtils;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.viewmodel.entity.Media;
import org.kexie.android.dng.player.media.vedio.IjkPlayerView;

import java.io.File;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class VideoPlayerFragment
        extends Fragment
        implements OnBackPressedCallback {

    private IjkPlayerView player;

    private boolean isFormWindow = false;

    private FrameLayout playerContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        playerContainer = (FrameLayout) inflater
                .inflate(R.layout.fragment_player_container,
                        container,
                        false);
        if (!isFormWindow) {
            player = new IjkPlayerView(inflater.getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            player.setLayoutParams(params);
        }
        playerContainer.addView(player);
        return playerContainer;
    }

    public static Fragment formWindow(IjkPlayerView player) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.isFormWindow = true;
        fragment.player = player;
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isFormWindow) {
            Media info = requireArguments().getParcelable("media");
            if (info != null && info.uri != null) {
                Glide.with(this)
                        .load(info.uri)
                        .apply(RequestOptions.fitCenterTransform())
                        .into(player.mPlayerThumb);

                // set title
                player.init().setTitle(info.title)
                        .enableDanmaku()
                        .setVideoPath(info.uri)
                        .alwaysFullScreen();
                List<File> files = FileUtils.getAttachedSubtitles(info.uri);
                if (!files.isEmpty()) {
                    File file = files.get(0);
                    player.setDanmakuSource(file);
                }
                player.start();
            }
        }
        requireActivity().addOnBackPressedCallback(this, this);
        player.setOnClickBackListener(requireActivity()::onBackPressed);
        player.setFloatClickListener(RxUtils.debounce(
                View.OnClickListener.class,
                getLifecycle(),
                v -> transformToWindow()));
    }

    private void transformToWindow() {
        playerContainer.removeView(player);
        player.setFloatClickListener(null);
        MediaHolderActivity holderActivity
                = (MediaHolderActivity) requireActivity();
        holderActivity.holdByWindow(player);
        player = null;
        holderActivity.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        playerContainer = null;
        if (player != null) {
            player.onDestroy();
            player = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
    }

    @Override
    public boolean handleOnBackPressed() {
        if (player != null) {
            player.onBackPressed();
        }
        return false;
    }
}