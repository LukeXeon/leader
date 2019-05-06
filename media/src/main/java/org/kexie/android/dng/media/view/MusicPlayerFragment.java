package org.kexie.android.dng.media.view;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zml.libs.widget.MusicSeekBar;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMusicPlayBinding;
import org.kexie.android.dng.media.viewmodel.LifecycleViewModelFactory;
import org.kexie.android.dng.media.viewmodel.MusicPlayerViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayBinding binding;
    private MusicPlayerViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LifecycleViewModelFactory factory = new LifecycleViewModelFactory(
                requireActivity().getApplication(),
                getLifecycle()
        );
        viewModel = ViewModelProviders.of(this, factory)
                .get(MusicPlayerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_music_play,
                container,
                false);
        viewModel.adapter.setEmptyView(inflater.inflate(R.layout.view_empty2, container, false));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        binding.rvMusicList.setAdapter(viewModel.adapter);
        //musicSeek
        binding.musicSeek.setTimePopupWindowViewColor(getResources().getColor(R.color.deeppurplea100));
        binding.musicSeek.setProgressColor(getResources().getColor(R.color.deeppurplea200));
        binding.musicSeek.setThumbColor(getResources().getColor(R.color.deeppurplea700));
        binding.musicSeek.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return MusicPlayerViewModel.getProgressTime(binding.musicSeek.getProgress());
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                viewModel.musicPlayer.seekTo(musicSeekBar.getProgress());

            }
        });
        binding.play.setOnClickListener(v -> viewModel.play("/storage/emulated" +
                "/0/qqmusic/song/泠鸢yousa - 何日重到苏澜桥 [mqms2].mp3"));
        //musicPlayer
        viewModel.lyrics.observe(this, lyrics -> {
            binding.lrcView.setLrc(lyrics);
            binding.lrcView.start();
        });
        viewModel.musicPlayer.getFft().observe(this,
                bytes -> binding.visualizer.updateVisualizer(bytes));
        viewModel.musicPlayer.getDuration().observe(this,
                duration -> {
                    binding.lrcView.setDuration(safeUnBoxInt(duration));
                    binding.musicSeek.setMax(safeUnBoxInt(duration));
                });
        viewModel.musicPlayer.getPosition().observe(this,
                position -> {
                    binding.musicSeek.setProgress(safeUnBoxInt(position));
                    binding.lrcView.seekTo(safeUnBoxInt(position));
                });
        binding.musicSeek.setEnabled(true);
        viewModel.adapter.setOnItemChildClickListener((adapter, view1, position) -> {

        });
        viewModel.details.observe(this,
                mediaDetails -> binding.setDetails(mediaDetails));
        binding.setVolume(viewModel.volume);
        int value = viewModel.getVolume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.volume.setProgress(value, true);
        } else {
            binding.volume.setProgress(value);
        }
    }

    private static int safeUnBoxInt(Long value) {
        return (int) safeUnBox(value);
    }

    private static long safeUnBox(Long value) {
        return value == null ? 0 : value;
    }
}
