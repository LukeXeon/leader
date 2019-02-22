package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.navi.AMapNaviView;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNavigationBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.widget.NaviViewFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapping;

@Mapping("dng/navi/navi")
public class NaviFragment extends Fragment
{
    private static final String ARG = "route";

    private NaviViewModel viewModel;

    private FragmentNavigationBinding binding;

    private AMapNaviView naviView;

    private AMap mapController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_navigation, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //initViews
        viewModel = ViewModelProviders.of(getActivity())
                .get(NaviViewModel.class);
        viewModel.startNavi();

        naviView = ((NaviViewFragment) getChildFragmentManager()
                .findFragmentById(R.id.fragment_navi))
                .getInnerView();



        mapController = naviView.getMap();
        //dataBinding




    }
}
