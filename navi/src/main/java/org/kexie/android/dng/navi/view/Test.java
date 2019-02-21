package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.TestBinding;
import org.kexie.android.dng.navi.widget.ScaleTransformer;
import org.kexie.android.dng.navi.widget.SimpleApplyAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import java8.util.stream.Collectors;
import java8.util.stream.IntStreams;
import mapper.Mapper;
import mapper.Mapping;
import mapper.Request;

@Mapping("dng/navi/test")
public class Test extends Fragment
{
    private TestBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.test,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Request request = new Request.Builder().uri("dng/navi/route").build();
        ViewPager viewPager = binding.test;
        viewPager.setOffscreenPageLimit(3);
        ScaleTransformer mTransformer = new ScaleTransformer();
        viewPager.setPageTransformer(false, mTransformer);
        List<Fragment> fragments = IntStreams
                .iterate(0, i -> i < 3, i -> i + 1)
                .boxed()
                .map(x -> Mapper.getOn(this, request))
                .collect(Collectors.toList());
        viewPager.setAdapter(new SimpleApplyAdapter(getChildFragmentManager(), fragments));
        viewPager.setCurrentItem(1);
    }
}
