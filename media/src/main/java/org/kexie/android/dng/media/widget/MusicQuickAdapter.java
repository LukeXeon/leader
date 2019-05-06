package org.kexie.android.dng.media.widget;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.BR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.viewmodel.entity.MusicDetails;

public class MusicQuickAdapter extends GenericQuickAdapter<MusicDetails> {
    public MusicQuickAdapter() {
        super(R.layout.item_music, BR.mediaInfo);
    }

    @Override
    protected void convert(GenericViewHolder helper, MusicDetails item) {
        super.convert(helper, item);
        helper.addOnClickListener(R.id.btn_play);
    }
}
