package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopBinding;
import org.kexie.android.dng.ux.viewmodel.AppsViewModel;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = "/ux/main")
public class DesktopFragment extends Fragment
{
    private FragmentDesktopBinding binding;

    private DesktopViewModel desktopViewModel;

    private InfoViewModel infoViewModel;

    private AppsViewModel appsViewModel;

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

        GenericQuickAdapter<Function> functionsAdapter
                = new GenericQuickAdapter<>(R.layout.item_desktop_function, BR.function);
        functionsAdapter.setOnItemClickListener((adapter, view1, position) -> {
            String uri = Objects.requireNonNull(functionsAdapter.getItem(position)).uri;
            Postcard postcard = ARouter.getInstance().build(uri);
            if ("/ux/apps".equals(uri))
            {
                jumpToNoHide(postcard);
            } else
            {
                jumpTo(postcard);
            }
        });

        desktopViewModel = ViewModelProviders.of(this)
                .get(DesktopViewModel.class);
        desktopViewModel.functions.observe(this, functionsAdapter::setNewData);
        desktopViewModel.time.observe(this, binding::setTime);
        desktopViewModel.onError.observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(requireContext(), s).show());
        desktopViewModel.onSuccess.observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(requireContext(), s).show());
        getLifecycle().addObserver(desktopViewModel);

        appsViewModel = ViewModelProviders.of(requireActivity())
                .get(AppsViewModel.class);

        infoViewModel = ViewModelProviders.of(requireActivity())
                .get(InfoViewModel.class);
        Transformations.map(infoViewModel.user,
                input -> new LiteUser(input.headImage, input.username, input.carNumber))
                .observe(this, binding::setUser);

        Map<String, View.OnClickListener> actions
                = new ArrayMap<String, View.OnClickListener>()
        {
            {
                put("个人信息", v -> jumpToNoHide(ARouter.getInstance().build("/ux/content")));
                put("导航", v -> jumpTo(ARouter.getInstance().build("/navi/navi")));
            }
        };
        binding.setActions(actions);
        binding.setLifecycleOwner(this);
        binding.setFunctions(functionsAdapter);
        binding.setFunctions(functionsAdapter);

    }

    private FragmentTransaction getTransaction(Postcard postcard)
    {
        Fragment fragment = (Fragment) postcard.navigation();
        return requireFragmentManager()
                .beginTransaction()
                .add(getId(), fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    private void jumpToNoHide(Postcard postcard)
    {
        getTransaction(postcard).commit();
    }

    private void jumpTo(Postcard postcard)
    {
        getTransaction(postcard).hide(this).commit();
    }
}
