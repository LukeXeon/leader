package org.kexie.android.dng.media.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.common.widget.ProgressFragment;
import org.kexie.android.dng.media.BR;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentMediaBrowseBinding;
import org.kexie.android.dng.media.model.entity.MediaInfo;
import org.kexie.android.dng.media.viewmodel.BrowserViewModel;
import org.kexie.android.dng.media.viewmodel.beans.Resource;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

@Route(path = Module.media.browse)
public class MediaBrowseFragment
        extends Fragment {
    private BrowserViewModel viewModel;

    private FragmentMediaBrowseBinding binding;

    private GenericQuickAdapter<Resource> mediasAdapter;

    private static final int REQUEST_TO_PHOTO = 1000;
    private static final int REQUEST_TO_VIDEO = 1001;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this)
                .get(BrowserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mediasAdapter = new GenericQuickAdapter<>(R.layout.item_resource, BR.mediaInfo);
        mediasAdapter.setOnItemClickListener(
                RxUtils.debounce(
                        BaseQuickAdapter.OnItemClickListener.class,
                        getLifecycle(),
                        (adapter, view1, position) -> {
                            Resource info = (Resource) adapter.getItem(position);
                            if (info == null) {
                                return;
                            }
                            switch (info.type) {
                                case MediaInfo.TYPE_PHOTO: {
                                    Postcard postcard = ARouter.getInstance()
                                            .build(Module.media.photo);
                                    Bundle bundle = postcard.getExtras();
                                    bundle.putParcelable("media", info);
                                    Fragment fragment = (Fragment) postcard.navigation();
                                    fragment.setTargetFragment(this, REQUEST_TO_PHOTO);
                                    jumpTo(fragment);
                                }
                                break;
                                case MediaInfo.TYPE_VIDEO: {
                                    Postcard postcard = ARouter.getInstance()
                                            .build(Module.media.media);
                                    Bundle bundle = postcard.getExtras();
                                    bundle.putParcelable("media", info);
                                    postcard.navigation(requireContext());
                                }
                                break;
                            }
                        }));
        mediasAdapter.openLoadAnimation(GenericQuickAdapter.ALPHAIN);
        mediasAdapter.setEmptyView(R.layout.view_empty, (ViewGroup) view);

        viewModel.medias.observe(this, mediasAdapter::setNewData);
        viewModel.title.observe(this, binding::setTitle);

        viewModel.medias.observe(this, Logger::d);

        viewModel.loadPhoto();

        Map<String, View.OnClickListener> actions
                = new ArrayMap<String, View.OnClickListener>() {
            {
                put("相册",
                        RxUtils.debounce(View.OnClickListener.class, getLifecycle(), v -> {
                            viewModel.loadPhoto();
                            binding.dataContent.stopScroll();
                            binding.dataContent.stopNestedScroll();
                        }));
                put("视频", RxUtils.debounce(View.OnClickListener.class, getLifecycle(),
                        v -> {
                            viewModel.loadVideo();
                            binding.dataContent.stopScroll();
                            binding.dataContent.stopNestedScroll();
                        }));
            }
        };
        binding.setLifecycleOwner(this);
        binding.getRoot().setOnTouchListener((v, event) -> true);
        binding.setActions(actions);
        binding.setMedias(mediasAdapter);

        ProgressFragment.observeWith(viewModel.isLoading, this);
    }

    private void jumpTo(Fragment fragment) {
        requireFragmentManager()
                .beginTransaction()
                .add(getId(), fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {
        if (requestCode == REQUEST_TO_PHOTO
                && Activity.RESULT_FIRST_USER == resultCode) {
            Resource media = Objects.requireNonNull(data)
                    .getParcelableExtra("media");
            int index = mediasAdapter.getData().indexOf(media);
            if (index != -1) {
                mediasAdapter.remove(index);
            }
        }
    }
}