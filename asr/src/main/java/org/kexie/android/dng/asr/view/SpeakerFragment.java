package org.kexie.android.dng.asr.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.asr.BR;
import org.kexie.android.dng.asr.R;
import org.kexie.android.dng.asr.databinding.FragmentSpeakerBinding;
import org.kexie.android.dng.asr.viewmodel.SpeakerViewModel;
import org.kexie.android.dng.asr.viewmodel.entity.Message;
import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.GenericQuickAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;

import static androidx.lifecycle.Lifecycle.Event;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = PR.asr.speaker)
public class SpeakerFragment extends Fragment
{
    private SpeakerViewModel viewModel;

    private FragmentSpeakerBinding binding;

    private GenericQuickAdapter<Message> messageGenericQuickAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_speaker,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        messageGenericQuickAdapter = new GenericQuickAdapter<>(R.layout.item_message, BR.message);

        binding.setLifecycleOwner(this);
        binding.background.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(requireActivity().getWindow()
                        .getDecorView()
                        .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(requireContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
        binding.setAdapter(messageGenericQuickAdapter);

        viewModel = ViewModelProviders.of(this).get(SpeakerViewModel.class);
        viewModel.getNextMessage()
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(messageGenericQuickAdapter::addData);
        viewModel.getVolume().observe(this, binding.animation::setCurrentDBLevelMeter);
        viewModel.getStatus().observe(this, status -> {
            switch (status)
            {
                case Initialization:
                {
                    binding.animation.startInitializingAnimation();
                }
                break;
                case Idle:
                {
                    binding.animation.startInitializingAnimation();
                }
                break;
                case Prepare:
                {
                    binding.animation.startInitializingAnimation();
                }
                break;
                case Speaking:
                {
                    binding.animation.startRecordingAnimation();
                }
                break;
                case Recognition:
                {
                    binding.animation.startRecognizingAnimation();
                }
                break;
            }
        });
        viewModel.getWeakUp()
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(s -> viewModel.beginTransaction());

        requireActivity().addOnBackPressedCallback(this, () -> {
            viewModel.endTransaction();
            return false;
        });
    }

}
