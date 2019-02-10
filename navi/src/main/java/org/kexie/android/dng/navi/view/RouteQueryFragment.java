package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteQueryBinding;
import org.kexie.android.dng.navi.viewmodel.RouteQueryViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import org.kexie.android.common.databinding.BT;
import org.kexie.android.common.widget.ProgressHelper;

public class RouteQueryFragment extends Fragment
{
    private static final String WAIT_QUERY = "wait query";

    private FragmentRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_route_query,
                container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setTips(null);
        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);

        viewModel.getRoutes().observe(this,
                routes -> binding.setRoutes(routes));
        viewModel.getTips().observe(this,
                tips -> {
                    if (BT.isEmpty(tips))
                    {
                        Toasty.error(getContext().getApplicationContext(),
                                "发生错误，请检查网络连接")
                                .show();
                    } else
                    {
                        Toasty.success(getContext().getApplicationContext(),
                                "查询成功").show();
                    }
                    binding.setTips(tips);
                });
                ProgressHelper.observe(viewModel.getLoading(),getChildFragmentManager(),0);
        viewModel.getQueryText().observe(this,
                s -> {
                    if (TextUtils.isEmpty(s))
                    {
                        Toasty.warning(getContext().getApplicationContext(),
                                "搜索内容为空").show();
                    } else
                    {
                        binding.setQueryText(s);
                    }
                });
        binding.setActions(viewModel.getActions());
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
}