package org.kexie.android.dng.media.view;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zml.libs.widget.MusicSeekBar;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMusicPlayerBinding;
import org.kexie.android.dng.media.util.Utils;
import org.kexie.android.dng.media.viewmodel.MusicPlayerViewModel;
import org.kexie.android.dng.media.viewmodel.beans.MusicDetail;
import org.kexie.android.dng.media.widget.MusicCallbacks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = Module.Media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayerBinding binding;
    private MusicPlayerViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this)
                .get(MusicPlayerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_music_player,
                container,
                false);
        viewModel.details.setEmptyView(inflater.inflate(R.layout.view_empty2,
                container,
                false));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        binding.rvMusicList.setAdapter(viewModel.details);
        //musicSeek
        binding.musicSeek.setTimePopupWindowViewColor(getResources().getColor(R.color.deeppurplea100));
        binding.musicSeek.setProgressColor(getResources().getColor(R.color.deeppurplea200));
        binding.musicSeek.setThumbColor(getResources().getColor(R.color.deeppurplea700));
        binding.musicSeek.setOnMusicListener(new MusicCallbacks() {
            @Override
            public String getTimeText() {
                return Utils.getProgressTime(binding.musicSeek.getProgress());
            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                viewModel.seekTo(musicSeekBar.getProgress());
            }
        });
        binding.play.setOnClickListener(v -> {
            if (viewModel.isPlaying()) {
                viewModel.pause();
            } else {
                viewModel.start();
            }
        });
        //musicPlayer
        viewModel.lyricSet.observe(this, lyrics -> {
            binding.lrcView.setLrc(lyrics);
            binding.lrcView.start();
        });
        viewModel.getFft().observe(this,
                bytes -> binding.visualizer.updateVisualizer(bytes));
        viewModel.duration.observe(this,
                duration -> {
                    binding.lrcView.setDuration(Utils.safeUnBoxInt(duration));
                    binding.musicSeek.setMax(Utils.safeUnBoxInt(duration));
                });
        viewModel.getPosition().observe(this,
                position -> {
                    binding.musicSeek.setProgress(Utils.safeUnBoxInt(position));
                    binding.lrcView.seekTo(Utils.safeUnBoxInt(position));
                });
        binding.musicSeek.setEnabled(true);
        viewModel.details.setOnItemChildClickListener((adapter, view1, position) -> {
            MusicDetail detail = (MusicDetail) adapter.getItem(position);
            if (detail != null) {
                viewModel.setNewSource(detail.path);
            }
        });
        viewModel.current.observe(this,
                mediaDetails -> binding.setDetails(mediaDetails));
        binding.setVolume(viewModel.volumePercent);
        int value = viewModel.getVolume();
        binding.volume.setMax(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.volume.setProgress(value, true);
        } else {
            binding.volume.setProgress(value);
        }
    }
}
