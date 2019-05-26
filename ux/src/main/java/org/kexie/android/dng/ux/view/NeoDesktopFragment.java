package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopNeoBinding;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import pl.droidsonroids.gif.GifDrawable;

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

        loadGif(binding.navi, R.mipmap.image_car_anim);
        loadGif(binding.store, R.mipmap.app_store);
        loadGif(binding.fm, R.mipmap.fm);
        loadGif(binding.apps, R.mipmap.apps);
        loadGif(binding.info, R.mipmap.info);
        loadGif(binding.music, R.mipmap.music);
        loadGif(binding.setting, R.mipmap.setting);
        loadGif(binding.time, R.mipmap.time);
        loadGif(binding.video, R.mipmap.video);
        loadGif(binding.photo, R.mipmap.photo);
        loadGif(binding.weather, R.mipmap.weather);
    }

    private void loadGif(ImageView imageView, int id) {
        try {
            GifDrawable gifDrawable = new GifDrawable(getResources(), id);
            imageView.setImageDrawable(gifDrawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
