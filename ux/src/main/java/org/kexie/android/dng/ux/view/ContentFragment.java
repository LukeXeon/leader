package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentContentBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

@Route(path = PR.ux.content)
public class ContentFragment extends Fragment
{
    private FragmentContentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_content,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        binding.setOnBack(v -> requireActivity().onBackPressed());
        //binding.rootView.setLifecycle(getLifecycle());
//        binding.rootView.setupWith((ViewGroup) view.getRootView())
//                .setFrameClearDrawable(requireActivity().getWindow()
//                        .getDecorView()
//                        .getBackground())
//                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
//                .setBlurRadius(20f)
//                .setHasFixedTransformationMatrix(true);

        Fragment fragment = (Fragment) ARouter.getInstance()
                .build(PR.ux.content_login)
                .navigation();

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.info_host, fragment)
                .commitAllowingStateLoss();
    }
}
