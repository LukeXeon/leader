package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentDesktopNeoBinding;
import org.kexie.android.dng.ux.widget.NeoDesktopAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


@Route(path = PR.ux.desktop)
public class NeoDesktopFragment extends Fragment {

    private FragmentDesktopNeoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return (binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_desktop_neo, container,
                false))
                .getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.list.setAdapter(new NeoDesktopAdapter(getLifecycle(), action -> {
            Postcard postcard = ARouter.getInstance().build(action);
            if (PR.ux.apps.equals(action)) {
                jumpToNoHide(postcard);
            } else {
                jumpTo(postcard);
            }
        }));
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
