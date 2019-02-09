package kexie.android.media.view;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.dou361.ijkplayer.widget.PlayStateParams;
import com.dou361.ijkplayer.widget.PlayerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.percentlayout.widget.PercentFrameLayout;
import kexie.android.media.R;
import kexie.android.media.databinding.FragmentVideoPlayerBinding;
import kexie.android.media.entity.MediaInfo;

public class VideoPlayerFragment extends Fragment
{

    public static VideoPlayerFragment newInstance(MediaInfo info)
    {
        Bundle args = new Bundle();
        args.putParcelable("info", info);
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private PlayerView player;

    private FragmentVideoPlayerBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        MediaInfo info = getArguments().getParcelable("info");
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_video_player,
                container,
                false);
        player = new PlayerView(getActivity(), binding.getRoot())
                .setScaleType(PlayStateParams.fitparent)
                .setPlayerBackListener(() -> {
                    //这里可以简单播放器点击返回键
                    getActivity().onBackPressed();
                })
                .showThumbnail(
                        ivThumbnail -> Glide.with(getContext())
                                .load(info.getPath())
                                .into(ivThumbnail))
                .seekTo(0)
                .setTitle(info.getTitle())
                .setPlaySource(info.getPath())
                .forbidTouch(false)
                .hideRotation(true)
                .hideMenu(true)
                .setOnlyFullScreen(true)
                .hideSteam(true)
                .setNetWorkTypeTie(false)
                .startPlay();
        player.setBrightness(50);
        return binding.getRoot();
    }

    @Override
    public void onStart()
    {
        super.onStart();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                MediaInfo info = getArguments().getParcelable("info");
                Glide.with(getContext()).asBitmap()
                        .load(info.getPath())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                .encodeQuality(1))
                        .listener(new RequestListener<Bitmap>()
                {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Bitmap> target,
                                                boolean isFirstResource)
                    {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap bitmap,
                                                   Object model,
                                                   Target<Bitmap> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource)
                    {
                        float height = binding.videoViewRoot.getMeasuredHeight();
                        float width = binding.videoViewRoot.getMeasuredWidth();
                        float videoHeight
                                = bitmap.getHeight(); // 视频高度
                        float videoWidth
                                = bitmap.getWidth(); // 视频宽
                        PercentFrameLayout.LayoutParams params
                                = (PercentFrameLayout.LayoutParams) binding
                                .videoView
                                .getLayoutParams();
                        params.getPercentLayoutInfo().widthPercent
                                = (height * (videoWidth / videoHeight)) / width;
                        return true;
                    }
                }).submit();
                observer.removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (player != null)
        {
            player.onResume();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (player != null)
        {
            player.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (player != null)
        {
            player.onConfigurationChanged(newConfig);
        }
    }
}
