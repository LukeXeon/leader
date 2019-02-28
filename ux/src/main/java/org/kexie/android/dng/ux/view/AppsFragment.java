package org.kexie.android.dng.ux.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentAppsBinding;
import org.kexie.android.dng.ux.viewmodel.AppsViewModel;
import org.kexie.android.dng.ux.viewmodel.entity.App;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;
import mapper.Mapping;

@Mapping("dng/ux/apps")
public class AppsFragment extends Fragment
{

    private FragmentAppsBinding binding;

    private AppsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_apps,
                container,
                false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.setOnTouchListener((x, y) -> true);

        setRetainInstance(false);

        viewModel = ViewModelProviders.of(this).get(AppsViewModel.class);
        //dataBinding
        binding.blurView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(requireActivity().getWindow()
                        .getDecorView()
                        .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);

        GenericQuickAdapter<App> adapter
                = new GenericQuickAdapter<>(R.layout.item_app, BR.app);

        binding.setAdapter(adapter);

        adapter.setOnItemClickListener((adapter1, view1, position) -> {
            String packName = Objects.requireNonNull(adapter.getItem(position))
                    .packageName;
            Intent intent = requireContext()
                    .getPackageManager()
                    .getLaunchIntentForPackage(packName);
            if (intent != null)
            {
                startActivity(intent);
            }
        });

        viewModel.apps.observe(this, adapter::setNewData);

        viewModel.isLoading.observe(this, binding::setIsLoading);

        viewModel.loadAppInfo();

    }
}