package org.kexie.android.dng.navi.widget

import android.animation.PropertyValuesHolder
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.view.animation.LinearInterpolator
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.model.Point

class CarMarker(context: Context, private val map: AMap) {

    private val context = context.applicationContext;
    private val carMarker: Marker
    private var directionMarker: Marker
    private var valueAnimator: ValueAnimator? = null
    private lateinit var last: Location
    private var time = BASE_TIME
    var isVisible: Boolean
        get() = carMarker.isVisible && directionMarker.isVisible
        set(value) {
            carMarker.isVisible = value;
            directionMarker.isVisible = value
            directionMarker.setToTop()
            carMarker.setToTop()
        }
    var zoom: Float = 20f
    var isLock: Boolean = false

    private data class Location(var point: Point, var bearing: Float)

    init {
        fun getMarker(res: Int): Marker {
            val descriptor = BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(this.context
                            .resources, res))
            return map.addMarker(MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .position(LatLng(0.0, 0.0))
                    .setFlat(true)
                    .icon(descriptor))
        }
        carMarker = getMarker(R.drawable.caricon)
        directionMarker = getMarker(R.drawable.navi_direction)
        isVisible = false
    }

    fun draw(location: Point, bearing: Float) {
        fun apply(it: Location) {
            val point = it.point.unBox(LatLng::class.java)
            directionMarker.position = point
            carMarker.position = point
            carMarker.rotateAngle = 360 - it.bearing
            isVisible = true
            if (!isLock) {
                return
            }
            map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                    .tilt(TILT)
                    .zoom(zoom)
                    .bearing(it.bearing)
                    .target(point)
                    .build()))
        }
        val animator = valueAnimator;
        if (animator == null) {
            last = Location(location, bearing)
            apply(last)
            val newAnimator = ValueAnimator()
            newAnimator.interpolator = interpolator
            newAnimator.duration = time
            newAnimator.addUpdateListener { animation ->
                last = animation.animatedValue as Location
                apply(last)
            }
            valueAnimator = newAnimator
        } else {
            if (animator.isRunning) {
                animator.cancel()
                if (time > BASE_TIME) {
                    time /= 2
                } else {
                    time *= 2
                }
            }
            animator.setValues(PropertyValuesHolder.ofObject("",
                    evaluator,
                    last,
                    Location(location, bearing)))
            animator.duration = time
            animator.start()
            valueAnimator = animator
        }
    }

    companion object {
        private val interpolator = LinearInterpolator()
        private val evaluator = object : TypeEvaluator<Location> {
            override fun evaluate(fraction: Float, startValue: Location, endValue: Location): Location {
                val x = endValue.point.longitude - startValue.point.longitude
                val y = endValue.point.latitude - startValue.point.latitude
                val point = Point.form(startValue.point.longitude + x * fraction,
                        startValue.point.latitude + y * fraction);
                var r = endValue.bearing - startValue.bearing
                if (r > 180) {
                    r -= 360
                } else if (r < -180) {
                    r += 360
                }
                val bearing = (startValue.bearing + r * fraction)
                return Location(point, bearing)
            }
        }
        private const val BASE_TIME: Long = 50
        private const val TILT = 80f
    }
}