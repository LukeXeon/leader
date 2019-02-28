package org.kexie.android.common.widget;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public final class BlurViewUtil
{
    private BlurViewUtil()
    {
        throw new AssertionError();
    }

    public static void initUseFragment(Fragment fragment, BlurView blurView)
    {
        blurView.setupWith((ViewGroup) fragment.requireView())
                .setFrameClearDrawable(fragment.requireActivity().getWindow()
                        .getDecorView()
                        .getBackground())
                .setBlurAlgorithm(new RenderScriptBlur(fragment.requireContext()))
                .setBlurRadius(20f)
                .setHasFixedTransformationMatrix(true);
    }
}
