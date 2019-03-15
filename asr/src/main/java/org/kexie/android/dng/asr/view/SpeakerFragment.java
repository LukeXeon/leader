package org.kexie.android.dng.asr.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.asr.BR;
import org.kexie.android.dng.asr.R;
import org.kexie.android.dng.asr.databinding.FragmentSpeakerBinding;
import org.kexie.android.dng.asr.viewmodel.SpeakerViewModel;
import org.kexie.android.dng.asr.viewmodel.entity.Message;
import org.kexie.android.dng.asr.widget.WaveformView2;
import org.kexie.android.dng.asr.widget.WaveformView2PreLoader;
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

        WaveformView2 waveformView2 = WaveformView2PreLoader.getView();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        waveformView2.setLayoutParams(params);
        binding.animation.addView(waveformView2);

        viewModel = ViewModelProviders.of(this).get(SpeakerViewModel.class);
        viewModel.getNextMessage()
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(data -> messageGenericQuickAdapter.addData(0, data));
        viewModel.getStatus().observe(this, status -> {
            Logger.d(status);
            switch (status)
            {
                case Initialization:
                case Idle:
                {
                    waveformView2.stop();
                }
                break;
                case Prepare:
                {
                    waveformView2.initialize();
                }
                break;
                case Speaking:
                {
                    waveformView2.speechStarted();
                }
                break;
                case Recognition:
                {
                    waveformView2.speechEnded();
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

        Bundle bundle = getArguments();
        Logger.d(bundle != null && bundle.getBoolean("weakUp"));
        if (bundle != null)
        {
            if (bundle.getBoolean("weakUp"))
            {
                viewModel.beginTransaction();
            }
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding.animation.removeAllViews();
        binding.unbind();
        binding = null;
    }
}
