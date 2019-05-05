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
import com.zml.libs.widget.MusicSeekBar;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.BR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMusicPlayBinding;
import org.kexie.android.dng.media.viewmodel.MusicBrowseViewModel;
import org.kexie.android.dng.media.viewmodel.entity.Media;
import org.kexie.android.dng.player.media.music.IjkMusicPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayBinding binding;
    private GenericQuickAdapter<Media> adapter;
    private MusicBrowseViewModel viewModel;
    private IjkMusicPlayer musicPlayer;
    private AudioManager audioManager;
    private MutableLiveData<Integer> volume = new MutableLiveData<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GenericQuickAdapter<>(R.layout.item_music, BR.mediaInfo);
        viewModel = ViewModelProviders.of(this).get(MusicBrowseViewModel.class);
        musicPlayer = IjkMusicPlayer.newInstance(requireContext(), this);
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
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
        adapter.setEmptyView(inflater.inflate(R.layout.view_empty2, container, false));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        //lrcView
        binding.lrcView.setFontSize(SizeUtils.sp2px(25));
        binding.lrcView.setPaintColor(new int[]{
                getResources().getColor(R.color.deeppurplea100),
                getResources().getColor(R.color.deeppurplea100)
        }, false);
        binding.lrcView.setPaintHLColor(new int[]{
                getResources().getColor(R.color.deeppurplea700),
                getResources().getColor(R.color.deeppurplea700)
        }, false);
        binding.rvMusicList.setAdapter(adapter);
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
                musicPlayer.seekTo(musicSeekBar.getProgress());
                binding.lrcView.seekto(musicSeekBar.getProgress());
            }
        });
        binding.play.setOnClickListener(v -> {
            musicPlayer.setNewSource("/storage/emulated/0/qqmusic/song/泠鸢yousa - 何日重到苏澜桥 [mqms2].mp3");
        });


        //musicPlayer
        musicPlayer.getFft().observe(this,
                bytes -> binding.visualizer.updateVisualizer(bytes));
        musicPlayer.getDuration().observe(this,
                duration -> binding.musicSeek.setMax((int) safeUnBox(duration)));
        musicPlayer.getPosition().observe(this,
                position -> binding.musicSeek.setProgress((int) safeUnBox(position)));
        binding.musicSeek.setEnabled(true);
        adapter.setOnItemClickListener((adapter, view12, position) -> {

        });
        binding.setVolume(volume);
        {
            float max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int value = Math.min(100, Math.round(current / max * 100));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.volume.setProgress(value, true);
            } else {
                binding.volume.setProgress(value);
            }
        }
        Transformations.map(volume, input -> (float) input / 100f)
                .observe(this, value -> {
                    float percent = MathUtils.clamp(value, 0, 1f);
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (int) (maxVolume * percent), 0);
                });

        binding.lrcView.setOnLrcClickListener(progress -> {
            Logger.d(progress);
            binding.musicSeek.setProgress(progress);
            musicPlayer.seekTo(progress);
        });
    }

    private static long safeUnBox(Long value) {
        return value == null ? 0 : value;
    }
}
