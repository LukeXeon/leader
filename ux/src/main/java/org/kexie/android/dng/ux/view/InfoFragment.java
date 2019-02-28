package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.widget.BlurViewUtil;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentInfoBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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

        setRetainInstance(false);

        binding.setOnBack(v -> requireActivity().onBackPressed());

        BlurViewUtil.initUseFragment(this,binding.blurView);

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.info_host, new QrcodeFragment())
                .commit();
    }
}
