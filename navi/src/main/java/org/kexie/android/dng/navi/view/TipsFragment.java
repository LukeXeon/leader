package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.common.databinding.RxOnClick;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviQueryTipsBinding;
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
public final class TipsFragment extends Fragment
{
    private FragmentNaviQueryTipsBinding binding;

    private InputTipViewModel inputTipViewModel;

    private QueryViewModel queryViewModel;

    private GenericQuickAdapter<InputTip> inputTipQuickAdapter;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navi_query_tips,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);
        requireActivity().addOnBackPressedCallback(this, () -> {
            if (isHidden())
            {
                return false;
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

        queryViewModel = ViewModelProviders.of(requireParentFragment().requireParentFragment())
                .get(QueryViewModel.class);
        inputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel.class);

        inputTipQuickAdapter = new GenericQuickAdapter<>(R.layout.item_tip, BR.inputTip);
        inputTipQuickAdapter.setOnItemClickListener(new GenericQuickAdapter.RxOnItemClick<InputTip>(
                this, (adapter, view1, position) -> {
            InputTip inputTip = Objects.requireNonNull(adapter.getItem(position));
            queryViewModel.query(inputTip);
        }));

        binding.setIsShowTips(false);
        binding.setIsShowQuery(false);
        binding.setStartQuery(new RxOnClick(this, v -> binding.setIsShowQuery(true)));
        binding.setLifecycleOwner(this);
        binding.setQueryText(inputTipViewModel.getQueryText());
        binding.setTipsAdapter(inputTipQuickAdapter);

        inputTipViewModel.getInputTips()
                .observe(this, data -> {
                    boolean isShow = data != null && !data.isEmpty();
                    binding.setIsShowTips(isShow);
                    inputTipQuickAdapter.setNewData(data);
                });
        inputTipViewModel.getQueryText()
                .observe(this, data -> {
                    binding.setIsShowQuery(!TextUtils.isEmpty(data));
                    inputTipViewModel.query(data);
                });


        queryViewModel.getRoutes().observe(this, data -> {
            if (data != null && !data.isEmpty()
                    && StreamSupport.stream(requireFragmentManager().getFragments())
                    .noneMatch(fragment -> PR.navi.query_select.equals(fragment.getTag())))
            {
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
                        .commit();
            }
        });
        queryViewModel.getOnError().mergeWith(inputTipViewModel.getOnError())
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, ON_DESTROY)))
                .subscribe(data -> Toasty.error(requireContext(), data).show());
        queryViewModel.getOnSuccess().mergeWith(inputTipViewModel.getOnSuccess())
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, ON_DESTROY)))
                .subscribe(data -> Toasty.success(requireContext(), data).show());
    }
}