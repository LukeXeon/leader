package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding;
import org.kexie.android.dng.navi.viewmodel.QueryViewModel;
import org.kexie.android.dng.navi.widget.SimpleApplyAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
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

    private QueryViewModel viewModel;

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
        viewModel = ViewModelProviders.of(this)
                .get(QueryViewModel.class);

        GenericQuickAdapter<String> genericQuickAdapter
                = new GenericQuickAdapter<>(R.layout.item_tip,"tip");

        viewModel.setAdapter(genericQuickAdapter);

        viewModel.getRoutes()
                .observe(this, requests -> {
                    List<Fragment> fragments = StreamSupport.stream(requests)
                            .map(request -> Mapper.getOn(this, request))
                            .collect(Collectors.toList());
                    SimpleApplyAdapter adapter = new SimpleApplyAdapter(
                            getChildFragmentManager(), fragments);

                });

        viewModel.getOnErrorMessage()
                .as(autoDisposable(from(this)))
                .subscribe(s -> Toasty.error(getContext(), s).show());
        viewModel.getOnSuccessMessage()
                .as(autoDisposable(from(this)))
                .subscribe(s -> Toasty.success(getContext(), s).show());

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
}