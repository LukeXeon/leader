package kexie.android.media.viewmodel;

import android.app.Application;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import kexie.android.media.entity.MediaInfo;

public class MediaManagedViewModel extends AndroidViewModel
{
    public MediaManagedViewModel(@NonNull Application application)
    {
        super(application);
    }

    public boolean delete(MediaInfo info)
    {
        if (info.getType() == MediaInfo.TYPE_PHOTO)
        {
            return getApplication().getContentResolver()
                    .delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "=?",
                            new String[]{info.getPath()}) > 0;
        } else
        {
            return getApplication().getContentResolver()
                    .delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.DATA + "=?",
                            new String[]{info.getPath()}) > 0;
        }
    }
}