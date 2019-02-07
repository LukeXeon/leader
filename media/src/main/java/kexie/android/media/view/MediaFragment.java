package kexie.android.media.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import kexie.android.media.R;

public class MediaFragment extends Fragment
{
    private BrowseSupportFragment browseFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(
                R.layout.fragment_media,
                container,
                false);
        Logger.d(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        browseFragment = (BrowseSupportFragment)getChildFragmentManager()
                .findFragmentById(R.id.browse);
        browseFragment.setHeadersState(BrowseSupportFragment.HEADERS_DISABLED);
        browseFragment.setTitle("相册与视频");
    }

}
