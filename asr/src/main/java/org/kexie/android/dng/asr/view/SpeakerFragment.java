package org.kexie.android.dng.asr.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.asr.BR;
import org.kexie.android.dng.asr.R;
import org.kexie.android.dng.asr.databinding.FragmentSpeakerBinding;
import org.kexie.android.dng.asr.viewmodel.SpeakerViewModel;
import org.kexie.android.dng.asr.viewmodel.entity.Message;
import org.kexie.android.dng.asr.widget.WaveformView2;
import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.GenericQuickAdapter;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static androidx.lifecycle.Lifecycle.Event;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = PR.asr.speaker)
public class SpeakerFragment extends Fragment
{
    private SpeakerViewModel speakerViewModel;

    private WaveformView2 waveformView2;

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
        waveformView2 = WaveformView2.Provider.INSTANCE.setTo(binding.animation);
        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
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

        speakerViewModel = ViewModelProviders.of(this).get(SpeakerViewModel.class);

        speakerViewModel.getNextMessage()
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(data -> {
                    binding.dataContent.scrollToPosition(messageGenericQuickAdapter
                            .getHeaderLayoutCount());
                    messageGenericQuickAdapter.addData(0, data);
                });
        speakerViewModel.getStatus().observe(this, status -> {
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
                    waveformView2.speechPrepare();
                }
                break;
                case Speaking:
                {
                    Vibrator vibrator = (Vibrator) Objects.requireNonNull(requireContext()
                            .getSystemService(Context.VIBRATOR_SERVICE));
                    vibrator.vibrate(100);
                    waveformView2.speechStarted();
                }
                break;
                case Recognition:
                {
                    waveformView2.speechEnded();
                }
                break;
                default:
                    break;
            }
        });
        speakerViewModel.getWeakUp()
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(s -> speakerViewModel.beginTransaction());

        requireActivity().addOnBackPressedCallback(this, () -> {
            speakerViewModel.endTransaction();
            return false;
        });

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            if (bundle.getBoolean("weakUp"))
            {
                speakerViewModel.beginTransaction();
            }
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        WaveformView2.Provider.INSTANCE.release();
        waveformView2 = null;
    }

}
