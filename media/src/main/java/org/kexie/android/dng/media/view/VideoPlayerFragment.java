package org.kexie.android.dng.media.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.viewmodel.entity.Media;
import org.kexie.android.dng.media.widget.FloatPlayerManager;
import org.kexie.android.dng.player.media.IjkPlayerView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


@Route(path = PR.media.video)
public class VideoPlayerFragment
        extends Fragment
        implements OnBackPressedCallback {

    private IjkPlayerView player;

    private FrameLayout thisContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context context = inflater.getContext();
        this.thisContainer = (FrameLayout) inflater
                .inflate(R.layout.fragment_player_container, container, false);
        player = new IjkPlayerView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        player.setLayoutParams(params);
        this.thisContainer.addView(player);
        return this.thisContainer;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Media info = requireArguments().getParcelable("media");
        requireActivity().addOnBackPressedCallback(this, this);
        if (info != null) {
            Glide.with(this)
                    .load(info.uri)
                    .apply(RequestOptions.fitCenterTransform())
                    .into(player.mPlayerThumb);
            player.init().setSaveDir(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .getAbsolutePath() + "/dng")
                    .setOnClickBackListener(requireActivity()::onBackPressed)
                    .setTitle(info.title)    // set title
                    .setVideoPath(info.uri)
                    .alwaysFullScreen()
                    .setMediaQuality(IjkPlayerView.MEDIA_QUALITY_HIGH)  // set the initial video url
                    .start();   // Start playing

            player.mFloatWindow.setOnClickListener(RxOnClickWrapper
                    .create(View.OnClickListener.class)
                    .owner(this)
                    .inner(v -> {
                        thisContainer.removeView(player);
                        player.mFloatWindow.setVisibility(View.GONE);
                        player.setOnClickBackListener(null);
                        player.mFloatWindow.setOnClickListener(null);
                        FloatPlayerManager.transform(player);
                        player = null;
                        requireActivity().onBackPressed();
                    })
                    .build());
        }
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
