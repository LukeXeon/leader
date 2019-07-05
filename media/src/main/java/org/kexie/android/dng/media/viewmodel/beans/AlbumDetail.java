package org.kexie.android.dng.media.viewmodel.beans;

import org.kexie.android.dng.media.model.beans.Graph;

import java.util.List;

public class AlbumDetail {
    public boolean isChecked;

    public final String title;
    public final List<Graph> resources;

    public AlbumDetail(String title, List<Graph> resources) {
        this.title = title;
        this.resources = resources;
    }

    public String getCover() {
        return resources.get(0).data;
    }

    public int getCount() {
        return resources.size();
    }
}
