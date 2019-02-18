package org.kexie.android.dng.media.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMediaBrowseBinding;
import org.kexie.android.dng.media.model.entity.MediaInfo;
import org.kexie.android.dng.media.viewmodel.MediaBrowseViewModel;
import org.kexie.android.dng.media.viewmodel.entity.LiteMediaInfo;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import mapper.Mapping;

@Mapping("dng/media/browse")
public class MediaBrowseFragment
        extends Fragment
{
    private MediaBrowseViewModel viewModel;

    private FragmentMediaBrowseBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_media_browse,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.getRoot().setOnTouchListener((v, event) -> true);
        Map<String, View.OnClickListener> actions = getActions();
        binding.setActions(actions);
        binding.dataContent.setLayoutManager(
                new StaggeredGridLayoutManager(4,
                        StaggeredGridLayoutManager.VERTICAL));
        binding.setOnItemClick((adapter, view1, position) -> {
            LiteMediaInfo info = (LiteMediaInfo) adapter.getData().get(position);
            switch (info.type)
            {
                case MediaInfo.TYPE_PHOTO:
                {
                }
                break;
                case MediaInfo.TYPE_VIDEO:
                {

                }
                break;
            }
        });
        viewModel = ViewModelProviders.of(this)
                .get(MediaBrowseViewModel.class);
        viewModel.getTitle().observe(this, binding::setTitle);
        //ProgressFragment.observe(viewModel.getLoading(), this);
        viewModel.getMediaInfo().observe(this, binding::setMediaInfo);
        viewModel.loadPhoto();
    }

    private Map<String, View.OnClickListener> getActions()
    {
        return new ArrayMap<String, View.OnClickListener>()
        {
            {
                put("相册", v ->{
                    viewModel.loadPhoto();
                    binding.dataContent.stopScroll();
                    binding.dataContent.stopNestedScroll();
                });
                put("视频",v ->{
                    viewModel.loadVideo();
                    binding.dataContent.stopScroll();
                    binding.dataContent.stopNestedScroll();
                });
            }
        };
    }
}