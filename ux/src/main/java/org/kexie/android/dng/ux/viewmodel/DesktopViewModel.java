package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.ux.BR;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.viewmodel.beans.DesktopItem;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class DesktopViewModel extends AndroidViewModel {

    private static final DesktopItem[] refItems = {
            item("导航", R.drawable.icon_navi, Module.Navi.navigator),
            item("时间", R.drawable.icon_time, Module.Ux.time),
            item("收音机", R.drawable.icon_fm, Module.Ux.fm),
            item("影库", R.drawable.icon_photo, Module.Media.gallery),
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

    public final GenericQuickAdapter<DesktopItem> items
            = new GenericQuickAdapter<>(R.layout.item_dsektop_normal, BR.function);

    public DesktopViewModel(@NonNull Application application) {
        super(application);
        items.setNewData(Arrays.asList(refItems));
    }

}
