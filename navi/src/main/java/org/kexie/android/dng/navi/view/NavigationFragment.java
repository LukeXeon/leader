package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNavigationBinding;
import org.kexie.android.dng.navi.model.Route;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import mapper.Mapping;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/navi/navi")
public class NavigationFragment extends Fragment
{
    private static final String ARG = "route";

    private NaviViewModel viewModel;

    private FragmentNavigationBinding binding;

    public static NavigationFragment newInstance(Route route)
    {
        Bundle args = new Bundle();
        args.putParcelable(ARG, route);
        NavigationFragment fragment = new NavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        Route route = Objects.requireNonNull(bundle).getParcelable(ARG);
        viewModel = ViewModelProviders.of(this)
                .get(NaviViewModel.class);
        viewModel.initMapController(
                ((NaviViewFragment) getChildFragmentManager()
                        .findFragmentById(R.id.fragment_navi))
                        .getInnerView().getMap());
        viewModel.beginBy(route);
        viewModel.getOnErrorMessage()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(getContext(), s).show());
        viewModel.getOnSuccessMessage()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(getContext(), s).show());
    }
}
