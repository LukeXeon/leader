package org.kexie.android.dng.media.viewmodel;

import android.app.Application;

import org.kexie.android.dng.media.model.MediaInfoProvider;
import org.kexie.android.dng.media.viewmodel.entity.LiteMediaInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class MediaBrowseViewModel extends AndroidViewModel
{

    private Executor singleTask = Executors.newSingleThreadExecutor();

    private MutableLiveData<String> title = new MutableLiveData<>();

    private MutableLiveData<List<LiteMediaInfo>> mediaInfo = new MutableLiveData<>();

    private MutableLiveData<String> loading = new MutableLiveData<>();

    public MediaBrowseViewModel(@NonNull Application application)
    {
        super(application);
    }

    public LiveData<List<LiteMediaInfo>> getMediaInfo()
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
            mediaInfo.postValue(
                    StreamSupport.stream(MediaInfoProvider.getVideoModels(getApplication()))
                            .map(mediaInfo1
                                    -> new LiteMediaInfo(mediaInfo1.title,
                                    mediaInfo1.path,
                                    mediaInfo1.type))
                            .collect(Collectors.toList()));
            loading.postValue(null);
            title.postValue("视频");
        });
    }

    public void loadPhoto()
    {
        loading.setValue("");
        singleTask.execute(() -> {
            mediaInfo.postValue(
                    StreamSupport.stream(MediaInfoProvider.getPhotoModels(getApplication()))
                            .map(mediaInfo1
                                    -> new LiteMediaInfo(mediaInfo1.title,
                                    mediaInfo1.path,
                                    mediaInfo1.type))
                            .collect(Collectors.toList()));
            loading.postValue(null);
            title.postValue("相册");
        });
    }
}
