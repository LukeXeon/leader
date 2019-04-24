package org.kexie.android.dng.media.view;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;
import com.yhao.floatwindow.FloatWindow;

import org.kexie.android.dng.common.app.PR;
import org.kexie.android.dng.media.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

//需要实现浮窗
//多进程,单进程是在是顶不住了
@Route(path = PR.media.video_proxy)
public class VideoPlayerProxyActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("video proxy start");
        setContentView(R.layout.fragment_player_container);
        startNewPlayer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        FloatWindow.destroy(getString(R.string.window_key));
        startNewPlayer();
    }

    private void startNewPlayer() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        Postcard postcard = ARouter
                .getInstance()
                .build(PR.media.video);
        Bundle extra = postcard.getExtras();
        extra.putAll(bundle);
        Fragment fragment = (Fragment) postcard.navigation();
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void onBackPressed() {
        getOnBackPressedDispatcher().onBackPressed();
        ARouter.getInstance()
                .build(PR.host.host)
                .navigation(this);
    }
}
