package org.kexie.android.dng.media.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.common.widget.AnimationAdapter;
import org.kexie.android.dng.common.widget.RxOnClickWrapper;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentPhotoViewBinding;
import org.kexie.android.dng.media.viewmodel.MediaBrowseViewModel;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;
import es.dmoral.toasty.Toasty;

@Route(path = PR.media.photo)
public class PhotoViewFragment extends Fragment
{

    private FragmentPhotoViewBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_photo_view,
                container,
                false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setLifecycleOwner(this);
        binding.blurView.setupWith(binding.photo)
                .setFrameClearDrawable(
                        requireActivity()
                                .getWindow()
                                .getDecorView()
                                .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
        binding.getRoot().setOnTouchListener((v, event) -> true);
        binding.setInfo(requireArguments().getParcelable("media"));
        binding.setHide(false);

        Fragment target = getTargetFragment();

        if (target != null) {
            MediaBrowseViewModel viewModel = ViewModelProviders.of(target)
                    .get(MediaBrowseViewModel.class);

            Map<String, View.OnClickListener> actions
                    = new ArrayMap<String, View.OnClickListener>() {
                {
                    put("back", RxOnClickWrapper
                            .create(View.OnClickListener.class)
                            .lifecycle(getLifecycle())
                            .inner(v -> requireActivity().onBackPressed())
                            .build());
                    put("delete", RxOnClickWrapper
                            .create(View.OnClickListener.class)
                            .lifecycle(getLifecycle())
                            .inner(v -> {
                                if (viewModel.delete(binding.getInfo())) {
                                    Fragment fragment = getTargetFragment();
                                    if (fragment != null) {
                                        fragment.onActivityResult(getTargetRequestCode(),
                                                Activity.RESULT_FIRST_USER, new Intent().putExtras(requireArguments()));
                                    }
                                    Toasty.success(requireContext(), "删除成功").show();
                                    requireActivity().onBackPressed();
                                } else {
                                    Toasty.error(requireContext(), "删除失败").show();
                                }
                            })
                            .build());
                    put("hide", RxOnClickWrapper
                            .create(View.OnClickListener.class)
                            .lifecycle(getLifecycle())
                            .inner(v -> doHideAnimation())
                            .build());
                }
            };

            binding.setActions(actions);
        }
    }

    private void doHideAnimation()
    {
        AlphaAnimation animation
                = (AlphaAnimation) binding.blurView.getTag();
        if (animation != null)
        {
            animation.cancel();
        }
        if (binding.getHide())//show
        {
            binding.blurView.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setDuration(200);
            binding.blurView.startAnimation(alphaAnimation);
            binding.blurView.setTag(alphaAnimation);
        } else//hide
        {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setAnimationListener(new AnimationAdapter()
            {
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    if (binding.getHide())
                    {
                        binding.blurView.setVisibility(View.GONE);
                    }
                }
            });
            alphaAnimation.setDuration(200);
            binding.blurView.startAnimation(alphaAnimation);
            binding.blurView.setTag(alphaAnimation);
        }
        binding.setHide(!binding.getHide());
    }
}
