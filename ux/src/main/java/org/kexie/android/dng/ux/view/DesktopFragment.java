package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopBinding;
import org.kexie.android.dng.ux.viewmodel.AppsViewModel;
import org.kexie.android.dng.ux.viewmodel.DesktopViewModel;
import org.kexie.android.dng.ux.viewmodel.InfoViewModel;
import org.kexie.android.dng.ux.viewmodel.entity.Function;
import org.kexie.android.dng.ux.viewmodel.entity.LiteUser;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static androidx.lifecycle.Lifecycle.Event;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = PR.ux.desktop)
public class DesktopFragment extends Fragment {
    private FragmentDesktopBinding binding;

    private DesktopViewModel desktopViewModel;

    private InfoViewModel infoViewModel;

    private AppsViewModel appsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appsViewModel = ViewModelProviders.of(requireActivity())
                .get(AppsViewModel.class);
        infoViewModel = ViewModelProviders.of(requireActivity())
                .get(InfoViewModel.class);
        desktopViewModel = ViewModelProviders.of(this)
                .get(DesktopViewModel.class);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_desktop, container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GenericQuickAdapter<Function> functionsAdapter
                = new GenericQuickAdapter<>(R.layout.item_desktop_function, BR.function);
        functionsAdapter.setOnItemClickListener(RxOnClickWrapper
                .create(BaseQuickAdapter.OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view1, position) -> {
                    Function function = (Function) adapter.getItem(position);
                    if (function == null) {
                        return;
                    }
                    String uri = function.uri;
                    Postcard postcard = ARouter.getInstance().build(uri);
                    if (PR.ux.apps.equals(uri)) {
                        jumpToNoHide(postcard);
                    } else {
                        jumpTo(postcard);
                    }
                })
                .build());

        desktopViewModel.functions.observe(this, functionsAdapter::setNewData);
        desktopViewModel.time.observe(this, binding::setTime);
        desktopViewModel.onError.observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(requireContext(), s).show());
        desktopViewModel.onSuccess.observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(requireContext(), s).show());
        getLifecycle().addObserver(desktopViewModel);

        Transformations.map(infoViewModel.user,
                input -> new LiteUser(input.headImage, input.username, input.carNumber))
                .observe(this, binding::setUser);

        Map<String, View.OnClickListener> actions
                = new ArrayMap<String, View.OnClickListener>() {
            {
                put("个人信息", RxOnClickWrapper
                        .create(View.OnClickListener.class)
                        .lifecycle(getLifecycle())
                        .inner(v -> jumpToNoHide(ARouter.getInstance().build(PR.ux.content)))
                        .build());
                put("导航", RxOnClickWrapper
                        .create(View.OnClickListener.class)
                        .lifecycle(getLifecycle())
                        .inner(v -> jumpToNoHide(ARouter.getInstance().build(PR.navi.navi)))
                        .build());
            }
        };

        binding.setActions(actions);
        binding.setLifecycleOwner(this);
        binding.setFunctions(functionsAdapter);
        binding.setFunctions(functionsAdapter);

    }

    private FragmentTransaction getTransaction(Postcard postcard) {
        Fragment fragment = (Fragment) postcard.navigation();
        return requireFragmentManager()
                .beginTransaction()
                .add(getId(), fragment, postcard.getPath())
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    private void jumpToNoHide(Postcard postcard) {
        getTransaction(postcard).commitAllowingStateLoss();
    }

    private void jumpTo(Postcard postcard) {
        getTransaction(postcard).hide(this)
                .commitAllowingStateLoss();
    }
}
