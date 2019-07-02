package org.kexie.android.dng.navi.widget;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.ItemPathBinding;
import org.kexie.android.dng.navi.viewmodel.beans.PathDescription;

import androidx.lifecycle.Observer;

public class PathAdapter extends GenericQuickAdapter<PathDescription> {

    private final Observer<Integer> observer;

    public PathAdapter(Observer<Integer> observer) {
        super(R.layout.item_path, BR.path);
        this.observer = observer;
    }

    @Override
    protected void convert(GenericViewHolder helper, PathDescription item) {
        super.convert(helper, item);
        ItemPathBinding binding = helper.getBinding();
        binding.setOnJumpToNavi(v -> observer.onChanged(item.id));
    }
}
