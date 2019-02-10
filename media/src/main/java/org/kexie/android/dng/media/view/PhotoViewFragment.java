package org.kexie.android.dng.media.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentPhotoViewBinding;
import org.kexie.android.dng.media.entity.MediaInfo;
import org.kexie.android.dng.media.viewmodel.MediaManagedViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import eightbitlab.com.blurview.RenderScriptBlur;
import es.dmoral.toasty.Toasty;

public class PhotoViewFragment extends Fragment
{

    private FragmentPhotoViewBinding binding;

    public interface Callback
    {
        void callback();
    }


    public static PhotoViewFragment newInstance(MediaInfo mediaInfo,
                                                Callback callback)
    {
        Bundle args = new Bundle();
        args.putParcelable("info", mediaInfo);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        fragment.callback = callback;
        return fragment;
    }

    private Callback callback;


    @SuppressLint("ClickableViewAccessibility")
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
        binding.setLifecycleOwner(this);
        binding.getRoot().setOnTouchListener((v, event) -> true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setInfo(getArguments().getParcelable("info"));
        MediaManagedViewModel viewModel = ViewModelProviders.of(this)
                .get(MediaManagedViewModel.class);
        binding.blurView.setupWith(binding.photo)
                .setFrameClearDrawable(
                        getActivity()
                                .getWindow()
                                .getDecorView()
                                .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
        binding.setHide(false);
        binding.setActions(new ArrayMap<String, View.OnClickListener>()
        {
            {
                View.OnClickListener a1 = v -> getActivity().onBackPressed();
                put("back", a1);
                put("delete", v -> {
                    if (viewModel.delete(binding.getInfo()))
                    {
                        Toasty.success(getContext(), "删除成功").show();
                    } else
                    {
                        Toasty.error(getContext(), "删除失败").show();
                    }
                    a1.onClick(v);
                    if (callback != null)
                    {
                        callback.callback();
                    }
                });
                put("hide", v -> {
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
                        alphaAnimation.setAnimationListener(new Animation.AnimationListener()
                        {
                            @Override
                            public void onAnimationStart(Animation animation)
                            {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation)
                            {
                                if (binding.getHide())
                                {
                                    binding.blurView.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation)
                            {

                            }
                        });
                        alphaAnimation.setDuration(200);
                        binding.blurView.startAnimation(alphaAnimation);
                        binding.blurView.setTag(alphaAnimation);
                    }
                    binding.setHide(!binding.getHide());
                });
            }
        });
    }
}
