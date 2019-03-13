package org.kexie.android.dng.asr.view;

import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.asr.viewmodel.SpeakerViewModel;
import org.kexie.android.dng.common.app.PR;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.asr.speaker)
public class SpeakerFragment extends Fragment
{
    private SpeakerViewModel viewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(SpeakerViewModel.class);
        requireActivity().addOnBackPressedCallback(this, () -> {
            if (!isHidden())
            {
                requireFragmentManager()
                        .beginTransaction()
                        .hide(this)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
