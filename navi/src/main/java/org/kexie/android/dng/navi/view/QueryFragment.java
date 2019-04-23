package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.ProgressFragment;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviQueryBinding;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

@Route(path = PR.navi.query)
public final class QueryFragment extends Fragment
{
    private FragmentNaviQueryBinding binding;

    private QueryViewModel queryViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        queryViewModel = ViewModelProviders.of(requireParentFragment())
                .get(QueryViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navi_query,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Fragment fragment = (Fragment) ARouter
                .getInstance()
                .build(PR.navi.query_tips)
                .navigation();

        getChildFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment_content, fragment)
                .commit();

        ProgressFragment.observeWith(queryViewModel.isLoading(), this);

        requireActivity().addOnBackPressedCallback(this,
                getChildFragmentManager()::popBackStackImmediate);

    }
}