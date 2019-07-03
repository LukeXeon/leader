package org.kexie.android.dng.media.widget;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.BR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.viewmodel.beans.MusicDetail;

public class MusicQuickAdapter extends GenericQuickAdapter<MusicDetail> {
    public MusicQuickAdapter() {
        super(R.layout.item_music, BR.music);
    }

    @Override
    protected void convert(GenericViewHolder helper, MusicDetail item) {
        super.convert(helper, item);
        helper.addOnClickListener(R.id.btn_play);
    }
}
