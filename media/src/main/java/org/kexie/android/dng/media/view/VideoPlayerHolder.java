package org.kexie.android.dng.media.view;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yhao.floatwindow.FloatWindow;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.util.Utils;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.widget.WindowPlayer;
import org.kexie.android.dng.player.media.vedio.IjkPlayerView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

//需要实现浮窗
//多进程,单进程是在是顶不住了
@Route(path = Module.Media.videoPlayer)
public class VideoPlayerHolder
        extends AppCompatActivity {

    private WindowPlayer windowPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doTransform();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        doTransform();
    }

    private void doTransform() {
        Utils.hideSystemUi(getWindow());
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        Fragment fragment = null;
        boolean isFormWindow = bundle.getBoolean(getString(R.string.is_form_window), false);
        if (FloatWindow.get(getString(R.string.window_key)) != null) {
            if (isFormWindow && windowPlayer != null) {
                fragment = windowPlayer.transformToFragment();
            }
            windowPlayer = null;
            FloatWindow.destroy(getString(R.string.window_key));
        }
        if (fragment == null) {
            fragment = new VideoPlayerFragment();
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(android.R.id.content, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    public void holdByWindow(IjkPlayerView player) {
        windowPlayer = new WindowPlayer(player);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ARouter.getInstance()
                .build(Module.Host.host)
                .navigation(this);
    }
}