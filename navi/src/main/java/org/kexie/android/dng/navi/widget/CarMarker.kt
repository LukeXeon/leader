package org.kexie.android.dng.navi.widget

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
    private var time = FIRST_TIME
    var isVisible: Boolean
        get() = carMarker.isVisible && directionMarker.isVisible
        set(value) {
            carMarker.isVisible = value;
            directionMarker.isVisible = value
        }
    var zoom: Float = 20f
    var isLock: Boolean = false
    var offsetX: Float = 0f
    var offsetY: Float = 0f

    private data class Location(val point: Point, val bearing: Float)

    init {
        fun getMarker(res: Int): Marker {
            val carBitmapDescriptor = BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(this.context
                            .resources, res))
            return map.addMarker(MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .setFlat(true)
                    .icon(carBitmapDescriptor))
        }
        carMarker = getMarker(R.drawable.caricon)
        directionMarker = getMarker(R.drawable.navi_direction)
        isVisible = false
    }

    fun draw(location: Point,bearing:Float) {
        fun apply(it: Location) {
            val point = it.point.unBox(LatLng::class.java)
            directionMarker.position = point
            carMarker.position = point
            carMarker.rotateAngle = 360 - it.bearing
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
        isVisible = true
        var animator = valueAnimator;
        if (animator == null) {
            last = Location(location, bearing)
            valueAnimator = ValueAnimator()
        } else {
            if (animator.isRunning && time > FIRST_TIME) {
                time /= 2
            } else {
                time *= 2
            }
            animator.pause()
            animator.removeAllUpdateListeners()
            animator = ValueAnimator.ofObject(evaluator,
                    last, Location(location, bearing))
            animator.addUpdateListener { animation ->
                last = animation.animatedValue as Location
                apply(last)
            }
            animator.interpolator = interpolator
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
        private const val FIRST_TIME: Long = 50
        private const val TILT = 80f
    }
}