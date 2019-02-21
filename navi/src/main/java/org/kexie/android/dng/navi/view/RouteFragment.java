package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.UiSettings;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding;
import org.kexie.android.dng.navi.viewmodel.RouteInfoViewModel;
import org.kexie.android.dng.navi.viewmodel.RouteMapViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapper;
import mapper.Mapping;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/navi/route")
public class RouteFragment extends Fragment
{
    private FragmentRouteBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_route, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        TextureSupportMapFragment mapFragment
                = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        AMap mapController = mapFragment.getMap();
        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            RouteMapViewModel viewModel1 = ViewModelProviders.of(this)
                    .get(RouteMapViewModel.class);
            binding.setOnJumpToNavi(v -> viewModel1.jumpToNavi());
            binding.setOnOpenDetails(v -> viewModel1.jumpToDetails());
            viewModel1.init(mapController, bundle);
            viewModel1.drawLine();
            viewModel1.setBounds();
            viewModel1.getOnJump()
                    .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(request -> getFragmentManager()
                            .beginTransaction()
                            .add(getId(), Mapper.getOn(this, request))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit());
            RouteInfoViewModel viewModel2 = ViewModelProviders.of(this)
                    .get(RouteInfoViewModel.class);
            viewModel2.getRouteInfo()
                    .observe(this, binding::setRoute);
            viewModel2.loadInfo(bundle);
        }
    }
}
