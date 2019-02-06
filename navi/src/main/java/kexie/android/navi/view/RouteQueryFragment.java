package kexie.android.navi.view;

import android.animation.ValueAnimator;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.amap.api.services.help.Tip;

import org.kexie.android.databinding.BT;

import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import kexie.android.common.widget.ProgressWidget;
import kexie.android.navi.R;
import kexie.android.navi.databinding.FragmentRouteQueryBinding;
import kexie.android.navi.entity.SdkRoute;
import kexie.android.navi.viewmodel.RouteQueryViewModel;

public class RouteQueryFragment extends Fragment
{
    private static final String WAIT_QUERY = "wait query";

    private FragmentRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_route_query, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding.setLifecycleOwner(this);
        binding.setTips(null);
        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);

        viewModel.getRoutes().observe(this,
                new Observer<List<SdkRoute>>()
                {
                    @Override
                    public void onChanged(@Nullable List<SdkRoute> routes)
                    {
                        drawerAnimating(!BT.isEmpty(routes));
                        binding.setRoutes(routes);
                    }
                });
        viewModel.getTips().observe(this,
                new Observer<List<Tip>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Tip> tips)
                    {
                        if (BT.isEmpty(tips))
                        {
                            Toasty.error(Objects.requireNonNull(getContext()).getApplicationContext(),
                                    "发生错误，请检查网络连接")
                                    .show();
                        } else
                        {
                            Toasty.success(Objects.requireNonNull(getContext()).getApplicationContext(),
                                    "查询成功").show();
                        }
                        binding.setTips(tips);
                    }
                });
        viewModel.getLoading().observe(this,
                new Observer<Boolean>()
                {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean)
                    {
                        if (aBoolean != null && aBoolean)
                        {
                            ProgressWidget progressWidget = new ProgressWidget();
                            progressWidget.show(Objects.requireNonNull(getFragmentManager()),
                                    WAIT_QUERY);
                        } else
                        {
                            ProgressWidget progressWidget
                                    = (ProgressWidget) Objects.requireNonNull(getFragmentManager())
                                    .findFragmentByTag(WAIT_QUERY);
                            if (progressWidget != null)
                            {
                                progressWidget.dismiss();
                            }
                        }
                    }
                });
        viewModel.getQueryText().observe(this,
                new Observer<String>()
                {
                    @Override
                    public void onChanged(@Nullable String s)
                    {
                        if (TextUtils.isEmpty(s))
                        {
                            Toasty.warning(getContext().getApplicationContext(),
                                    "搜索内容为空").show();
                        } else
                        {
                            binding.setQueryText(s);
                        }
                    }
                });
        binding.setActions(viewModel.getActions());
    }

    private void drawerAnimating(boolean enable)
    {
        final LinearLayout.LayoutParams layoutParams
                = (LinearLayout.LayoutParams) binding.vpPager
                .getLayoutParams();
        ValueAnimator animator = (ValueAnimator) binding.vpPager.getTag();
        if (animator != null)
        {
            animator.cancel();
        }
        final int size = 500;
        animator = ValueAnimator.ofFloat(0, size);
        animator.setDuration(1000);
        ValueAnimator.AnimatorUpdateListener updateListener;
        if (enable)
        {
            updateListener = new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    layoutParams.weight = 0.618f
                            * (float) animation.getAnimatedValue()
                            / size;
                    binding.vpPager.setLayoutParams(layoutParams);
                }
            };
        } else
        {
            updateListener = new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    layoutParams.weight = 0.618f
                            * (1f - (float) animation.getAnimatedValue())
                            / size;
                    binding.vpPager.setLayoutParams(layoutParams);
                }
            };
        }
        animator.addUpdateListener(updateListener);
        animator.start();
        binding.vpPager.setTag(animator);
    }
}