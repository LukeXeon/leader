package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentAppsBinding;
import org.kexie.android.dng.ux.viewmodel.AppsViewModel;
import org.kexie.android.dng.ux.viewmodel.entity.LiteAppInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import eightbitlab.com.blurview.RenderScriptBlur;
import es.dmoral.toasty.Toasty;
import mapper.Mapping;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

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

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(AppsViewModel.class);
        //dataBinding
        binding.blurView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(getActivity().getWindow()
                        .getDecorView()
                        .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
        binding.dataContent.setLayoutManager(new GridLayoutManager(getContext(),
                5));
        GenericQuickAdapter<LiteAppInfo> genericQuickAdapter
                = new GenericQuickAdapter<>(R.layout.item_app,"appInfo");
        genericQuickAdapter.setOnItemClickListener((adapter, view1, position) ->
                viewModel.requestJumpBy(genericQuickAdapter.getData().get(position)));
        binding.setAppInfos(genericQuickAdapter);
        viewModel.setAdapter(genericQuickAdapter);
        viewModel.loadAppInfo();
        //rx
        viewModel.getOnJumpTo()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(this::startActivity);
        viewModel.getOnErrorMessage()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(getContext(), s).show());
        viewModel.getOnSuccessMessage()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(getContext(), s).show());
    }
}