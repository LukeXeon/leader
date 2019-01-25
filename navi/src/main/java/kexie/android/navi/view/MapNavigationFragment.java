package kexie.android.navi.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.navi.AMapNaviView;

public class MapNavigationFragment extends Fragment
{
    private AMapNaviView innerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        innerView = new AMapNaviView(getContext());
        return innerView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        innerView.onCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        innerView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        innerView.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        innerView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        innerView.onSaveInstanceState(outState);
    }

    public AMapNaviView getInnerView()
    {
        return innerView;
    }
}
