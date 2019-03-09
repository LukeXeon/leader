package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.content.Context;

import com.amap.api.navi.AMapNavi;

import java.lang.reflect.Constructor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public final class NaviViewModelFactory
        extends ViewModelProvider.AndroidViewModelFactory
{
    private final AMapNavi navi;
    private final Application application;

    public NaviViewModelFactory(@NonNull Context context, AMapNavi navi)
    {
        super((Application) context.getApplicationContext());
        this.application = (Application) context.getApplicationContext();
        this.navi = navi;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
    {
        try
        {
            Constructor<T> constructor = modelClass
                    .getConstructor(Application.class, AMapNavi.class);
            return constructor.newInstance(application, navi);
        } catch (Exception e)
        {
            return super.create(modelClass);
        }
    }
}
