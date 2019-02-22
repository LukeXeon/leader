package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureSupportMapFragment;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.widget.ProgressFragment;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.TipViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.LiteTip;
import org.kexie.android.dng.navi.widget.ScaleTransformer;
import org.kexie.android.dng.navi.widget.SimpleApplyAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import mapper.Mapper;
import mapper.Mapping;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/navi/query")
public class QueryFragment extends Fragment
{
    private FragmentQueryBinding binding;

    private NaviViewModel naviViewModel;

    private TipViewModel tipViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_query,
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
        binding.pagerRoot.setOnTouchListener((x, y) -> true);
        binding.routePager.setPageTransformer(false, new ScaleTransformer());

        naviViewModel = ViewModelProviders.of(getActivity())
                .get(NaviViewModel.class);
        tipViewModel = ViewModelProviders.of(this)
                .get(TipViewModel.class);

        AMap mapController = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view)).getMap();

        GenericQuickAdapter<LiteTip> tipsAdapter
                = new GenericQuickAdapter<>(R.layout.item_tip, "tip");

        tipsAdapter.setOnItemClickListener(
                (adapter, view1, position) -> naviViewModel
                        .query(tipsAdapter.getData().get(position)));

        binding.setTipsAdapter(tipsAdapter);

        tipViewModel.bindAdapter(tipsAdapter);

        naviViewModel.getRoutes()
                .observe(this, requests -> {
                    if (requests == null)
                    {
                        binding.setRouteAdapter(null);
                        return;
                    }
                    List<Fragment> fragments = StreamSupport.stream(requests)
                            .map(request -> Mapper.getOn(this, request))
                            .collect(Collectors.toList());
                    SimpleApplyAdapter adapter = new SimpleApplyAdapter(
                            getChildFragmentManager(), fragments);
                    binding.setRouteAdapter(adapter);
                });

        Observable.merge(naviViewModel.getOnErrorMessage(), tipViewModel.getOnErrorMessage())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(getContext(), s).show());
        Observable.merge(naviViewModel.getOnSuccessMessage(), tipViewModel.getOnSuccessMessage())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(getContext(), s).show());
        Observable.merge(naviViewModel.getOnLoading(), tipViewModel.getOnLoading())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(ProgressFragment.makeObserver(this));


    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
}