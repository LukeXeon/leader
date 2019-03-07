package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.model.Query;
import org.kexie.android.dng.navi.viewmodel.NaviViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

@Route(path = "/navi/main")
public final class HostFragment extends Fragment
{

    private NaviViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        Logger.d(this);
        return inflater.inflate(R.layout.fragment_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(false);

        viewModel = ViewModelProviders.of(this).get(NaviViewModel.class);
        viewModel.isNavigating().observe(this, data -> {
            Postcard postcard;
            if (data)
            {
                postcard = ARouter.getInstance().build("/navi/navi");
            } else
            {
                postcard = ARouter.getInstance().build("/navi/query");
            }
            Fragment fragment = (Fragment) postcard.navigation();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_content, fragment)
                    .commit();
        });

        Bundle bundle = getArguments();
        if (bundle == null)
        {
            viewModel.isNavigating().setValue(false);
        } else
        {
            Query query = bundle.getParcelable("query");
            if (query == null)
            {
                viewModel.isNavigating().setValue(false);
            } else
            {
                viewModel.isNavigating().setValue(true);
                viewModel.query(query);
            }
        }
    }
}
