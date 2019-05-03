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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

@Route(path = PR.media.music)
public class MusicPlayerFragment extends Fragment {

    private FragmentMusicPlayBinding binding;
    private GenericQuickAdapter<Object> adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        adapter = new GenericQuickAdapter<>(R.layout.item_music, BR.mediaInfo);
        super.onCreate(savedInstanceState);
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.lrcView.setFontSize(SizeUtils.sp2px(25));
        binding.lrcView.setPaintColor(new int[]{
                getResources().getColor(R.color.deeppurplea100),
                getResources().getColor(R.color.deeppurplea100)
        });
        binding.lrcView.setPaintHLColor(new int[]{
                getResources().getColor(R.color.deeppurplea700),
                getResources().getColor(R.color.deeppurplea700)
        });
        binding.appbarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float scrollRange = appBarLayout.getTotalScrollRange();
            View view1 = binding.llTitleRoot;
            float p = 0.8f;
            float offset = Math.abs(verticalOffset);
            if (offset >= scrollRange * p) {
                offset -= (scrollRange * p);
                float openPer = offset / (scrollRange * (1f - p));
                view1.setAlpha(openPer);
                view1.setVisibility(View.VISIBLE);
            } else {
                view1.setVisibility(View.GONE);
            }
        });
        binding.rvMusicList.setAdapter(adapter);
    }
}
