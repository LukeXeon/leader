package org.kexie.android.dng.navi.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.model.Route;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import java8.util.stream.Collectors;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import mapper.Mapping;

@Mapping("dng/navi/route")
public class RouteFragment extends Fragment
{

    private FragmentRouteBinding binding;

    private AMap mapController;

    private UiSettings uiSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_route, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //noinspection ConstantConditions
        mapController = ((SupportMapFragment) (Object)
                getChildFragmentManager().findFragmentById(R.id.map_view))
                .getMap();
        uiSettings = mapController.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        Route route = getArguments().getParcelable("route");
        if (route != null)
        {
            setBounds(route);
            setLine(route);
        }
    }

    private void setBounds(Route route)
    {
        List<Point> points = Route.getAllPoint(route);
        List<Point> boundPoints = Point.getBounds(points);
        LatLngBounds bounds = new LatLngBounds(
                boundPoints.get(0).unBox(LatLng.class),
                boundPoints.get(1).unBox(LatLng.class));
        mapController.setMapStatusLimits(bounds);
    }

    //绘制一条纹理线
    private void setLine(Route route)
    {
        //四个点
        List<Point> points = Route.getAllPoint(route);
        //用一个数组来存放纹理
        List<BitmapDescriptor> texturesList = new ArrayList<>();
        texturesList.add(BitmapDescriptorFactory.fromResource(R.mipmap.map_1));
        //指定某一段用某个纹理，对应texturesList的index即可, 四个点对应三段颜色
        List<Integer> texIndexList = IntStreams
                .iterate(0, x -> x < points.size() - 1, x -> x + 1)
                .boxed()
                .map(x -> 0)
                .collect(Collectors.toList());
        PolylineOptions options = new PolylineOptions();
        options.width(20);//设置宽度
        LatLng[] rawPoints = StreamSupport.stream(points)
                .map(p -> p.unBox(LatLng.class))
                .collect(Collectors.toList())
                .toArray(new LatLng[0]);
        //加入点
        options.add(rawPoints);
        //加入对应的颜色,使用setCustomTextureList 即表示使用多纹理；
        options.setCustomTextureList(texturesList);
        //设置纹理对应的Index
        options.setCustomTextureIndex(texIndexList);
        mapController.addPolyline(options);
    }
}
