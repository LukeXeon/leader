package kexie.android.media.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import kexie.android.media.R;
import kexie.android.media.databinding.FragmentPhotoViewBinding;
import kexie.android.media.entity.MediaInfo;

public class PhotoViewFragment extends Fragment
{

    private FragmentPhotoViewBinding binding;

    public static PhotoViewFragment newInstance(MediaInfo mediaInfo)
    {

        Bundle args = new Bundle();
        args.putParcelable("info", mediaInfo);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_photo_view,
                container,
                false);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.setInfo(getArguments().getParcelable("info"));
    }
}
