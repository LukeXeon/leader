package org.kexie.android.dng.ux.widget;

import org.kexie.android.dng.common.BR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.viewmodel.beans.DesktopItem;

public class DesktopItemAdapter extends GenericQuickAdapter<DesktopItem> {
    public DesktopItemAdapter() {
        super(R.layout.item_dsektop_normal, BR.function);
    }
}
