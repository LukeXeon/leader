package org.kexie.android.dng.ux.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.util.ArrayMap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.ItemDesktopGroupBinding;
import org.kexie.android.dng.ux.databinding.ItemDsektopNormalBinding;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;

public final class NeoDesktop {

    private NeoDesktop() {
        throw new AssertionError();
    }

    public interface Action {
        void onAction(String action);
    }

    private static final float SCALE_FORM = 0.9f;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_GROUP = 2;

    public static RecyclerView.Adapter<RecyclerView.ViewHolder> newAdapter(Action action) {
        return new NeoDesktopAnimationAdapter(action);
    }

    private static final class NeoDesktopAnimationAdapter extends AnimationAdapter {

        @SuppressWarnings("unchecked")
        private NeoDesktopAnimationAdapter(Action action) {
            super((RecyclerView.Adapter) new NeoDesktopAdapter(action));
            setFirstOnly(false);
            setInterpolator(new AnticipateOvershootInterpolator());
            setDuration(600);
        }

        @Override
        protected Animator[] getAnimators(View view) {
            return new Animator[]{
                    ObjectAnimator.ofFloat(view, "scaleY", SCALE_FORM, 1f),
                    ObjectAnimator.ofFloat(view, "scaleX", SCALE_FORM, 1f),
                    ObjectAnimator.ofFloat(view, "translationX",
                            view.getMeasuredWidth() / 4f, 0)
            };
        }
    }

    private static final class NeoDesktopItem {
        private final int image;
        private final String name;

        private NeoDesktopItem(String name, int image) {
            this.image = image;
            this.name = name;
        }
    }

    private static NeoDesktopItem item(String name, int imageId) {
        return new NeoDesktopItem(name, imageId);
    }

    private static final class NeoDesktopViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;
        private final int type;

        private NeoDesktopViewHolder(@NonNull ViewDataBinding viewDataBinding, int type) {
            super(viewDataBinding.getRoot());
            binding = viewDataBinding;
            this.type = type;
        }
    }

    private static final class NeoDesktopAdapter
            extends RecyclerView.Adapter<NeoDesktopViewHolder>
            implements View.OnClickListener {
        private final Action action;
        private final Map<View, String> mapping = new ArrayMap<>();
        private final Object[] items = {
                item("导航", R.drawable.icon_navi),
                item("时间", R.drawable.icon_time),
                item("收音机", R.drawable.icon_fm),
                item("视频", R.drawable.icon_video),
                item("照片", R.drawable.icon_photo),
                new NeoDesktopItem[]{
                        item("APPS", R.drawable.icon_apps),
                        item("音乐", R.drawable.icon_music),
                        item("个人中心", R.drawable.icon_info),
                        item("天气", R.drawable.icon_weather)
                },
                item("应用商店", R.drawable.icon_store),
                item("设置", R.drawable.icon_setting)
        };

        private NeoDesktopAdapter(Action action) {
            this.action = action;
        }

        @Override
        public int getItemViewType(int position) {
            return (items[position] instanceof NeoDesktopItem[]) ? VIEW_TYPE_GROUP : VIEW_TYPE_NORMAL;
        }

        @NonNull
        @Override
        public NeoDesktopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NeoDesktopViewHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    (viewType == VIEW_TYPE_GROUP) ? R.layout.item_desktop_group : R.layout.item_dsektop_normal,
                    parent, false
            ), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull NeoDesktopViewHolder holder, int position) {
            if (holder.type == VIEW_TYPE_GROUP) {
                ItemDesktopGroupBinding binding = (ItemDesktopGroupBinding) holder.binding;
                List<Pair<ImageView, TextView>> views = Arrays.asList(
                        Pair.create(binding.image1, binding.text1),
                        Pair.create(binding.image2, binding.text2),
                        Pair.create(binding.image3, binding.text3),
                        Pair.create(binding.image4, binding.text4));
                NeoDesktopItem[] subItems = (NeoDesktopItem[]) items[position];
                for (int i = 0; i < 4; i++) {
                    Pair<ImageView, TextView> view = views.get(i);
                    NeoDesktopItem item = subItems[i];
                    Glide.with(view.first).load(item.image).into(view.first);
                    view.second.setText(item.name);
                    mapping.put(view.first, "");
                }
            } else {
                NeoDesktopItem item = (NeoDesktopItem) items[position];
                ItemDsektopNormalBinding binding = (ItemDsektopNormalBinding) holder.binding;
                Glide.with(binding.image).load(item.image).into(binding.image);
                binding.text.setText(item.name);
                mapping.put(binding.image, "");
            }
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        @Override
        public void onClick(View v) {
            action.onAction(mapping.get(v));
        }
    }
}
