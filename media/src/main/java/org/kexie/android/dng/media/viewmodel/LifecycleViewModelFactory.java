package org.kexie.android.dng.media.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class LifecycleViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    private final Lifecycle lifecycle;
    private final Application application;

    public LifecycleViewModelFactory(@NonNull Application application, Lifecycle lifecycle) {
        super(application);
        this.lifecycle = lifecycle;
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            return modelClass.getConstructor(Application.class, Lifecycle.class)
                    .newInstance(application, lifecycle);
        } catch (Exception e) {
            return super.create(modelClass);
        }
    }
}
