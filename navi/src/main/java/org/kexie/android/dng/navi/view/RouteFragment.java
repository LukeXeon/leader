package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviSelectRouteBinding;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.RunningViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.navi.query_select_route)
public final class RouteFragment extends Fragment {
    private FragmentNaviSelectRouteBinding binding;

    private QueryViewModel queryViewModel;

    private RunningViewModel runningViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment root = requireParentFragment()
                .requireParentFragment()
                .requireParentFragment();
        queryViewModel = ViewModelProviders.of(root)
                .get(QueryViewModel.class);
        runningViewModel = ViewModelProviders.of(root)
                .get(RunningViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_navi_select_route,
                    container,
                    false);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            int id = bundle.getInt("pathId");
            Map<Integer, RouteInfo> routeInfos = queryViewModel.getRoutes().getValue();
            if (routeInfos != null) {
                RouteInfo routeInfo = routeInfos.get(id);
                if (routeInfo != null) {
                    binding.setRoute(routeInfo);
                    binding.setOnJumpToNavi(RxOnClickWrapper
                            .create(View.OnClickListener.class)
                            .lifecycle(getLifecycle())
                            .inner(v -> runningViewModel.isRunning().setValue(true))
                            .build());
                }
            }
        }
    }
}
