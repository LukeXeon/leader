package org.kexie.android.dng.navi.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.navi.AMapNaviView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NaviViewFragment extends Fragment
{
    private AMapNaviView interView;

    public AMapNaviView getInterView()
    {
        return Objects.requireNonNull(interView);
    }

    @Override
    public void onInflate(@NonNull Context context,
                          @NonNull AttributeSet attrs,
                          @Nullable Bundle savedInstanceState)
    {
        super.onInflate(context, attrs, savedInstanceState);
        if (interView == null)
        {
            interView = new AMapNaviView(context);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        if (interView == null)
        {
            interView = new AMapNaviView(inflater.getContext());
        }
        return interView;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (interView != null)
        {
            interView.onResume();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (interView != null)
        {
            interView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (interView != null)
        {
            interView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (interView != null)
        {
            interView.onDestroy();
            interView = null;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (interView != null)
        {
            interView.onPause();
        }
    }
}
