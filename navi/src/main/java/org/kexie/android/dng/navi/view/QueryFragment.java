package org.kexie.android.dng.navi.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.widget.ProgressFragment;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviQueryBinding;
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = "/navi/query")
public final class QueryFragment extends Fragment
{
    private FragmentNaviQueryBinding binding;

    private NaviViewModel naviViewModel;

    private InputTipViewModel inputTipViewModel;

    private GenericQuickAdapter<InputTip> inputTipQuickAdapter;

    private RouteAdapter routeAdapter;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        naviViewModel = ViewModelProviders.of(requireParentFragment())
                .get(NaviViewModel.class);
        inputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel.class);

        inputTipQuickAdapter = new GenericQuickAdapter<>(R.layout.item_tip, BR.inputTip);
        inputTipQuickAdapter.setOnItemClickListener((adapter, view1, position) -> {
            InputTip inputTip = Objects.requireNonNull(inputTipQuickAdapter.getItem(position));
            naviViewModel.query(inputTip);
        });

        binding.setIsShowTips(false);
        binding.setIsShowQuery(false);
        binding.setStartQuery(v -> binding.setIsShowQuery(true));
        binding.setLifecycleOwner(this);
        binding.setQueryText(inputTipViewModel.getQueryText());
        binding.setTipsAdapter(inputTipQuickAdapter);
        binding.routePager.initStack(3, StackPageTransformer.Orientation.VERTICAL);
        binding.routePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                if (routeAdapter != null && !routeAdapter.fragments.isEmpty())
                {
                    int index = routeAdapter.fragments.keyAt(position);
                    naviViewModel.getCurrentSelect().setValue(index);
                } else
                {
                    naviViewModel.getCurrentSelect().setValue(NaviViewModel.NO_SELECT);
                }
            }
        });

        inputTipViewModel.getInputTips()
                .observe(this, data -> {
                    boolean isShow = data != null && !data.isEmpty();
                    alphaAnimation(binding.tipList, isShow);
                    binding.setIsShowTips(isShow);
                    inputTipQuickAdapter.setNewData(data);
                });
        inputTipViewModel.getQueryText()
                .observe(this, data -> {
                    binding.setIsShowQuery(!TextUtils.isEmpty(data));
                    inputTipViewModel.query(data);
                });

        naviViewModel.getRoutes().observe(this, x -> {
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
            boolean isShow = !ids.isEmpty();
            switchRouteAnimation(isShow);
            binding.setIsShowRoutes(isShow);
            routeAdapter = new RouteAdapter(ids);
            binding.setRouteAdapter(routeAdapter);
        });
        naviViewModel.getOnError().mergeWith(inputTipViewModel.getOnError())
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, ON_DESTROY)))
                .subscribe(data -> Toasty.error(requireContext(), data).show());
        naviViewModel.getOnSuccess().mergeWith(inputTipViewModel.getOnSuccess())
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, ON_DESTROY)))
                .subscribe(data -> Toasty.success(requireContext(), data).show());

        ProgressFragment.observeWith(naviViewModel.isLoading(), this);

        requireActivity().addOnBackPressedCallback(this, () -> {
            Map<Integer, RouteInfo> routeInfos = naviViewModel.getRoutes().getValue();
            if (routeInfos != null && !routeInfos.isEmpty())
            {
                naviViewModel.getCurrentSelect().setValue(NaviViewModel.NO_SELECT);
                naviViewModel.getRoutes().setValue(Collections.emptyMap());
                return true;
            }
            List<InputTip> inputTips = inputTipViewModel.getInputTips().getValue();
            if (inputTips != null && !inputTips.isEmpty())
            {
                inputTipViewModel.getQueryText().setValue("");
                inputTipViewModel.getInputTips().setValue(Collections.emptyList());
                return true;
            }
            return false;
        });
    }

    private void switchRouteAnimation(boolean isShow)
    {
        alphaAnimation(binding.pagerRoot, isShow);
        alphaAnimation(binding.tipRoot, !isShow);
    }

    private void alphaAnimation(View view, boolean isShow)
    {
        AlphaAnimation alphaAnimation = new AlphaAnimation(isShow ? 0 : 1, isShow ? 1 : 0);
        alphaAnimation.setDuration(500);
        view.startAnimation(alphaAnimation);
    }

    private final class RouteAdapter extends FragmentPagerAdapter
    {
        private final SparseArrayCompat<Fragment> fragments = new SparseArrayCompat<>();

        private RouteAdapter(List<Integer> ids)
        {
            super(getChildFragmentManager());
            StreamSupport.stream(ids).forEach(id -> {
                Fragment fragment = (Fragment) ARouter
                        .getInstance()
                        .build("/navi/route")
                        .withInt("pathId", id)
                        .navigation();
                fragments.put(id, fragment);
            });
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            return fragments.valueAt(position);
        }

        @Override
        public int getCount()
        {
            return fragments.size();
        }
    }
}