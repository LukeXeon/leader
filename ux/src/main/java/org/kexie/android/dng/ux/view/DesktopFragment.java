package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopBinding;
import org.kexie.android.dng.ux.widget.DesktopController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


@Route(path = Module.Ux.desktop)
public class DesktopFragment extends Fragment {

    private FragmentDesktopBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return (binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_desktop, container,
                false))
                .getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.list.setAdapter(new DesktopController(getLifecycle(),
                action -> jumpTo(ARouter.getInstance().build(action))));
    }

    private FragmentTransaction getTransaction(Postcard postcard) {
        Fragment fragment = (Fragment) postcard.navigation();
        return requireFragmentManager()
                .beginTransaction()
                .add(getId(), fragment, postcard.getPath())
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    private void jumpTo(Postcard postcard) {
        getTransaction(postcard).hide(this)
                .commitAllowingStateLoss();
    }
}
