package org.kexie.android.dng.navi.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentSearchBinding;
import org.kexie.android.dng.navi.databinding.FragmentSpeakerBinding;
import org.kexie.android.dng.navi.viewmodel.SearchViewModel;
import org.kexie.android.dng.navi.viewmodel.beans.TipText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

@Route(path = Module.Navi.search)
public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private FragmentSearchBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_search,
                container,
                false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.getRoot().setOnTouchListener((v, event) -> true);
        binding.setTipsAdapter(viewModel.tips);
        binding.setOnSpeech(v -> {
            Fragment fragment = new SpeakerFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitAllowingStateLoss();
        });
        viewModel.tips.setOnItemClickListener((adapter, view1, position) -> {
            TipText tipText = (TipText) adapter.getItem(position);
            requireActivity().onBackPressed();
            Fragment target = getTargetFragment();
            if (target != null) {
                Intent intent = new Intent();
                intent.putExtra("tip", tipText);
                target.onActivityResult(R.id.search_request_code, Activity.RESULT_OK, intent);
            }
        });
        viewModel.speechResult.observe(this, s -> binding.setText(s));
    }

    public static class SpeakerFragment extends Fragment {

        private SearchViewModel viewModel;

        private FragmentSpeakerBinding binding;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            viewModel = ViewModelProviders.of(requireParentFragment())
                    .get(SearchViewModel.class);
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
            viewModel.asr.begin();
            viewModel.speechPartText.observe(this, s -> binding.setText(s));
            viewModel.speechResult.observe(this, s -> {
                binding.setText(s);
                viewModel.search(s);
                requireFragmentManager().popBackStackImmediate();
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

        @Override
        public void onDestroy() {
            super.onDestroy();
            viewModel.asr.stop();
        }
    }
}
