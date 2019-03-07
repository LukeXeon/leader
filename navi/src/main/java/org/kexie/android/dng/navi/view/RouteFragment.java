package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = "/navi/route")
public final class RouteFragment extends Fragment
{
    private FragmentRouteBinding binding;

    private NaviViewModel naviViewModel;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        if (binding == null)
        {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_route,
                    container,
                    false);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        binding.setLifecycleOwner(this);

        naviViewModel = ViewModelProviders.of(requireParentFragment().requireParentFragment())
                .get(NaviViewModel.class);
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            int id = bundle.getInt("pathId");
            apply(id);
        }
    }

    private void apply(int id)
    {
        Map<Integer, RouteInfo> routeInfos = naviViewModel.getRoutes().getValue();

        if (routeInfos != null)
        {
            RouteInfo routeInfo = routeInfos.get(id);
            if (routeInfo != null)
            {
                binding.setRoute(routeInfo);
                binding.setOnJumpToNavi(v -> {

                });
                binding.setOnJumpToNavi(v -> {

                });
            }
        }
    }
}
