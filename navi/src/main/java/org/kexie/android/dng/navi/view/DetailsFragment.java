package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentDetailsBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import mapper.Mapping;

@Mapping("dng/navi/details")
public class DetailsFragment extends Fragment
{
    private FragmentDetailsBinding binding;

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

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        TextureSupportMapFragment mapFragment
                = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view));
        AMap mapController = mapFragment.getMap();
        Bundle bundle = getArguments();
        if (bundle != null)
        {

        }
    }

}
