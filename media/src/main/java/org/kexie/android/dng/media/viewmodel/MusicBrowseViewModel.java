package org.kexie.android.dng.media.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;

public class MusicBrowseViewModel
        extends AndroidViewModel 
        implements LifecycleObserver {

    public MusicBrowseViewModel(@NonNull Application application) {
        super(application);
    }

}
