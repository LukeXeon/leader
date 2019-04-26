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

import org.kexie.android.dng.ai.BR;
import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.ai.databinding.FragmentSpeakerBinding;
import org.kexie.android.dng.ai.viewmodel.SpeakerViewModel;
import org.kexie.android.dng.ai.viewmodel.entity.Message;
import org.kexie.android.dng.ai.widget.WaveformView2;
import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static androidx.lifecycle.Lifecycle.Event;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = PR.ai.speaker)
public class SpeakerFragment extends Fragment {
    private SpeakerViewModel speakerViewModel;

    private WaveformView2 waveformView2;

    private FragmentSpeakerBinding binding;

    private GenericQuickAdapter<Message> messageGenericQuickAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        speakerViewModel = ViewModelProviders.of(this)
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
        waveformView2 = WaveformView2.Provider.INSTANCE.attachTo(binding.animation);
        binding.icon.bringToFront();
        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
        binding.setIsShowPartial(false);

        speakerViewModel.getNextMessage()
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(data -> {
                    binding.dataContent.scrollToPosition(messageGenericQuickAdapter
                            .getHeaderLayoutCount());
                    messageGenericQuickAdapter.addData(0, data);
                });
        speakerViewModel.getPartialResult()
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(s -> {
                    binding.setIsShowPartial(!TextUtils.isEmpty(s));
                    binding.setSpeechText(s);
                });
        binding.setOnStart(v -> {
            speakerViewModel.stopSpeak();
            speakerViewModel.beginSpeechTransaction();
        });

        Transformations.map(speakerViewModel.getVolume(),
                input -> input.floatValue() / 10f)
                .observe(this, v -> waveformView2.setAmplitude(v));
        speakerViewModel.getStatus().observe(this, status -> {
            switch (status) {
                case Initialization:
                case Idle: {
                    binding.icon.postDelayed(
                            () -> binding.icon.setVisibility(View.VISIBLE),
                            100);
                    waveformView2.stop();
                    binding.setIsShowPartial(false);
                }
                break;
                case Prepare: {
                    binding.icon.postDelayed(
                            () -> binding.icon.setVisibility(View.GONE),
                            100);
                    waveformView2.prepare();
                }
                break;
                case Speaking: {
                    Vibrator vibrator = (Vibrator) Objects.requireNonNull(requireContext()
                            .getSystemService(Context.VIBRATOR_SERVICE));
                    vibrator.vibrate(200);
                }
                break;
                case Recognition: {
                    waveformView2.setAmplitude(0.1f);
                }
                break;
            }
        });
        speakerViewModel.getWeakUp()
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(s -> speakerViewModel.beginSpeechTransaction());

        requireActivity().addOnBackPressedCallback(this,
                requireFragmentManager()::popBackStackImmediate);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getBoolean("weakUp")) {
                speakerViewModel.beginSpeechTransaction();
            }
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        speakerViewModel.endSpeechTransaction();
        WaveformView2.Provider.INSTANCE.detach();
    }
}
