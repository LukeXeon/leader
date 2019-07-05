package org.kexie.android.dng.ai.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.ai.databinding.FragmentSpeakerBinding;
import org.kexie.android.dng.ai.viewmodel.SpeakerViewModel;
import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = Module.Ai.speaker)
public class SpeakerFragment extends Fragment {
    private SpeakerViewModel viewModel;

    private FragmentSpeakerBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this)
                .get(SpeakerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_speaker,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.part.observe(this, s -> binding.setText(s));
        viewModel.finish.observe(this, s -> {
            binding.setText(s);
            requireFragmentManager().popBackStackImmediate();
            Fragment target = getTargetFragment();
            if (target != null) {
                Intent intent = new Intent();
                intent.putExtra("text", s);
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            }
        });
        viewModel.status.observe(this, status -> {
            if (status == ASR.RECOGNITION) {
                binding.setText("识别中......");
            }
        });
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                requireFragmentManager().popBackStackImmediate();
                            }
                        });
    }
}
