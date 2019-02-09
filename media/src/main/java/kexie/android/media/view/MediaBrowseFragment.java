package kexie.android.media.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import kexie.android.common.widget.ProgressHelper;
import kexie.android.media.R;
import kexie.android.media.databinding.FragmentMediaBrowseBinding;
import kexie.android.media.entity.MediaInfo;
import kexie.android.media.viewmodel.MediaViewModel;

public class MediaBrowseFragment
        extends Fragment
{
    private MediaViewModel viewModel;

    private FragmentMediaBrowseBinding binding;

    @SuppressLint("ClickableViewAccessibility")
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
        binding.getRoot().setOnTouchListener((v, event) -> true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        binding.dataContent.setLayoutManager(
                new StaggeredGridLayoutManager(4,
                        StaggeredGridLayoutManager.VERTICAL));
        binding.setOnCreateAdapter((adapter) -> {
            TextView textView = new TextView(getContext());
            textView.setTextSize(20);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getResources()
                    .getColor(R.color.colorBlackAlpha54));
            textView.setText("空空如也");
            adapter.setEmptyView(textView);
            adapter.setOnItemClickListener((adapter1, view1, position) -> {
                MediaInfo info = (MediaInfo) adapter.getData().get(position);
                switch (info.getType())
                {
                    case MediaInfo.TYPE_PHOTO:
                    {
                        getFragmentManager()
                                .beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .add(R.id.fragment_root,PhotoViewFragment.newInstance(info))
                                .addToBackStack(null)
                                .commit();
                    }
                    break;
                    case MediaInfo.TYPE_VIDEO:
                    {

                    }
                    break;
                }
            });
        });
        viewModel = ViewModelProviders.of(this)
                .get(MediaViewModel.class);
        viewModel.getTitle().observe(this,
                (value) -> binding.setTitle(value));
        ProgressHelper.observe(viewModel.getLoading(), getFragmentManager()
                , R.id.fragment_root);
        viewModel.getMediaInfo().observe(this,
                (value) -> binding.setMediaInfos(value));
        binding.setActions(getActions());
        viewModel.loadPhoto();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        System.gc();
    }

    private Map<String, View.OnClickListener> getActions()
    {
        return new ArrayMap<String, View.OnClickListener>()
        {
            {
                put("相册", v -> viewModel.loadPhoto());
                put("视频", v -> viewModel.loadVideo());
            }
        };
    }
}
