package org.kexie.android.dng.media.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMusicPlayBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

@Route(path = PR.media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayBinding binding;

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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.lrcView.setFontSize(SizeUtils.dp2px(20));
        binding.lrcView.setPaintColor(new int[]{
                getResources().getColor(R.color.deeppurplea100),
                getResources().getColor(R.color.deeppurplea100)
        });
        binding.lrcView.setPaintHLColor(new int[]{
                getResources().getColor(R.color.deeppurplea700),
                getResources().getColor(R.color.deeppurplea700)
        });


    }
}
