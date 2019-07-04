package org.kexie.android.dng.ai.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.ai.databinding.FragmentSiriBinding;
import org.kexie.android.dng.ai.viewmodel.SiriViewModel;
import org.kexie.android.dng.ai.widget.WaveformView;
import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = Module.Ai.siri)
public class SiriFragment extends Fragment {

    private WaveformView waveformView;

    private FragmentSiriBinding binding;

    private SiriViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(SiriViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_siri,
                container,
                false);
        waveformView = WaveformView.Provider.INSTANCE.attachTo(binding.animation);
        binding.icon.bringToFront();
        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        binding.setAdapter(viewModel.messages);
        binding.setIsShowPartial(false);
        viewModel.scroll.observe(this, val -> binding.dataContent.scrollToPosition(val));
        viewModel.part.observe(this, s -> {
            binding.setIsShowPartial(!TextUtils.isEmpty(s));
            binding.setSpeechText(s);
        });
        binding.setOnStart(v -> {
            viewModel.asr.stop();
            viewModel.asr.begin();
        });
        viewModel.volume.observe(this,
                integer -> waveformView.setAmplitude(integer.floatValue() / 100f));
        viewModel.status.observe(this, status -> {
            switch (status) {
                case ASR.INITIALIZATION: {
                    binding.icon.postDelayed(
                            () -> binding.icon.setVisibility(View.VISIBLE),
                            100);
                }
                waveformView.stop();
                binding.setIsShowPartial(false);
                break;
                case ASR.PREPARE: {
                    binding.icon.postDelayed(
                            () -> binding.icon.setVisibility(View.GONE),
                            100);
                    waveformView.prepare();
                }
                break;
                case ASR.SPEAKING: {
                    Vibrator vibrator = (Vibrator) requireContext()
                            .getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(200);
                    }
                }
                break;
                case ASR.RECOGNITION: {
                    waveformView.setAmplitude(0.1f);
                }
                break;
            }
        });
        viewModel.action.observe(this, runnable -> {
            requireFragmentManager().popBackStackImmediate();
            runnable.run();
        });
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                requireFragmentManager().popBackStackImmediate();
                            }
                        });
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getBoolean("weakUp")) {
                viewModel.asr.begin();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WaveformView.Provider.INSTANCE.detach();
    }
}
