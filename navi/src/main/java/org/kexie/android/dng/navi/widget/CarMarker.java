package org.kexie.android.dng.navi.widget;

import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.model.beans.Point;



@SuppressWarnings("WeakerAccess")
public class CarMarker {

    private final AMap map;
    private final Marker car;
    private final Marker direction;

    private long time = BASE_TIME;
    private float zoom = 20f;
    private boolean isLock;
    private Pos last;
    private ValueAnimator animator;

    private static final TypeEvaluator<Pos> POS_TYPE_EVALUATOR
            = (fraction, startValue, endValue) -> {
        double x = endValue.point.getLongitude() - startValue.point.getLongitude();
        double y = endValue.point.getLatitude() - startValue.point.getLatitude();
        Point point = Point.form(startValue.point.getLongitude() + x * fraction,
                startValue.point.getLatitude() + y * fraction);
        float r = endValue.bearing - startValue.bearing;
        if (r > 180) {
            r -= 360;
        } else if (r < -180) {
            r += 360;
        }
        float bearing = (startValue.bearing + r * fraction);
        return new Pos(point, bearing);
    };
    private static final int BASE_TIME = 50;
    private static final float TILT = 80f;

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public boolean isVisible() {
        return car.isVisible() && direction.isVisible();
    }

    public void setVisible(boolean value) {
        car.setVisible(value);
        direction.setVisible(value);
        car.setToTop();
        direction.setToTop();
    }

    public CarMarker(AMap aMap, Bitmap car, Bitmap direction) {
        this.map = aMap;
        this.car = getMarker(aMap, car);
        this.direction = getMarker(aMap, direction);
        setVisible(false);
    }

    public static CarMarker getDefault(Context context, AMap aMap) {
        Resources resources = context.getResources();
        Bitmap car = BitmapFactory.decodeResource(resources, R.drawable.caricon);
        Bitmap direction = BitmapFactory.decodeResource(resources, R.drawable.navi_direction);
        return new CarMarker(aMap, car, direction);
    }

    public void draw(Point location, float bearing) {
        ValueAnimator animator = this.animator;
        if (animator == null) {
            last = new Pos(location, bearing);
            apply(last);
            ValueAnimator newAnimator = new ValueAnimator();
            newAnimator.setInterpolator(new LinearInterpolator());
            newAnimator.setDuration(time);
            newAnimator.addUpdateListener(animation -> {
                last = (Pos) animation.getAnimatedValue();
                apply(last);
            });
            this.animator = newAnimator;
        } else {
            if (animator.isRunning()) {
                animator.cancel();
                if (time > BASE_TIME) {
                    time /= 2;
                } else {
                    time *= 2;
                }
            }
            animator.setValues(PropertyValuesHolder.ofObject(
                    "",
                    POS_TYPE_EVALUATOR,
                    last,
                    new Pos(location, bearing)));
            animator.setDuration(time);
            animator.start();
            this.animator = animator;
        }
    }

    private static Marker getMarker(AMap map, Bitmap bitmap) {
        BitmapDescriptor descriptor = BitmapDescriptorFactory
                .fromBitmap(bitmap);
        return map.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(new LatLng(0.0, 0.0))
                .setFlat(true)
                .icon(descriptor));
    }

    private void apply(Pos it) {
        LatLng point = it.point.unBox(LatLng.class);
        direction.setPosition(point);
        car.setPosition(point);
        car.setRotateAngle(360 - it.bearing);
        setVisible(true);
        if (!isLock) {
            return;
        }
        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                        .tilt(TILT)
                        .zoom(zoom)
                        .bearing(it.bearing)
                        .target(point)
                        .build()));
    }

    private static final class Pos {
        final Point point;
        final float bearing;

        private Pos(Point point, float bearing) {
            this.point = point;
            this.bearing = bearing;
        }
    }
}