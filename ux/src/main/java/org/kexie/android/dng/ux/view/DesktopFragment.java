package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.databinding.RxEvent;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopBinding;
import org.kexie.android.dng.ux.viewmodel.DesktopViewModel;
import org.kexie.android.dng.ux.viewmodel.InfoViewModel;
import org.kexie.android.dng.ux.viewmodel.entity.Function;
import org.kexie.android.dng.ux.viewmodel.entity.LiteUser;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

@Mapping("dng/ux/main")
public class DesktopFragment extends Fragment
{
    private FragmentDesktopBinding binding;

    private DesktopViewModel viewModel;

    private InfoViewModel infoViewModel;


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_desktop, container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(false);

        viewModel = ViewModelProviders.of(this)
                .get(DesktopViewModel.class);

        infoViewModel = ViewModelProviders.of(this)
                .get(InfoViewModel.class);

        binding.setLifecycleOwner(this);

        getLifecycle().addObserver(viewModel);
        //dataBinding
        GenericQuickAdapter<Function> functionsAdapter
                = new GenericQuickAdapter<>(R.layout.item_desktop_function, BR.function);

        functionsAdapter.setOnItemClickListener((adapter, view1, position) -> {
            String uri = Objects.requireNonNull(functionsAdapter.getItem(position)).uri;
            Request request = new Request.Builder().uri(uri).build();
            jumpTo(request);
        });

        viewModel.functions.observe(this,functionsAdapter::setNewData);

        binding.setFunctions(functionsAdapter);

        Map<String, View.OnClickListener> actions = new ArrayMap<String, View.OnClickListener>()
        {
            {
                put("个人信息", v -> jumpTo(new Request.Builder().uri("dng/ux/info").build()));
                put("导航", v -> jumpTo(new Request.Builder().uri("dng/navi/query").build()));
            }
        };

        binding.setActions(actions);

        binding.setFunctions(functionsAdapter);
        //liveData
        Transformations.map(infoViewModel.user,
                input -> new LiteUser(input.headImage, input.username, input.carNumber))
                .observe(this, binding::setUser);



        viewModel.time.observe(this, binding::setTime);
        //rx
        viewModel.onError
                .as(RxEvent.bind(this))
                .subscribe(s -> Toasty.error(requireContext(), s).show());

        viewModel.onSuccess
                .as(RxEvent.bind(this))
                .subscribe(s -> Toasty.success(requireContext(), s).show());
    }

    private void jumpTo(Request request)
    {
        requireFragmentManager()
                .beginTransaction()
                .add(getId(), Mapper.getOn(this, request))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

}
