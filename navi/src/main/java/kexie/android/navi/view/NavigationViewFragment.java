package kexie.android.navi.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.navi.AMapNaviView;

public class NavigationViewFragment extends Fragment
{
    private AMapNaviView innerView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return innerView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        innerView = new AMapNaviView(getContext());
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