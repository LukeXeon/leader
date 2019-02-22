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
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

@Mapping("dng/navi/route")
public class RouteFragment extends Fragment
{
    private FragmentRouteBinding binding;

    private AMap mapController;

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
        mapController = mapFragment.getMap();
        UiSettings uiSettings = mapController.getUiSettings();
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            NaviViewModel viewModel = ViewModelProviders.of(getParentFragment())
                    .get(NaviViewModel.class);
            int id = bundle.getInt("pathId");
            binding.infosList.setGuideData(viewModel.getGuideInfo(id));
        }
    }


    private void jumpTo(Request request)
    {
        int id = getParentFragment().getId();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(id, Mapper.getOn(this, request))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
