package org.kexie.android.dng.media.viewmodel;

import android.app.Application;
import android.provider.MediaStore;

import org.kexie.android.dng.media.model.entity.MediaInfo;
import org.kexie.android.dng.media.viewmodel.entity.LiteMediaInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class MediaManagedViewModel extends AndroidViewModel
{

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();
    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    public MediaManagedViewModel(@NonNull Application application)
    {
        super(application);
    }

    public boolean delete(LiteMediaInfo info)
    {
        boolean success;
        if (info.type == MediaInfo.TYPE_PHOTO)
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
        if (success)
        {
            onErrorMessage.onNext("删除成功");
        } else
        {
            onErrorMessage.onNext("删除失败");
        }
        return success;
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage;
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage;
    }
}
