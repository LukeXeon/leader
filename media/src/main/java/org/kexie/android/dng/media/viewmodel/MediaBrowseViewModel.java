package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.os.Bundle;

import org.kexie.android.common.util.Collectors;
import org.kexie.android.dng.media.model.MediaInfoProvider;
import org.kexie.android.dng.media.model.entity.MediaInfo;
import org.kexie.android.dng.media.viewmodel.entity.LiteMediaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.StreamSupport;
import mapper.Request;

public class MediaBrowseViewModel extends AndroidViewModel
{

    public static final int REQUEST_TO_PHOTO = 1000;
    public static final int REQUEST_TO_VIDEO = 1001;

    private static final String TYPE_PHOTO = "相册";
    private static final String TYPE_VIDEO = "视频";

    private Executor singleTask = Executors.newSingleThreadExecutor();

    private MutableLiveData<String> title = new MutableLiveData<>();

    private MutableLiveData<Map<LiteMediaInfo, MediaInfo>> mediaInfo = new MutableLiveData<>();

    private PublishSubject<String> loading = PublishSubject.create();

    private PublishSubject<Request> onJump = PublishSubject.create();

    public MediaBrowseViewModel(@NonNull Application application)
    {
        super(application);
    }

    public LiveData<List<LiteMediaInfo>> getMediaInfo()
    {
        return Transformations.map(mediaInfo, x -> new ArrayList<>(x.keySet()));
    }

    public LiveData<String> getTitle()
    {
        return title;
    }

    public Observable<String> getLoading()
    {
        return loading;
    }

    public void requestJump(LiteMediaInfo mediaInfo)
    {
        Map<LiteMediaInfo, MediaInfo> map = this.mediaInfo.getValue();
        if (map != null)
        {
            MediaInfo info = map.get(mediaInfo);
            if (info != null)
            {
                Bundle bundle = new Bundle();
                bundle.putParcelable("info", info);
                Request request = new Request.Builder()
                        .uri(info.type == MediaInfo.TYPE_PHOTO
                                ? "dng/media/photo"
                                : "dng/media/video")
                        .bundle(bundle)
                        .code(info.type == MediaInfo.TYPE_PHOTO
                                ? REQUEST_TO_PHOTO
                                : REQUEST_TO_VIDEO)
                        .build();
                onJump.onNext(request);
            }
        }
    }

    public Observable<Request> getOnJump()
    {
        return onJump;
    }

    public void loadVideo()
    {
        internalLoad(TYPE_VIDEO);
    }

    private void internalLoad(String type)
    {
        loading.onNext("加载中...");
        singleTask.execute(() -> {
            Map<LiteMediaInfo, MediaInfo> map
                    = StreamSupport.stream(TYPE_VIDEO.equals(type)
                    ? MediaInfoProvider.getVideoModels(getApplication())
                    : MediaInfoProvider.getPhotoModels(getApplication()))
                    .collect(Collectors.toLinkedHashMap(i -> new LiteMediaInfo(i.title, i.uri), i -> i));
            mediaInfo.postValue(map);
            loading.onNext("");
            title.postValue(type);
        });
    }

    public void loadPhoto()
    {
        internalLoad(TYPE_PHOTO);
    }
}
