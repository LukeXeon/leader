package org.kexie.android.dng.navi.view;

import android.annotation.SuppressLint;
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
import org.kexie.android.dng.navi.viewmodel.NaviViewModel2;
import org.kexie.android.dng.navi.viewmodel.TipViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;
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
public class QueryFragment2 extends Fragment
{

    private FragmentQueryBinding binding;

    private NaviViewModel2 naviViewModel2;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.setLifecycleOwner(this);
        binding.pagerRoot.setOnTouchListener((x, y) -> true);
        binding.routePager.setOffscreenPageLimit(3);
        binding.routePager.setPageTransformer(false,
                new ScaleTransformer());

        naviViewModel2 = ViewModelProviders.of(getActivity())
                .get(NaviViewModel2.class);
        tipViewModel = ViewModelProviders.of(this)
                .get(TipViewModel.class);

        AMap mapController = TextureSupportMapFragment.class
                .cast(getChildFragmentManager()
                        .findFragmentById(R.id.map_view)).getMap();

        //getActivity().addOnBackPressedCallback(this,naviViewModel2);


        GenericQuickAdapter<InputTip> tipsAdapter
                = new GenericQuickAdapter<>(R.layout.item_tip, 0);

        tipsAdapter.setOnItemClickListener((adapter, view1, position) -> naviViewModel2
                        .query(tipsAdapter.getData().get(position)));

        binding.setTipsAdapter(tipsAdapter);

        tipViewModel.bindAdapter(tipsAdapter);

        tipViewModel.getIsShowTips()
                .observe(this,binding::setIsShowTips);

        tipViewModel.getQueryText()
                .observe(this,binding::setQuery);

        naviViewModel2.getRoutes()
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

        Observable.merge(naviViewModel2.getOnErrorMessage(), tipViewModel.getOnErrorMessage())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(getContext(), s).show());
        Observable.merge(naviViewModel2.getOnSuccessMessage(), tipViewModel.getOnSuccessMessage())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(getContext(), s).show());
        naviViewModel2.getOnLoading()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(ProgressFragment.makeObserver(this));


        tipViewModel.query(TipViewModel.DEBUG_TEXT);
        //test


//        new Thread()
//        {
//            @Override
//            public void run()
//            {
//                  Logger.d("begin");
//                Query q = new Query.Builder()
//                        .from(Point.form(109.200903, 24.40092))
//                        .to(Point.form(109.29154, 24.298327))
//                        .build();
//                naviViewModel2.loadRoute(q);
//                Logger.d("end");
//            }
//        }.start();

    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
}