package org.kexie.android.dng.media.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.media.R;
import org.kexie.android.dng.media.databinding.FragmentGalleryBinding;
import org.kexie.android.dng.media.model.beans.Graph;
import org.kexie.android.dng.media.viewmodel.BrowserViewModel;
import org.kexie.android.dng.media.viewmodel.beans.AlbumDetail;
import org.kexie.android.dng.media.widget.VideoPlayerActivityHolder;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

@Route(path = Module.Media.gallery)
public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    private BrowserViewModel viewModel;

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
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_gallery,
                container,
                false);
        return binding.getRoot();
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.setAlbums(viewModel.albums);
        binding.setGraphs(viewModel.graphs);
        viewModel.albums.setOnItemClickListener((adapter, view1, position) -> {
            AlbumDetail album = (AlbumDetail) adapter.getItem(position);
            if (album != null) {
                List<AlbumDetail> details = adapter.getData();
                List<Integer> change = new LinkedList<>();
                change.add(position);
                for (int i = 0; i < details.size(); i++) {
                    AlbumDetail detail = details.get(i);
                    if (detail.isChecked) {
                        detail.isChecked = false;
                        change.add(i);
                    }
                }
                album.isChecked = true;
                for (int pos : change) {
                    adapter.notifyItemChanged(adapter.getHeaderLayoutCount() + pos);
                }
                viewModel.current.setValue(album);
            }
        });
        viewModel.current.observe(this, albumDetail -> binding.setTitle(albumDetail.title));
        binding.setOnOpen(v -> binding.drawer.openDrawer(GravityCompat.START));
        viewModel.graphs.setOnItemClickListener((adapter, view12, position) -> {
            Graph graph = (Graph) adapter.getItem(position);
            if (graph != null) {
                if (graph.isVideo()) {
                    Intent intent = new Intent(requireActivity(),
                            VideoPlayerActivityHolder.class);
                    intent.putExtra("media", graph);
                    requireActivity().startActivity(intent);
                } else {
                    Fragment fragment = new PhotoViewerFragment();
                    Bundle bundle = new Bundle();
                    fragment.setTargetFragment(this, R.id.photo_request_code);
                    bundle.putParcelable("media", graph);
                    fragment.setArguments(bundle);
                    requireFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .add(getId(), fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commitAllowingStateLoss();
                }
            }
        });
        viewModel.isLoading.observe(this,
                isLoading -> binding.progressBar.enableIndeterminateMode(isLoading));
    }
}
