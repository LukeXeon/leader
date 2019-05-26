package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopNeoBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;


@Route(path = PR.ux.desktop)
public class NeoDesktopFragment extends Fragment {

    private FragmentDesktopNeoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return (binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_desktop_neo, container,
                false))
                .getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImage(binding.navi, R.drawable.icon_navi);
        loadImage(binding.store, R.drawable.icon_store);
        loadImage(binding.fm, R.drawable.icon_fm);
        loadImage(binding.apps, R.drawable.icon_apps);
        loadImage(binding.info, R.drawable.icon_info);
        loadImage(binding.music, R.drawable.icon_music);
        loadImage(binding.setting, R.drawable.icon_setting);
        loadImage(binding.time, R.drawable.icon_time);
        loadImage(binding.video, R.drawable.icon_video);
        loadImage(binding.photo, R.drawable.icon_photo);
        loadImage(binding.weather, R.drawable.icon_weather);
    }

    private static void loadImage(ImageView imageView, int id) {
        Glide.with(imageView).load(id).into(imageView);
    }
}
