package org.kexie.android.dng.common.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import io.alterac.blurkit.BlurLayout;

public class BlurFrameLayout extends BlurLayout {
    public BlurFrameLayout(Context context) {
        this(context, null);
    }

    public void setLifecycle(Lifecycle lifecycle) {
        lifecycle.addObserver((LifecycleEventObserver) (source, event) -> {
            switch (event) {
                case ON_START: {
                    startBlur();
                }
                break;
                case ON_STOP: {
                    pauseBlur();
                }
                break;
            }
        });
    }

    public BlurFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
