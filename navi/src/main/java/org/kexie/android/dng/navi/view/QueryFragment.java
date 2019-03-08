package org.kexie.android.dng.navi.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.widget.ProgressFragment;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviQueryBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = "/navi/query")
public final class QueryFragment extends Fragment implements OnBackPressedCallback
{
    private FragmentNaviQueryBinding binding;

    private NaviViewModel naviViewModel;

    private InputTipViewModel inputTipViewModel;

    private GenericQuickAdapter<InputTip> inputTipQuickAdapter;

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
            Point location = naviViewModel.getLocation().getValue();
            if (location != null)
            {
                naviViewModel.query(inputTip, location);
            } else
            {
                Toasty.error(requireContext(), "获取定位失败").show();
            }
        });

        binding.setIsShowSelect(false);
        binding.setIsShowQuery(false);
        binding.setStartQuery(v -> binding.setIsShowQuery(true));
        binding.setLifecycleOwner(this);
        binding.setQueryText(inputTipViewModel.getQueryText());
        binding.setTipsAdapter(inputTipQuickAdapter);
        binding.routePager.initStack(3, StackPageTransformer.Orientation.VERTICAL);

        inputTipViewModel.getInputTips()
                .observe(this, data -> {
                    binding.setIsShowTips(data != null && !data.isEmpty());
                    inputTipQuickAdapter.setNewData(data);
                });

        inputTipViewModel.getQueryText()
                .observe(this, data -> {
                    binding.setIsShowQuery(!TextUtils.isEmpty(data));
                    Point point = naviViewModel.getLocation().getValue();
                    if (point != null)
                    {
                        inputTipViewModel.query(data, point);
                    } else
                    {
                        Toasty.error(requireContext(), "获取定位失败").show();
                    }
                });

        naviViewModel.getRoutes().observe(this, x -> {
            List<Fragment> fragments;
            if (x != null)
            {
                fragments = StreamSupport.stream(x.keySet())
                        .map(id -> ARouter.getInstance()
                                .build("/navi/route")
                                .withInt("pathId", id))
                        .map(postcard -> (Fragment) postcard.navigation())
                        .collect(Collectors.toList());
            } else
            {
                fragments = Collections.emptyList();
            }
            binding.setIsShowRoutes(!fragments.isEmpty());
            binding.setRouteAdapter(wrapToAdapter(fragments));
        });
        naviViewModel.getCurrentShow().observe(this, data -> {
            if (data == null || data == NaviViewModel.NO_SELECT)
            {
                binding.setIsShowSelect(false);
            } else
            {
                binding.setIsShowSelect(true);
            }
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

        requireActivity().addOnBackPressedCallback(this, this);
    }

    private PagerAdapter wrapToAdapter(List<Fragment> fragments)
    {
        return new FragmentPagerAdapter(getChildFragmentManager())
        {

            @NonNull
            @Override
            public Fragment getItem(int position)
            {
                return fragments.get(position);
            }

            @Override
            public int getCount()
            {
                return fragments.size();
            }
        };
    }

    @Override
    public boolean handleOnBackPressed()
    {
        Boolean isSelect = binding.getIsShowSelect();
        if (isSelect != null && isSelect)
        {
            naviViewModel.getCurrentShow().setValue(NaviViewModel.NO_SELECT);
            return true;
        }
        Map<Integer, RouteInfo> routeInfos = naviViewModel.getRoutes().getValue();
        if (routeInfos != null && !routeInfos.isEmpty())
        {
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
    }
}