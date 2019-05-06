package org.kexie.android.dng.media.view;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;
import com.orhanobut.logger.Logger;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

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
        //lrcView
        binding.lrcView.setFontSize(SizeUtils.sp2px(20));
        binding.lrcView.setPaintColor(new int[]{
                getResources().getColor(R.color.deeppurplea100),
                getResources().getColor(R.color.deeppurplea100)
        }, false);
        binding.lrcView.setPaintHLColor(new int[]{
                getResources().getColor(R.color.deeppurplea700),
                getResources().getColor(R.color.deeppurplea700)
        }, false);
        binding.rvMusicList.setAdapter(viewModel.adapter);
        //musicSeek
        binding.musicSeek.setTimePopupWindowViewColor(getResources().getColor(R.color.deeppurplea100));
        binding.musicSeek.setProgressColor(getResources().getColor(R.color.deeppurplea200));
        binding.musicSeek.setThumbColor(getResources().getColor(R.color.deeppurplea700));
        binding.musicSeek.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return TimeUtils.parseMMSSString(binding.musicSeek.getProgress());
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
                binding.lrcView.seekto(musicSeekBar.getProgress());
            }
        });
        binding.play.setOnClickListener(v -> viewModel.playNewTask("/storage/emulated" +
                "/0/qqmusic/song/泠鸢yousa - 何日重到苏澜桥 [mqms2].mp3")
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(readerOptional -> {
                    if (readerOptional.isPresent()) {
                        binding.lrcView.setLyricsReader(readerOptional.get());
                        binding.lrcView.play((int) safeUnBox(viewModel.musicPlayer.getPosition().getValue()));
                    }
                    else {
                        binding.lrcView.setLrcStatus(ManyLyricsView.LRCSTATUS_NOLRC_DEFTEXT);
                    }
                }));
        //musicPlayer
        viewModel.musicPlayer.getFft().observe(this,
                bytes -> binding.visualizer.updateVisualizer(bytes));
        viewModel.musicPlayer.getDuration().observe(this,
                duration -> binding.musicSeek.setMax((int) safeUnBox(duration)));
        viewModel.musicPlayer.getPosition().observe(this,
                position -> binding.musicSeek.setProgress((int) safeUnBox(position)));
        binding.musicSeek.setEnabled(true);
        viewModel.adapter.setOnItemChildClickListener((adapter, view1, position) -> {

        });
        viewModel.details.observe(this,
                mediaDetails -> binding.setDetails(mediaDetails));
        binding.lrcView.setOnLrcClickListener(progress -> {
            Logger.d(progress);
            binding.musicSeek.setProgress(progress);
            viewModel.musicPlayer.seekTo(progress);
        });
        binding.setVolume(viewModel.volume);
        initVolumeSeekBar();

    }

    private void initVolumeSeekBar()
    {
        AudioManager audioManager = (AudioManager) requireContext()
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            float max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int value = Math.min(100, Math.round(current / max * 100));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.volume.setProgress(value, true);
            } else {
                binding.volume.setProgress(value);
            }
        }
    }

    private static long safeUnBox(Long value) {
        return value == null ? 0 : value;
    }
}
