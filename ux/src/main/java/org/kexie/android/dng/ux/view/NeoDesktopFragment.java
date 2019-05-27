package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopNeoBinding;
import org.kexie.android.dng.ux.widget.NeoDesktop;

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
        binding.list.setAdapter(NeoDesktop.newAdapter(action -> {

        }));

    }
}
