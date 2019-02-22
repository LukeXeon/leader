package org.kexie.android.dng.navi.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.navi.view.RouteOverLay;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentDetailsBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

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

@Mapping("dng/navi/details")
public class DetailsFragment extends Fragment
{
    private FragmentDetailsBinding binding;

    private NaviViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_details,
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
        binding.root.setOnTouchListener((x,y)->true);

        TextureSupportMapFragment mapFragment
                = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        AMap mapController = mapFragment.getMap();
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            viewModel = ViewModelProviders.of(getActivity())
                    .get(NaviViewModel.class);
            int id = bundle.getInt("pathId");

            RouteOverLay routeOverLay = new RouteOverLay(mapController,
                    viewModel.getPath(id),
                    getContext().getApplicationContext());


            routeOverLay.setTrafficLine(false);
            routeOverLay.addToMap();

            binding.setOnToNavi(v -> viewModel.jumpToNavi(id));
            binding.setOnBack(v -> getActivity().onBackPressed());
            viewModel.getOnJump()
                    .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(request -> {
                        getActivity().onBackPressed();
                        getFragmentManager()
                                .beginTransaction()
                                .addToBackStack(null)
                                .add(getId(), Mapper.getOn(this, request))
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                    });
        }
    }
}
