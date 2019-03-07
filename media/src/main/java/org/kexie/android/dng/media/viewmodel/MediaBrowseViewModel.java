package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.provider.MediaStore;

import org.kexie.android.dng.media.model.MediaInfoLoader;
import org.kexie.android.dng.media.model.entity.MediaType;
import org.kexie.android.dng.media.viewmodel.entity.Media;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class MediaBrowseViewModel extends AndroidViewModel
{
    private static final String TYPE_PHOTO = "相册";

    private static final String TYPE_VIDEO = "视频";

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    public final MutableLiveData<String> title = new MutableLiveData<>();

    public final MutableLiveData<List<Media>> medias = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();;

    public MediaBrowseViewModel(@NonNull Application application)
    {
        super(application);
    }

    public void loadVideo()
    {
        internalLoad(TYPE_VIDEO);
    }

    public void loadPhoto()
    {
        internalLoad(TYPE_PHOTO);
    }

    private void internalLoad(String type)
    {
        isLoading.setValue(true);
        singleTask.execute(() -> {
            List<Media> medias = StreamSupport.stream(TYPE_VIDEO.equals(type)
                    ? MediaInfoLoader.getVideoModels(getApplication())
                    : MediaInfoLoader.getPhotoModels(getApplication()))
                    .map(x -> new Media(x.title, x.uri, x.type))
                    .collect(Collectors.toList());
            this.medias.postValue(medias);
            isLoading.postValue(false);
            title.postValue(type);
        });
    }

    public boolean delete(Media info)
    {
        boolean success;
        if (info.type == MediaType.TYPE_PHOTO)
        {
            success = getApplication().getContentResolver()
                    .delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "=?",
                            new String[]{info.uri}) > 0;
        } else
        {
            success = getApplication().getContentResolver()
                    .delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.DATA + "=?",
                            new String[]{info.uri}) > 0;
        }
        return success;
    }
}
