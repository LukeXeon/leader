package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentNaviRunningBinding;
import org.kexie.android.dng.navi.viewmodel.RunningViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Route(path = "/navi/running")
public final class RunningFragment extends Fragment
{
    private FragmentNaviRunningBinding binding;

    private RunningViewModel runningViewModel;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_navi_running,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        runningViewModel = ViewModelProviders.of(requireParentFragment())
                .get(RunningViewModel.class);

        runningViewModel.getRunningInfo().observe(this, naviInfo -> {
            binding.myTrafficBar.update(
                    naviInfo.getAllLength(),
                    naviInfo.getPathRetainDistance(),
                    naviInfo.getTrafficStatuses());
            binding.iconNextTurnTip.setIconType(naviInfo.getIconType());
            binding.textNextRoadName.setText(naviInfo.getNextRoadName());
            binding.textNextRoadDistance.setText(naviInfo.getNextRoadDistance());
        });
        runningViewModel.getLaneInfo().observe(this, aMapLaneInfo -> {
            if (aMapLaneInfo != null)
            {
                binding.myDriveWayView.setVisibility(View.VISIBLE);
                binding.myDriveWayView.buildDriveWay(aMapLaneInfo);

            } else
            {
                binding.myDriveWayView.hide();
            }
        });
        runningViewModel.getCrossImage().observe(this, data -> {
            if (data != null)
            {
                binding.myZoomInIntersectionView.setIntersectionBitMap(data);
                binding.myZoomInIntersectionView.setVisibility(View.VISIBLE);
            } else
            {
                binding.myZoomInIntersectionView.setVisibility(View.GONE);
            }
        });
        runningViewModel.getOnInfo().observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(x -> Toasty.info(requireContext(), x).show());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        binding.myZoomInIntersectionView.recycleResource();
    }
}
