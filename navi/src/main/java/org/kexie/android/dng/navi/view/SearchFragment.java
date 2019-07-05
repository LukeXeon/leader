package org.kexie.android.dng.navi.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentSearchBinding;
import org.kexie.android.dng.navi.viewmodel.SearchViewModel;
import org.kexie.android.dng.navi.viewmodel.beans.TipText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

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
            Fragment fragment = (Fragment) ARouter.getInstance()
                    .build(Module.Ai.speaker)
                    .navigation();
            fragment.setTargetFragment(this, R.id.speaker_request_code);
            requireFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(getId(), fragment)
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
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            }
        });
        //viewModel.speechResult.observe(this, s -> binding.setText(s));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (R.id.speaker_request_code == requestCode
                && Activity.RESULT_OK == resultCode
                && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String text = bundle.getString("text");
                binding.setText(text);
                viewModel.search(text);
            }
        }
    }
}
