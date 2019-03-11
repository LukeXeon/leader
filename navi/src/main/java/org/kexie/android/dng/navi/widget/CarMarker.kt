package org.kexie.android.dng.navi.widget

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.navi.model.AMapNaviLocation
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.model.Point

class CarMarker(context: Context, private val map: AMap) {
    private val context = context.applicationContext;
    private val marker: Marker
    private var valueAnimator: ValueAnimator? = null
    private lateinit var last: Location
    private var time = FIRST_TIME
    var zoom: Float = 200f
    var isLock: Boolean = false
    var offsetX: Int = 0
    var offsetY: Int = 0

    data class Location(val point: Point, val bearing: Float)

    init {
        val bitmap = BitmapFactory.decodeResource(this.context
                .resources, R.drawable.caricon)
        marker = map.addMarker(MarkerOptions()
                .anchor(0.5f, 0.5f)
                .setFlat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
        marker.isVisible = false
    }

    fun draw(location: AMapNaviLocation) {
        val apply: (Location) -> Unit = {
            val point = it.point.unBox(LatLng::class.java)
            marker.position = point
            marker.rotateAngle = 360 - it.bearing
            map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                    .tilt(TILT)
                    .zoom(zoom)
                    .bearing(it.bearing)
                    .target(point)
                    .build()))
        }

        marker.isVisible = true
        var animator = valueAnimator;
        if (animator == null) {
            last = Location(Point.box(location.coord), location.bearing)
            apply(last)
            valueAnimator = Companion
        } else {
            if (animator.isRunning && time > FIRST_TIME) {
                time /= 2
            } else {
                time *= 2
            }
            animator.pause()
            animator.removeAllUpdateListeners()
            animator = ValueAnimator.ofObject(Companion,
                    last, Location(Point.box(location.coord), location.bearing))
            animator.addUpdateListener { animation ->
                last = animation.animatedValue as Location
                apply(last)
            }
            animator.duration = time
            animator.start()

            valueAnimator = animator
        }
    }

    companion object : ValueAnimator(), TypeEvaluator<Location> {

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

        private const val FIRST_TIME: Long = 50
        private const val TILT = 80f
    }
}