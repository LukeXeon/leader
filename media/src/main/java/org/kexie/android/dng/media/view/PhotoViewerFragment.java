package org.kexie.android.dng.media.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import org.kexie.android.dng.common.widget.AnimationAdapter;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentPhotoViewerBinding;
import org.kexie.android.dng.media.util.Utils;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class PhotoViewerFragment extends Fragment {

    private FragmentPhotoViewerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_photo_viewer,
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
        binding.getRoot().setOnTouchListener((v, event) -> true);
        binding.setInfo(requireArguments().getParcelable("media"));
        binding.setHide(false);

        Fragment target = getTargetFragment();

        if (target != null) {
            Map<String, View.OnClickListener> actions = new ArrayMap<>();
            actions.put("back", v -> requireActivity().onBackPressed());
            actions.put("hide", v -> doHideAnimation());
            binding.setActions(actions);
        }
    }

    private void doHideAnimation() {
        AlphaAnimation animation
                = (AlphaAnimation) binding.header.getTag();
        if (animation != null) {
            animation.cancel();
        }
        if (Utils.safeUnBox(binding.getHide())) {
            //show
            binding.header.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setDuration(200);
            binding.header.startAnimation(alphaAnimation);
            binding.header.setTag(alphaAnimation);
        } else {
            //hide
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setAnimationListener(new AnimationAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (Utils.safeUnBox(binding.getHide())) {
                        binding.header.setVisibility(View.GONE);
                    }
                }
            });
            alphaAnimation.setDuration(200);
            binding.header.startAnimation(alphaAnimation);
            binding.header.setTag(alphaAnimation);
        }
        binding.setHide(!Utils.safeUnBox(binding.getHide()));
    }
}