package kexie.android.common.util;

import android.os.Build;
import android.view.View;
import android.view.Window;

public final class SystemUI
{
    private SystemUI()
    {
        throw new AssertionError();
    }

    public static void hide(Window window)
    {
        window.getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        window.getDecorView().setOnSystemUiVisibilityChangeListener(
                visibility -> {
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            //布局位于状态栏下方
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            //全屏
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            //隐藏导航栏
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    if (Build.VERSION.SDK_INT >= 19)
                    {
                        uiOptions |= 0x00001000;
                    } else
                    {
                        uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                    }
                    window.getDecorView().setSystemUiVisibility(uiOptions);
                });
    }
}
