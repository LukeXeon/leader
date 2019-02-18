package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentInfoBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import eightbitlab.com.blurview.RenderScriptBlur;
import mapper.Mapping;

@Mapping("dng/ux/info")
public class InfoFragment extends Fragment
{
    private FragmentInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_info,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setOnBack(v -> getActivity().onBackPressed());
        binding.blurView.setupWith((ViewGroup) view.getParent())
                .setFrameClearDrawable(getActivity().getWindow()
                                .getDecorView()
                                .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.info_host, new QrCodeFragment())
                .commit();
    }
}
