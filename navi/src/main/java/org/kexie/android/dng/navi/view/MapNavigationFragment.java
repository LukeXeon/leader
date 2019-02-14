package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.widget.ProgressHelper;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNavigationBinding;
import org.kexie.android.dng.navi.entity.Route;
import org.kexie.android.dng.navi.viewmodel.MapNavigationViewModel;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;

public class MapNavigationFragment extends Fragment
{
    private static final String ARG = "route";
    private MapNavigationViewModel viewModel;
    private FragmentNavigationBinding binding;

    public static MapNavigationFragment newInstance(Route route)
    {
        Bundle args = new Bundle();
        args.putParcelable(ARG, route);
        MapNavigationFragment fragment = new MapNavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navigation, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        Route route = Objects.requireNonNull(bundle).getParcelable(ARG);
        viewModel = ViewModelProviders.of(this)
                .get(MapNavigationViewModel.class);
        viewModel.calculate(route);
        viewModel.getCalculateResult().observe(this,
                aBoolean -> {
                    if (aBoolean != null && aBoolean)
                    {
                        Toasty.success(Objects.requireNonNull(getContext())
                                        .getApplicationContext(),
                                "路径规划成功")
                                .show();
                    } else
                    {
                        Toasty.success(Objects.requireNonNull(getContext())
                                        .getApplicationContext(),
                                "路径规划失败，请检查网络连接")
                                .show();
                    }
                });
        ProgressHelper.observe(viewModel.getLoading(), this);
    }
}
