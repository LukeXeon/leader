package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.media.model.MediaInfoProvider;
import org.kexie.android.dng.media.model.entity.MediaInfo;
import org.kexie.android.dng.media.viewmodel.entity.LiteMediaInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    private Map<LiteMediaInfo, MediaInfo> mediaInfos = new HashMap<>();

    private GenericQuickAdapter<LiteMediaInfo> adapter;

    private PublishSubject<String> loading = PublishSubject.create();

    private PublishSubject<Request> onJump = PublishSubject.create();

    public MediaBrowseViewModel(@NonNull Application application)
    {
        super(application);
    }

    public LiveData<String> getTitle()
    {
        return title;
    }

    public Observable<String> getLoading()
    {
        return loading;
    }

    public void setAdapter(GenericQuickAdapter<LiteMediaInfo> adapter)
    {
        this.adapter = adapter;
    }

    public void requestJump(LiteMediaInfo mediaInfo)
    {
        MediaInfo info = mediaInfos.get(mediaInfo);
        if (info != null)
        {
            int index = adapter.getData().indexOf(mediaInfo);
            Bundle bundle = new Bundle();
            bundle.putParcelable("info", info);
            bundle.putInt("index", index);
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

    public void remove(int index)
    {
        if (index != -1)
        {
            LiteMediaInfo data = adapter.getItem(index);
            adapter.remove(index);
            mediaInfos.remove(data);
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
            Handler handler = new Handler(Looper.getMainLooper());
            mediaInfos.clear();
            handler.post(() -> {
                adapter.getData().clear();
                adapter.notifyDataSetChanged();
            });
            StreamSupport.stream(TYPE_VIDEO.equals(type)
                    ? MediaInfoProvider.getVideoModels(getApplication())
                    : MediaInfoProvider.getPhotoModels(getApplication()))
                    .forEach(x -> {
                        LiteMediaInfo liteMediaInfo
                                = new LiteMediaInfo(x.title, x.uri);
                        mediaInfos.put(liteMediaInfo, x);
                        handler.post(() -> adapter.addData(liteMediaInfo));
                    });
            loading.onNext("");
            title.postValue(type);
        });
    }

    public void loadPhoto()
    {
        internalLoad(TYPE_PHOTO);
    }
}
