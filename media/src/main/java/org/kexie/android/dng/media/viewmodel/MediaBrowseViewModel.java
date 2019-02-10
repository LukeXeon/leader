package org.kexie.android.dng.media.viewmodel;

import android.app.Application;

import org.kexie.android.dng.media.viewmodel.entity.MediaInfo;
import org.kexie.android.dng.media.model.MediaInfoProvider;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MediaBrowseViewModel extends AndroidViewModel
{

    private Executor singleTask = Executors.newSingleThreadExecutor();

    private MutableLiveData<String> title = new MutableLiveData<>();

    private MutableLiveData<List<MediaInfo>> mediaInfo = new MutableLiveData<>();

    private MutableLiveData<String> loading = new MutableLiveData<>();

    public MediaBrowseViewModel(@NonNull Application application)
    {
        super(application);
    }

    public LiveData<List<MediaInfo>> getMediaInfo()
    {
        return mediaInfo;
    }

    public LiveData<String> getTitle()
    {
        return title;
    }

    public LiveData<String> getLoading()
    {
        return loading;
    }

    public void loadVideo()
    {
        loading.setValue("");
        singleTask.execute(() -> {
            mediaInfo.postValue(MediaInfoProvider.getVideoModels(getApplication()));
            loading.postValue(null);
            title.postValue("视频");
        });
    }

    public void loadPhoto()
    {
        loading.setValue("");
        singleTask.execute(() -> {

            mediaInfo.postValue(MediaInfoProvider.getPhotoModels(getApplication()));
            loading.postValue(null);
            title.postValue("相册");
        });
    }

}
