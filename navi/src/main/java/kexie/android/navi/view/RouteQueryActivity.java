package kexie.android.navi.view;

import android.animation.ValueAnimator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.amap.api.services.help.Tip;

import java.util.List;


import es.dmoral.toasty.Toasty;
import kexie.android.common.widget.ProgressDialog;
import kexie.android.navi.R;
import kexie.android.navi.adapter.RouteBindingAdapter;
import kexie.android.navi.databinding.ActivityRouteQueryBinding;
import kexie.android.navi.entity.Route;
import kexie.android.navi.viewmodel.RouteQueryViewModel;

public class RouteQueryActivity extends AppCompatActivity
{
    private static final String WAIT_QUERY = "wait query";

    private final RouteBindingAdapter routeBindingAdapter
            = new RouteBindingAdapter();
    private ActivityRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_route_query);

        binding.setLifecycleOwner(this);
        binding.setHandler(this);
        binding.setAdapter(routeBindingAdapter);

        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);

        viewModel.getRoutes().observe(this,
                new Observer<List<Route>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Route> routes)
                    {
                        setListViewAnimation(isEmptyList(routes));
                        routeBindingAdapter.setNewData(routes);
                    }
                });
        viewModel.getTips().observe(this,
                new Observer<List<Tip>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Tip> tips)
                    {
                        ProgressDialog progressDialog
                                = (ProgressDialog) getSupportFragmentManager()
                                .findFragmentByTag(WAIT_QUERY);
                        if (progressDialog != null)
                        {
                            progressDialog.dismiss();
                        }
                        if (isEmptyList(tips))
                        {
                            Toasty.error(getApplicationContext(),
                                    "发生错误，请检查网络连接").show();
                        } else
                        {

                        }
                    }
                });
    }

    private static <T> boolean isEmptyList(List<T> list)
    {
        return !(list != null && list.size() != 0);
    }

    private void setListViewAnimation(boolean enable)
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
        animator = ValueAnimator.ofFloat(0,size);
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

    public void beginQuery()
    {
        String text = "火车站";
        if (!TextUtils.isEmpty(text))
        {
            final ProgressDialog progressDialog = new ProgressDialog();
            progressDialog.show(getSupportFragmentManager(),
                    WAIT_QUERY);
            progressDialog.setMessage("正在拼命加载中，请稍等");
            viewModel.textQuery(text);
        } else
        {
            Toasty.warning(this, "搜索内容为空").show();
            binding.setQueryText("");
            viewModel.getRoutes().setValue(null);
        }
    }

    public static void startOf(Activity context)
    {
        context.startActivity(new Intent(context, RouteQueryActivity.class));
    }
}
