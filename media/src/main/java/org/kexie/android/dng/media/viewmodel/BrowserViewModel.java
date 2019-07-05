package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.kexie.android.dng.common.BR;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.model.MediaBeanStore;
import org.kexie.android.dng.media.model.beans.Graph;
import org.kexie.android.dng.media.viewmodel.beans.AlbumDetail;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class BrowserViewModel extends AndroidViewModel {

    private final HandlerThread workerThread;

    private final Handler worker;

    private final Handler main = new Handler(Looper.getMainLooper());

    public final MutableLiveData<AlbumDetail> current = new MutableLiveData<>();

    public final GenericQuickAdapter<AlbumDetail> albums
            = new GenericQuickAdapter<>(R.layout.item_album, BR.album);

    public final GenericQuickAdapter<Graph> graphs
            = new GenericQuickAdapter<>(R.layout.item_graph, BR.res);

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public BrowserViewModel(@NonNull Application application) {
        super(application);
        workerThread = new HandlerThread("gallery");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        graphs.openLoadAnimation();
        current.observeForever(album -> graphs.setNewData(album == null
                ? Collections.emptyList()
                : album.resources));
        load();
    }

    private void load() {
        isLoading.setValue(true);
        worker.post(() -> {
            List<Graph.Album> albums = MediaBeanStore.getInstance(getApplication()).loadGraph();
            List<AlbumDetail> albumDetails = new LinkedList<>();
            for (Graph.Album album : albums) {
                albumDetails.add(new AlbumDetail(album.name, album.resources));
            }
            main.post(() -> {
                if (!albumDetails.isEmpty()) {
                    AlbumDetail albumDetail = albumDetails.get(0);
                    albumDetail.isChecked = true;
                    current.setValue(albumDetail);
                }
                this.albums.setNewData(albumDetails);

            });
            isLoading.postValue(false);
        });

    }

    @Override
    protected void onCleared() {
        workerThread.quit();
        worker.removeCallbacksAndMessages(null);
        main.removeCallbacksAndMessages(null);
    }
}
