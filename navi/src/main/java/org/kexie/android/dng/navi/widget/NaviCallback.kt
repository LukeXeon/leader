@file:Suppress("DEPRECATION")

package org.kexie.android.dng.navi.widget

import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.model.*
import com.autonavi.tbt.TrafficFacilityInfo

/**
 * Created by Luke on 2018/12/27.
 */

@Suppress("OverridingDeprecatedMember", "DEPRECATION")
abstract class NaviCallback : AMapNaviListener {
    override fun onInitNaviFailure() {}

    override fun onInitNaviSuccess() {

    }

    override fun onStartNavi(i: Int) {

    }

    override fun onTrafficStatusUpdate() {

    }

    override fun onLocationChange(aMapNaviLocation: AMapNaviLocation) {

    }

    override fun onGetNavigationText(i: Int, s: String) {

    }

    override fun onGetNavigationText(s: String) {

    }

    override fun onEndEmulatorNavi() {

    }

    override fun onArriveDestination() {

    }

    override fun onCalculateRouteFailure(i: Int) {

    }

    override fun onReCalculateRouteForYaw() {

    }

    override fun onReCalculateRouteForTrafficJam() {

    }

    override fun onArrivedWayPoint(i: Int) {

    }

    override fun onGpsOpenStatus(b: Boolean) {

    }

    override fun onNaviInfoUpdate(naviInfo: NaviInfo) {

    }

    override fun onNaviInfoUpdated(aMapNaviInfo: AMapNaviInfo) {

    }

    override fun updateCameraInfo(aMapNaviCameraInfos: Array<AMapNaviCameraInfo>) {

    }

    override fun updateIntervalCameraInfo(aMapNaviCameraInfo: AMapNaviCameraInfo,
                                          aMapNaviCameraInfo1: AMapNaviCameraInfo,
                                          i: Int) {

    }

    override fun onServiceAreaUpdate(aMapServiceAreaInfos: Array<AMapServiceAreaInfo>) {

    }

    override fun showCross(aMapNaviCross: AMapNaviCross) {

    }

    override fun hideCross() {

    }

    override fun showModeCross(aMapModelCross: AMapModelCross) {

    }

    override fun hideModeCross() {

    }

    override fun showLaneInfo(aMapLaneInfos: Array<AMapLaneInfo>, bytes: ByteArray, bytes1: ByteArray) {

    }

    override fun showLaneInfo(aMapLaneInfo: AMapLaneInfo) {

    }

    override fun hideLaneInfo() {

    }

    override fun onCalculateRouteSuccess(ints: IntArray) {

    }

    override fun notifyParallelRoad(i: Int) {

    }

    override fun OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfo: AMapNaviTrafficFacilityInfo) {

    }

    override fun OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfos: Array<AMapNaviTrafficFacilityInfo>) {

    }

    override fun OnUpdateTrafficFacility(trafficFacilityInfo: TrafficFacilityInfo) {

    }

    override fun updateAimlessModeStatistics(aimLessModeStat: AimLessModeStat) {

    }

    override fun updateAimlessModeCongestionInfo(aimLessModeCongestionInfo: AimLessModeCongestionInfo) {

    }

    override fun onPlayRing(i: Int) {

    }

    override fun onCalculateRouteSuccess(aMapCalcRouteResult: AMapCalcRouteResult) {

    }

    override fun onCalculateRouteFailure(aMapCalcRouteResult: AMapCalcRouteResult) {

    }

    override fun onNaviRouteNotify(aMapNaviRouteNotifyData: AMapNaviRouteNotifyData) {

    }
}
