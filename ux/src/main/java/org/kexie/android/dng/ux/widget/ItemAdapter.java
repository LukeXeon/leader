package org.kexie.android.dng.ux.widget;

import android.view.View;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.widget.PileLayout;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.ItemDsektopNormalBinding;
import org.kexie.android.dng.ux.viewmodel.beans.DesktopItem;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

public class ItemAdapter extends PileLayout.Adapter {

    public void observe(LifecycleOwner owner, Observer<String> observer) {
        Lifecycle lifecycle = owner.getLifecycle();
        if (Lifecycle.State.DESTROYED.equals(lifecycle.getCurrentState())) {
            return;
        }
        this.observer = observer;
        lifecycle.addObserver((LifecycleEventObserver) (source, event) -> {
            if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                this.observer = null;
            }
        });
    }

    private Observer<String> observer;

    private static final DesktopItem[] refItems = {
            item("导航", R.drawable.icon_navi, Module.Navi.navigator),
            item("时间", R.drawable.icon_time, Module.Ux.time),
            item("收音机", R.drawable.icon_fm, Module.Ux.fm),
            item("视频和照片", R.drawable.icon_photo, Module.Media.gallery),
            item("APPS", R.drawable.icon_apps, Module.Ux.apps),
            item("音乐", R.drawable.icon_music, Module.Media.music),
            item("个人中心", R.drawable.icon_info, Module.Ux.userInfo),
            item("天气", R.drawable.icon_weather, Module.Ux.weather),
            item("应用商店", R.drawable.icon_store, Module.Ux.appStore),
            item("设置", R.drawable.icon_setting, Module.Ux.setting)
    };

    private static DesktopItem item(String name, int imageId, String path) {
        return new DesktopItem(name, imageId, path);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_dsektop_normal;
    }

    @Override
    public int getItemCount() {
        return refItems.length;
    }

    @Override
    public void bindView(View view, int index) {
        ItemDsektopNormalBinding binding = DataBindingUtil.bind(view);
        if (binding != null) {
            binding.setFunction(refItems[index]);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (observer != null) {
            observer.onChanged(refItems[position].path);
        }
    }
}
