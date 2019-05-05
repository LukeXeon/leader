package org.kexie.android.dng.media.view;

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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayBinding binding;
    private GenericQuickAdapter<Media> adapter;
    private MusicBrowseViewModel viewModel;
    private IjkMusicPlayer musicPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GenericQuickAdapter<>(R.layout.item_music, BR.mediaInfo);
        viewModel = ViewModelProviders.of(this).get(MusicBrowseViewModel.class);
        musicPlayer = IjkMusicPlayer.newInstance(requireContext(), this);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
        //viewModel
        musicPlayer.getPosition().observe(this,
                position -> {
                    int duration = (int) safeUnBox(musicPlayer.getDuration().getValue());
                    binding.musicSeek.setEnabled(true);
                    if (binding.musicSeek.getMax() == 0) {
                        binding.musicSeek.setMax(duration);
                    }
                    binding.musicSeek.setProgress((int) safeUnBox(position));
                });
        musicPlayer.getFft().observe(this,
                bytes -> binding.visualizer.updateVisualizer(bytes));
//        viewModel.onNewReader.as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
//                .subscribe(lyricsReader -> {
//                    ManyLyricsView manyLyricsView = binding.lrcView;
//                    if (lyricsReader.isPresent()) {
//                        manyLyricsView.setLyricsReader(lyricsReader.get());
//                        int state = manyLyricsView.getLrcStatus();
//                        if (viewModel.playing() && state == ManyLyricsView.LRCSTATUS_LRC) {
//                            Integer integer;
//                            binding.lrcView.play((integer = viewModel
//                                    .position
//                                    .getValue()) == null ? 0 : integer);
//                        }
//                    } else {
//                        manyLyricsView.setLyricsReader(null);
//                        manyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_ERROR);
//                    }
//                });
        adapter.setOnItemClickListener((adapter, view12, position) -> {

        });
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
//        musicPlayer.onSourcePrepared()
//                .as(autoDisposable(from(this, Event.ON_DESTROY)))
//                .subscribe(aBoolean -> musicPlayer.start());

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
