package org.kexie.android.dng.navi.view;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureSupportMapFragment;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.view.RouteOverLay;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.widget.ProgressFragment;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo;
import org.kexie.android.dng.navi.widget.ScaleTransformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.OnRebindCallback;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java8.util.stream.Collectors;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/navi/query")
public final class QueryFragment extends Fragment implements OnBackPressedCallback
{
    private FragmentQueryBinding binding;

    private NaviViewModel naviViewModel;

    private InputTipViewModel inputTipViewModel;

    private TextureSupportMapFragment mapFragment;

    private AMap mapController;

    private GenericQuickAdapter<InputTip> inputTipQuickAdapter;

    @NonNull
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        naviViewModel = ViewModelProviders.of(this)
                .get(NaviViewModel.class);
        inputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel.class);

        inputTipQuickAdapter = new GenericQuickAdapter<>(R.layout.item_tip, BR.inputTip);
        inputTipQuickAdapter.setOnItemClickListener((adapter, view1, position) -> {
            if (mapController == null)
            {
                return;
            }
            InputTip inputTip = Objects.requireNonNull(inputTipQuickAdapter.getItem(position));
            Location location = mapController.getMyLocation();
            Point point = Point.form(location.getLongitude(), location.getLatitude());
            naviViewModel.query(inputTip, point);
        });

        binding.setOnBack(v -> naviViewModel.select(NaviViewModel.Companion.getNO_SELECT()));
        binding.setOnToNavi(v -> {
            clearView();
            Request request = new Request.Builder()
                    .uri("dng/navi/navi")
                    .code(1)
                    .build();
            Fragment naviFragment = Mapper.getOn(this, request);
            requireFragmentManager().beginTransaction()
                    .hide(this)
                    .add(getId(), naviFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        });
        binding.setIsShowSelect(false);
        binding.setIsShowQuery(false);
        binding.setStartQuery(v -> binding.setIsShowQuery(true));
        binding.setLifecycleOwner(this);
        binding.routePager.setPageTransformer(false, new ScaleTransformer());
        binding.routePager.setOffscreenPageLimit(3);
        binding.setQueryText(inputTipViewModel.getQueryText());
        binding.setTipsAdapter(inputTipQuickAdapter);
        binding.addOnRebindCallback(new OnRebindCallback()
        {
            @Override
            public void onBound(ViewDataBinding binding)
            {
                ViewPager pager = QueryFragment.this.binding.routePager;
                PagerAdapter adapter = pager.getAdapter();
                if (adapter != null)
                {
                    int count = adapter.getCount();
                    int[] arr = IntStreams.iterate(0, x -> x < count, x -> x + 1)
                            .toArray();
                    pager.setCurrentItem(median(arr));
                }
            }

            private int median(int[] nums)
            {
                if (nums.length == 0)
                    return 0;
                int start = 0;
                int end = nums.length - 1;
                int index = partition(nums, start, end);
                if (nums.length % 2 == 0)
                {
                    while (index != nums.length / 2 - 1)
                    {
                        if (index > nums.length / 2 - 1)
                        {
                            index = partition(nums, start, index - 1);
                        } else
                        {
                            index = partition(nums, index + 1, end);
                        }
                    }
                } else
                {
                    while (index != nums.length / 2)
                    {
                        if (index > nums.length / 2)
                        {
                            index = partition(nums, start, index - 1);
                        } else
                        {
                            index = partition(nums, index + 1, end);
                        }
                    }
                }
                return nums[index];
            }

            private int partition(int nums[], int start, int end)
            {
                int left = start;
                int right = end;
                int pivot = nums[left];
                while (left < right)
                {
                    while (left < right && nums[right] >= pivot)
                    {
                        right--;
                    }
                    if (left < right)
                    {
                        nums[left] = nums[right];
                        left++;
                    }
                    while (left < right && nums[left] <= pivot)
                    {
                        left++;
                    }
                    if (left < right)
                    {
                        nums[right] = nums[left];
                        right--;
                    }
                }
                nums[left] = pivot;
                return left;
            }
        });

        mapFragment = TextureSupportMapFragment.newInstance();
        mapController = mapFragment.getMap();
        mapController.setOnMapLoadedListener(() -> {
            MyLocationStyle myLocationStyle = new MyLocationStyle().interval(1000);
            mapController.setMyLocationStyle(myLocationStyle);
            mapController.setOnMyLocationChangeListener(location -> {
                MyLocationStyle myLocationStyle1 = new MyLocationStyle()
                        .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
                mapController.setMyLocationStyle(myLocationStyle1);
                mapController.setOnMyLocationChangeListener(null);
            });
            mapController.setMyLocationEnabled(true);
            mapController.getUiSettings().setMyLocationButtonEnabled(true);
            mapController.setOnMapLoadedListener(null);
        });

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.map_view, Objects.requireNonNull(Fragment.class.cast(mapFragment)))
                .commitAllowingStateLoss();

        inputTipViewModel.getInputTips()
                .observe(this, data -> {
                    binding.setIsShowTips(data != null && !data.isEmpty());
                    inputTipQuickAdapter.setNewData(data);
                });
        inputTipViewModel.getQueryText()
                .observe(this, data -> {
                    binding.setIsShowQuery(!TextUtils.isEmpty(data));
                    inputTipViewModel.query(data);
                });

        naviViewModel.getRoutes()
                .observe(this, x -> {
                    List<Fragment> fragments;
                    if (x != null)
                    {
                        fragments = StreamSupport.stream(x.keySet())
                                .map(id -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("pathId", id);
                                    return bundle;
                                }).map(bundle -> new Request.Builder()
                                        .uri("dng/navi/route")
                                        .bundle(bundle)
                                        .build())
                                .map(request -> Mapper.getOn(this, request))
                                .collect(Collectors.toList());
                    } else
                    {
                        fragments = Collections.emptyList();
                    }
                    binding.setIsShowRoutes(!fragments.isEmpty());
                    binding.setRouteAdapter(wrapToAdapter(fragments));
                });
        naviViewModel.getSelect()
                .observe(this, select -> {
                    if (select != null && select != NaviViewModel.Companion.getNO_SELECT())
                    {
                        if (mapController == null)
                        {
                            return;
                        }
                        binding.setIsShowSelect(true);
                        Map<Integer, RouteInfo> routeInfos = naviViewModel.getRoutes()
                                .getValue();
                        if (routeInfos != null)
                        {
                            Context context = requireContext().getApplicationContext();
                            mapController.moveCamera(CameraUpdateFactory.changeTilt(0));
                            StreamSupport.stream(routeInfos.entrySet())
                                    .forEach(entry -> {
                                        RouteOverLay routeOverLay = new RouteOverLay(mapController,
                                                entry.getValue().path,
                                                context);
                                        routeOverLay.setTrafficLine(true);
                                        routeOverLay.setArrowOnRoute(true);
                                        routeOverLay.setLightsVisible(true);
                                        routeOverLay.setTrafficLightsVisible(true);
                                        if (entry.getKey() == (int) select)
                                        {
                                            routeOverLay.setTransparency(1);
                                            routeOverLay.setZindex(1);
                                            routeOverLay.addToMap();
                                            routeOverLay.zoomToSpan(300);
                                        } else
                                        {
                                            routeOverLay.setTransparency(0.4f);
                                            routeOverLay.addToMap();
                                        }
                                    });
                        }
                    } else
                    {
                        Location location = mapController.getMyLocation();
                        mapController.clear();
                        mapController.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(Point.form(
                                        location.getLongitude(),
                                        location.getLatitude())
                                        .unBox(LatLng.class), 10));
                        binding.setIsShowSelect(false);
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

    @SuppressWarnings("StatementWithEmptyBody")
    private void clearView()
    {
        while (handleOnBackPressed()) ;
        getChildFragmentManager().beginTransaction()
                .remove(Objects.requireNonNull(Fragment.class.cast(mapFragment)))
                .commitAllowingStateLoss();
        mapFragment = null;
        mapController = null;
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
        if (!isHidden())
        {
            Boolean isSelect = binding.getIsShowSelect();
            if (isSelect != null && isSelect)
            {
                naviViewModel.select(NaviViewModel.Companion.getNO_SELECT());
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
        }
        return false;
    }
}