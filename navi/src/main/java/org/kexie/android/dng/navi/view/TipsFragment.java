package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviQueryTipsBinding;
import org.kexie.android.dng.navi.viewmodel.InputTipsViewModel;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java8.util.stream.StreamSupport;

import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = PR.navi.query_tips)
public final class TipsFragment extends Fragment {
    private FragmentNaviQueryTipsBinding binding;

    private InputTipsViewModel inputTipsViewModel;

    private QueryViewModel queryViewModel;

    private GenericQuickAdapter<InputTip> inputTipQuickAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputTipsViewModel = ViewModelProviders.of(this)
                .get(InputTipsViewModel.class);
        queryViewModel = ViewModelProviders.of(requireParentFragment().requireParentFragment())
                .get(QueryViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navi_query_tips,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addOnBackPressedCallback(this, () -> {
            if (isHidden()) {
                return false;
            }
            List<InputTip> inputTips = inputTipsViewModel.getInputTips().getValue();
            if (inputTips != null && !inputTips.isEmpty()) {
                inputTipsViewModel.clear();
                return true;
            }
            return false;
        });


        inputTipQuickAdapter = new GenericQuickAdapter<>(R.layout.item_tip, BR.inputTip);
        inputTipQuickAdapter.setOnItemClickListener(RxOnClickWrapper
                .create(BaseQuickAdapter.OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view1, position) -> {
                    InputTip inputTip = (InputTip) adapter.getItem(position);
                    if (inputTip == null) {
                        return;
                    }
                    queryViewModel.query(inputTip);
                })
                .build());

        binding.setIsShowQuery(false);
        binding.setStartQuery(RxOnClickWrapper.create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> binding.setIsShowQuery(true))
                .build());
        binding.setLifecycleOwner(this);
        binding.setQueryText(inputTipsViewModel.getQuery());
        binding.setTipsAdapter(inputTipQuickAdapter);

        inputTipsViewModel.getInputTips()
                .observe(this, data -> {
                    boolean isShow = data != null && !data.isEmpty();
                    binding.setIsShowTips(isShow);
                    inputTipQuickAdapter.setNewData(data);
                });
        inputTipsViewModel.getQuery()
                .observe(this, data -> {
                    binding.setIsShowQuery(!TextUtils.isEmpty(data));
                    inputTipsViewModel.query(data);
                });


        queryViewModel.getRoutes().observe(this, data -> {
            if (data != null && !data.isEmpty()
                    && StreamSupport.stream(requireFragmentManager().getFragments())
                    .noneMatch(fragment -> PR.navi.query_select.equals(fragment.getTag()))) {
                Fragment fragment = (Fragment) ARouter
                        .getInstance()
                        .build(PR.navi.query_select)
                        .navigation();
                requireFragmentManager()
                        .beginTransaction()
                        .hide(this)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .add(getId(), fragment, PR.navi.query_select)
                        .commitAllowingStateLoss();
            }
        });
        queryViewModel.getOnError().mergeWith(inputTipsViewModel.getOnError())
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, ON_DESTROY)))
                .subscribe(data -> Toasty.error(requireContext(), data).show());
        queryViewModel.getOnSuccess().mergeWith(inputTipsViewModel.getOnSuccess())
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, ON_DESTROY)))
                .subscribe(data -> Toasty.success(requireContext(), data).show());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            inputTipsViewModel.clear();
        }
    }
}