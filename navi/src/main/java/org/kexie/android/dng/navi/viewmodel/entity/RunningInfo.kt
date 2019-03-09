package org.kexie.android.dng.navi.viewmodel.entity

import com.amap.api.navi.model.AMapTrafficStatus

data class RunningInfo(
        val allLength:Int,
        val trafficStatuses:List<AMapTrafficStatus>,
        val pathRetainDistance: Int,
        val iconType:Int,
        val nextRoadName:String,
        val nextRoadDistance:String,
        val curStep:Int)