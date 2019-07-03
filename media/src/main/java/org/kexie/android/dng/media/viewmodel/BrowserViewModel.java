package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;

import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.media.model.MediaInfoLoader;
import org.kexie.android.dng.media.model.beans.MediaInfo;
import org.kexie.android.dng.media.viewmodel.beans.Resource;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class BrowserViewModel extends AndroidViewModel {
    private static final String TYPE_PHOTO = "相册";

    private static final String TYPE_VIDEO = "视频";

    private final HandlerThread workerThread = new HandlerThread(toString());

    private final Handler singleTask = ((Function<HandlerThread, Handler>) input -> {
        input.start();
        return new Handler(workerThread.getLooper());
    }).apply(workerThread);

    private final Handler main = new Handler(Looper.getMainLooper());

    public final MutableLiveData<String> title = new MutableLiveData<>();

    public final GenericQuickAdapter<Resource> resources = new GenericQuickAdapter<>(0,0);

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public BrowserViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadVideo() {
        internalLoad(TYPE_VIDEO);
    }

    public void loadPhoto() {
        internalLoad(TYPE_PHOTO);
    }

    private void internalLoad(String type) {
        isLoading.setValue(true);
        singleTask.post(() -> {
            List<Resource> medias = StreamSupport.stream(TYPE_VIDEO.equals(type)
                    ? MediaInfoLoader.getVideoInfos(getApplication())
                    : MediaInfoLoader.getPhotoInfos(getApplication()))
                    .map(x -> new Resource(x.title, x.uri, x.type))
                    .collect(Collectors.toList());
            main.post(()-> resources.setNewData(medias));
            isLoading.postValue(false);
            title.postValue(type);
        });
    }

    public boolean delete(Resource info) {
        boolean success;
        if (info.type == MediaInfo.TYPE_PHOTO) {
            success = getApplication().getContentResolver()
                    .delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "=?",
                            new String[]{info.uri}) > 0;
        } else {
            success = getApplication().getContentResolver()
                    .delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.DATA + "=?",
                            new String[]{info.uri}) > 0;
        }
        return success;
    }

    @Override
    protected void onCleared() {
        workerThread.quit();
    }
}
