package org.kexie.android.dng.media.view;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.viewmodel.entity.Media;
import org.kexie.android.dng.player.media.IjkPlayerView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class VideoPlayerFragment
        extends Fragment
        implements OnBackPressedCallback {

    private IjkPlayerView player;

    private boolean restart = false;

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
        if (!restart) {
            player = new IjkPlayerView(inflater.getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            player.setLayoutParams(params);
        }
        playerContainer.addView(player);
        return playerContainer;
    }

    public static Fragment restart(IjkPlayerView player) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.restart = true;
        fragment.player = player;
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!restart) {
            Media info = requireArguments().getParcelable("media");
            if (info != null) {
                Glide.with(this)
                        .load(info.uri)
                        .apply(RequestOptions.fitCenterTransform())
                        .into(player.mPlayerThumb);

                player.init().setSaveDir(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .getAbsolutePath() + "/dng")
                        .setTitle(info.title)    // set title
                        .setVideoPath(info.uri)
                        .alwaysFullScreen()
                        .setMediaQuality(IjkPlayerView.MEDIA_QUALITY_HIGH)  // set the initial video url
                        .start();   // Start playing
            }
        }
        requireActivity().addOnBackPressedCallback(this, this);
        player.setOnClickBackListener(requireActivity()::onBackPressed);
        player.mFloatWindow.setOnClickListener(RxOnClickWrapper
                .create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> {
                    playerContainer.removeView(player);
                    player.mFloatWindow.setVisibility(View.GONE);
                    player.setOnClickBackListener(null);
                    player.mFloatWindow.setOnClickListener(null);
                    VideoPlayerHolderActivity holderActivity
                            = (VideoPlayerHolderActivity) requireActivity();
                    holderActivity.transformToWindow(player);
                    player = null;
                    holderActivity.onBackPressed();
                })
                .build());
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