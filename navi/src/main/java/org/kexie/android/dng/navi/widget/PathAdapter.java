package org.kexie.android.dng.navi.widget;

import android.view.View;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.ItemPathBinding;
import org.kexie.android.dng.navi.viewmodel.beans.PathDescription;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class PathAdapter
        extends PileLayout.Adapter
        implements View.OnClickListener {

    private final Runnable submit;

    private final Observer<Integer> onSelect;

    private final MutableLiveData<List<PathDescription>> data = new MutableLiveData<>();

    public PathAdapter(Runnable submit, Observer<Integer> onSelect) {
        this.submit = submit;
        this.onSelect = onSelect;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_path;
    }

    @Override
    public int getItemCount() {
        return getValue().size();
    }

    private List<PathDescription> getValue() {
        List<PathDescription> descriptions = data.getValue();
        return descriptions == null ? Collections.emptyList() : descriptions;
    }

    @Override
    public void bindView(View view, int index) {
        ItemPathBinding binding = DataBindingUtil.bind(view);
        if (binding != null) {
            binding.setPath(getValue().get(index));
            binding.setOnJump(this);
        }
    }

    public void setValue(List<PathDescription> descriptions) {
        data.setValue(descriptions);
    }

    public void observe(LifecycleOwner owner, Observer<Collection<PathDescription>> observer) {
        data.observe(owner, observer);
    }

    @Override
    public void displaying(int position) {
        onSelect.onChanged(position);
    }

    @Override
    public void onClick(View v) {
        submit.run();
    }
}
