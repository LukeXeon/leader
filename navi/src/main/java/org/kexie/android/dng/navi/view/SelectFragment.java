package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviQuerySelectBinding;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

@Route(path = PR.navi.query_select)
public final class SelectFragment extends Fragment
{
    private FragmentNaviQuerySelectBinding binding;

    private QueryViewModel queryViewModel;

    private RouteAdapter routeAdapter;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navi_query_select,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        requireActivity().addOnBackPressedCallback(this, () -> {
            Map<Integer, RouteInfo> routeInfos = queryViewModel.getRoutes().getValue();
            if (routeInfos != null && !routeInfos.isEmpty())
            {
                queryViewModel.getCurrentSelect().setValue(QueryViewModel.NO_SELECT);
                queryViewModel.getRoutes().setValue(Collections.emptyMap());
            }
            return false;
        });

        queryViewModel = ViewModelProviders.of(requireParentFragment().requireParentFragment())
                .get(QueryViewModel.class);

        binding.setLifecycleOwner(this);
        binding.routePager.initStack(3, StackPageTransformer.Orientation.VERTICAL);
        binding.routePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onPageSelected(int position)
            {
                if (routeAdapter != null && !routeAdapter.fragments.isEmpty())
                {
                    int index = routeAdapter.fragments.get(position).first;
                    queryViewModel.select(index);
                } else
                {
                    queryViewModel.select(QueryViewModel.NO_SELECT);
                }
            }
        });

        queryViewModel.getRoutes().observe(this, x -> {
            List<Integer> ids;
            if (x != null)
            {
                ids = StreamSupport.stream(x.keySet())
                        .collect(Collectors.toList());
                Collections.reverse(ids);
            } else
            {
                ids = Collections.emptyList();
            }
            routeAdapter = new RouteAdapter(ids);
            binding.setRouteAdapter(routeAdapter);
        });


    }

    private final class RouteAdapter extends FragmentPagerAdapter
    {
        private final List<Pair<Integer,Fragment>> fragments;

        private RouteAdapter(List<Integer> ids)
        {
            super(getChildFragmentManager());
            fragments = StreamSupport.stream(ids)
                    .map(id -> {
                        Fragment fragment = (Fragment) ARouter
                                .getInstance()
                                .build(PR.navi.query_select_route)
                                .withInt("pathId", id)
                                .navigation();
                        return Pair.create(id, fragment);
                    }).collect(Collectors.toList());
        }

        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            return fragments.get(position).second;
        }

        @Override
        public int getCount()
        {
            return fragments.size();
        }
    }

}
