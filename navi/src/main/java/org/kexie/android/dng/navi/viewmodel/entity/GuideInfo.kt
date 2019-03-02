package org.kexie.android.dng.navi.viewmodel.entity

import android.text.TextUtils
import java.util.*

/**
 * Created by shixin on 2017/6/22.
 */

class GuideInfo {
    var groupName: String? = null
    var groupLen: Int = 0
    var groupTrafficLights: Int = 0
    var groupIconType: Int = 0
    var groupToll: Int = 0
    val steps: List<Step>

    init {
        steps = ArrayList()
    }

    class Step(val stepIconType: Int, roadName: String, val stepDistance: Int) {
        var stepRoadName: String? = null
            private set

        init {
            if (TextUtils.isEmpty(roadName)) {
                stepRoadName = "内部道路"
            } else {
                stepRoadName = roadName
            }

        }
    }
}


