package org.kexie.android.dng.media.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.common.databinding.RxEvent;
import org.kexie.android.common.widget.ProgressFragment;
import org.kexie.android.dng.media.BR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMediaBrowseBinding;
import org.kexie.android.dng.media.model.entity.MediaType;
import org.kexie.android.dng.media.viewmodel.MediaBrowseViewModel;
import org.kexie.android.dng.media.viewmodel.entity.Media;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

@Mapping("dng/media/browse")
public class MediaBrowseFragment
        extends Fragment
{
    private MediaBrowseViewModel viewModel;

    private FragmentMediaBrowseBinding binding;

    private GenericQuickAdapter<Media> mediasAdapter;

    private static final int REQUEST_TO_PHOTO = 1000;
    private static final int REQUEST_TO_VIDEO = 1001;

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

        mediasAdapter = new GenericQuickAdapter<>(R.layout.item_media_info, BR.mediaInfo);

        mediasAdapter.setOnItemClickListener((adapter, view1, position) -> {
            Media info = mediasAdapter.getData().get(position);
            switch (info.type)
            {
                case MediaType.TYPE_PHOTO:
                {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("media", info);
                    Request request = new Request.Builder()
                            .uri("dng/media/photo")
                            .code(REQUEST_TO_PHOTO)
                            .bundle(bundle)
                            .build();
                    jumpTo(request);
                }
                break;
                case MediaType.TYPE_VIDEO:
                {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("media", info);
                    Request request = new Request.Builder()
                            .uri("dng/media/video")
                            .code(REQUEST_TO_VIDEO)
                            .bundle(bundle)
                            .build();
                    jumpTo(request);
                }
                break;
            }
        });

        mediasAdapter.openLoadAnimation(GenericQuickAdapter.ALPHAIN);

        mediasAdapter.setEmptyView(R.layout.view_empty, (ViewGroup) view);

        viewModel.medias.observe(this,mediasAdapter::setNewData);

        binding.setMedias(mediasAdapter);

        //liveData
        viewModel.title.observe(this, binding::setTitle);
        //rx
        viewModel.onLoading.as(RxEvent.bind(this))
                .subscribe(ProgressFragment.makeObserver(this));

        viewModel.loadPhoto();
    }

    private void jumpTo(Request request)
    {
        requireFragmentManager()
                .beginTransaction()
                .add(getId(), Mapper.getOn(this, request))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data)
    {
        if (requestCode == REQUEST_TO_PHOTO
                && Activity.RESULT_FIRST_USER == resultCode)
        {
            Media media = Objects.requireNonNull(data)
                    .getParcelableExtra("media");
            int index = mediasAdapter.getData().indexOf(media);
            if (index != -1)
            {
                mediasAdapter.remove(index);
            }
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