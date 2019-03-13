package org.kexie.android.dng.ux.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.GenericQuickAdapter;
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

@Route(path = PR.ux.apps)
public class AppsFragment extends Fragment
{

    private FragmentAppsBinding binding;

    private AppsViewModel appsViewModel;

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

        GenericQuickAdapter<App> adapter
                = new GenericQuickAdapter<>(R.layout.item_app, BR.app);
        adapter.setOnItemClickListener(new GenericQuickAdapter.RxOnItemClick<App>(
                this,
                (adapter1, view1, position) -> {
                    String packName = Objects.requireNonNull(adapter1.getItem(position))
                            .packageName;
                    Intent intent = requireContext()
                            .getPackageManager()
                            .getLaunchIntentForPackage(packName);
                    if (intent != null)
                    {
                        startActivity(intent);
                    }
                }));

        appsViewModel = ViewModelProviders.of(requireActivity())
                .get(AppsViewModel.class);
        appsViewModel.apps.observe(this, adapter::setNewData);
        appsViewModel.isLoading.observe(this, binding::setIsLoading);

        binding.setAdapter(adapter);
        binding.setLifecycleOwner(this);
        binding.rootView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(requireActivity().getWindow()
                        .getDecorView()
                        .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);


    }
}