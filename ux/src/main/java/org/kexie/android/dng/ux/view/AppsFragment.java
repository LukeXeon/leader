package org.kexie.android.dng.ux.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chad.library.adapter.base.BaseQuickAdapter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentAppsBinding;
import org.kexie.android.dng.ux.viewmodel.AppsViewModel;
import org.kexie.android.dng.ux.viewmodel.entity.App;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.ux.apps)
public class AppsFragment extends Fragment {

    private FragmentAppsBinding binding;

    private AppsViewModel appsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appsViewModel = ViewModelProviders.of(requireActivity())
                .get(AppsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_apps,
                container,
                false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnTouchListener((x, y) -> true);

        GenericQuickAdapter<App> adapter = appsViewModel.appAdapter;
        adapter.setOnItemClickListener(RxOnClickWrapper
                .create(BaseQuickAdapter.OnItemClickListener.class)
                .owner(this)
                .inner((adapter1, view1, position) -> {
                    App app = (App) adapter1.getItem(position);
                    if (app == null) {
                        return;
                    }
                    String packName = app.packageName;
                    Intent intent = requireContext()
                            .getPackageManager()
                            .getLaunchIntentForPackage(packName);
                    if (intent != null) {
                        startActivity(intent);
                    }
                })
                .build());
        adapter.setOnItemLongClickListener((adapter12, view12, position) -> {



            return true;
        });

        appsViewModel.isLoading.observe(this, binding::setIsLoading);
        binding.setAdapter(adapter);
        binding.setLifecycleOwner(this);
        binding.rootView.setLifecycle(getLifecycle());
//        binding.rootView.setupWith((ViewGroup) view.getParent())
//                .setFrameClearDrawable(requireActivity().getWindow()
//                        .getDecorView()
//                        .getBackground())
//                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
//                .setBlurRadius(20f)
//                .setHasFixedTransformationMatrix(true);
    }

    @Override
    public void onDestroyView() {
        binding.setAdapter(null);
        super.onDestroyView();
    }
}