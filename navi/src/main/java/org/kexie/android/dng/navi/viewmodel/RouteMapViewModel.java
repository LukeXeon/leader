package org.kexie.android.dng.navi.viewmodel;

import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.route.DrivePath;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.model.BoxRoute;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.model.Route;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.Collectors;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import mapper.Request;

public class RouteMapViewModel extends ViewModel
{
    private AMap mapController;

    private Route route;

    private DrivePath path;

    private final PublishSubject<Request> onJump = PublishSubject.create();

    public void init(AMap aMap, Bundle bundle)
    {
        this.mapController = aMap;
        Point from = bundle.getParcelable("from");
        Point to = bundle.getParcelable("to");
        path = bundle.getParcelable("path");
        route = new BoxRoute(from, to, path);
    }

    public void setBounds()
    {
        List<Point> points = Point.getBounds(Route.getAllPoint(route));
        LatLngBounds bounds = new LatLngBounds(
                points.get(0).unBox(LatLng.class),
                points.get(1).unBox(LatLng.class)
        );
        mapController.setMapStatusLimits(bounds);
    }

    //绘制一条纹理线
    public void drawLine()
    {
        //点
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

    public void jumpToDetails()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("path", path);
        Request request = new Request.Builder()
                .bundle(bundle)
                .uri("dng/navi/details")
                .build();
        onJump.onNext(request);
    }

    public void jumpToNavi()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("route", route);
        Request request = new Request.Builder()
                .bundle(bundle)
                .uri("dng/navi/navi")
                .build();
        onJump.onNext(request);
    }

    public Observable<Request> getOnJump()
    {
        return onJump;
    }

}
