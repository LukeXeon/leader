package kexie.android.navi.view;

import android.animation.ValueAnimator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.amap.api.services.help.Tip;

import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import kexie.android.common.util.T;
import kexie.android.common.widget.ProgressDialog;
import kexie.android.navi.R;
import kexie.android.navi.databinding.ActivityRouteQueryBinding;
import kexie.android.navi.entity.Route;
import kexie.android.navi.viewmodel.RouteQueryViewModel;

public class RouteQueryActivity extends AppCompatActivity
{
    private static final String WAIT_QUERY = "wait query";

    private ActivityRouteQueryBinding binding;
    private RouteQueryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_route_query);

        binding.setLifecycleOwner(this);

        binding.setTips(null);

        viewModel = ViewModelProviders.of(this)
                .get(RouteQueryViewModel.class);

        viewModel.getRoutes().observe(this,
                new Observer<List<Route>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Route> routes)
                    {
                        setListViewAnimation(!T.isEmpty(routes));
                        binding.setRoutes(routes);
                    }
                });
        viewModel.getTips().observe(this,
                new Observer<List<Tip>>()
                {
                    @Override
                    public void onChanged(@Nullable List<Tip> tips)
                    {
                        if (T.isEmpty(tips))
                        {
                            Toasty.error(getApplicationContext(),
                                    "发生错误，请检查网络连接")
                                    .show();
                        } else
                        {
                            Toasty.success(getApplicationContext(),
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
                            ProgressDialog progressDialog = new ProgressDialog();
                            progressDialog.show(getSupportFragmentManager(),
                                    WAIT_QUERY);
                        } else
                        {
                            ProgressDialog progressDialog
                                    = (ProgressDialog) getSupportFragmentManager()
                                    .findFragmentByTag(WAIT_QUERY);
                            if (progressDialog != null)
                            {
                                progressDialog.dismiss();
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
                            Toasty.warning(getApplicationContext(),
                                    "搜索内容为空").show();
                        } else
                        {
                            binding.setQueryText(s);
                        }
                    }
                });
        viewModel.getActions().observe(this,
                new Observer<Map<String, View.OnClickListener>>()
                {
                    @Override
                    public void onChanged(@Nullable Map<String, View.OnClickListener> actions)
                    {
                        binding.setActions(actions);
                    }
                });
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

    public static void startOf(Context context)
    {
        context.startActivity(new Intent(context, RouteQueryActivity.class));
    }
}
