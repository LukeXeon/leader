package org.kexie.android.dng.media.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.widget.ProgressFragment;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMediaBrowseBinding;
import org.kexie.android.dng.media.viewmodel.MediaBrowseViewModel;
import org.kexie.android.dng.media.viewmodel.entity.LiteMediaInfo;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/media/browse")
public class MediaBrowseFragment
        extends Fragment
{
    private MediaBrowseViewModel viewModel;

    private FragmentMediaBrowseBinding binding;

    private Runnable updateViewCallback;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this)
                .get(MediaBrowseViewModel.class);
        //dataBinding
        binding.getRoot().setOnTouchListener((v, event) -> true);
        Map<String, View.OnClickListener> actions = getActions();
        binding.setActions(actions);
        binding.dataContent.setLayoutManager(
                new StaggeredGridLayoutManager(4,
                        StaggeredGridLayoutManager.VERTICAL));
        GenericQuickAdapter<LiteMediaInfo> genericQuickAdapter
                = new GenericQuickAdapter<>(
                R.layout.item_media_info,
                "mediaInfo");
        genericQuickAdapter.setOnItemClickListener((adapter, view1, position) -> {
            LiteMediaInfo info = genericQuickAdapter.getData().get(position);
            viewModel.requestJump(info);
            updateViewCallback = () -> adapter.remove(position);
        });
        genericQuickAdapter.setEmptyView(R.layout.view_empty, (ViewGroup) view);
        binding.setMediaInfos(genericQuickAdapter);
        viewModel.setAdapter(genericQuickAdapter);
        //liveData
        viewModel.getTitle().observe(this, binding::setTitle);
        //rx
        viewModel.getOnJump()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(this::jumpTo);
        viewModel.getLoading()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(ProgressFragment.makeObserver(this));
        viewModel.loadPhoto();
    }

    private void jumpTo(Request request)
    {
        getFragmentManager()
                .beginTransaction()
                .add(getId(), Mapper.getOn(this, request))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == MediaBrowseViewModel.REQUEST_TO_PHOTO
                && Activity.RESULT_FIRST_USER == resultCode
                && updateViewCallback != null)
        {
            updateViewCallback.run();
        }
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