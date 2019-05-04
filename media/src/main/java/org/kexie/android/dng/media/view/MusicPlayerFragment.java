package org.kexie.android.dng.media.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.BR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMusicPlayBinding;
import org.kexie.android.dng.media.viewmodel.entity.Media;
import org.kexie.android.dng.player.media.music.IjkMusicPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

@Route(path = PR.media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayBinding binding;
    private GenericQuickAdapter<Media> adapter;
    private IjkMusicPlayer player;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GenericQuickAdapter<>(R.layout.item_music, BR.mediaInfo);
        player = IjkMusicPlayer.newInstance(requireActivity());
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
        adapter.setOnItemClickListener((adapter, view12, position) -> {

        });
        player.source("/storage/emulated/0/qqmusic/song/泠鸢yousa - 何日重到苏澜桥 [mqms2].mp3");
        player.start();
        player.sessionId().observe(this,
                sessionId -> binding.visualizer.setAudioSessionId(sessionId));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.destroy();
            player = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.resume();
        }
    }
}
