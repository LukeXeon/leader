package org.kexie.android.dng.navi.entity;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;

/**
 * Created by Luke on 2018/12/27.
 */

interface PointCompat
{
    double getLatitude();

    double getLongitude();

    LatLng toLatLng();

    LatLonPoint toLatLonPoint();

    NaviLatLng toNaviLatLng();
}
