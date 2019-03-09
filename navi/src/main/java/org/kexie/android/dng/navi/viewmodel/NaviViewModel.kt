package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.model.AMapLaneInfo
import com.amap.api.navi.model.AMapModelCross
import com.amap.api.navi.model.AMapNaviCross
import com.amap.api.navi.model.NaviInfo
import com.autonavi.ae.gmap.gloverlay.GLCrossVector
import org.kexie.android.dng.navi.viewmodel.entity.ModeCross
import org.kexie.android.dng.navi.viewmodel.entity.RunningInfo
import org.kexie.android.dng.navi.widget.DensityUtils
import org.kexie.android.dng.navi.widget.NaviCallback
import org.kexie.android.dng.navi.widget.NaviUtil

class NaviViewModel(application: Application,val navi:NaviController)
    : AndroidViewModel(application) {

    private val naviImpl = NaviImpl()

    private val worker = HandlerThread(toString()).apply {
        start()
    }

    private val handler = Handler(worker.looper)

    init {
        with(navi)
        {
            addAMapNaviListener(naviImpl)
        }
    }

    val runningInfo = MutableLiveData<RunningInfo>()

    val laneInfo = MutableLiveData<AMapLaneInfo>()

    val crossImage = MutableLiveData<AMapNaviCross>()

    val isRunning = MutableLiveData<Boolean>()

    val modeCross = MutableLiveData<ModeCross>()

    val isLockCamera = MutableLiveData<Boolean>()

    private inner class NaviImpl : NaviCallback() {
        override fun onNaviInfoUpdate(naviInfo: NaviInfo?) {
            if (naviInfo != null) {
                val allLength = navi.naviPath.allLength

                val trafficStatuses = navi.getTrafficStatuses(0, 0)

                val pathRetainDistance = naviInfo.pathRetainDistance

                val iconType = naviInfo.iconType

                val nextRoadName = naviInfo.nextRoadName

                val nextRoadDistance = NaviUtil.formatKM(naviInfo.curStepRetainDistance)

                val curStep = naviInfo.curStep

                runningInfo.value = RunningInfo(
                        allLength = allLength,
                        trafficStatuses = trafficStatuses,
                        pathRetainDistance = pathRetainDistance,
                        iconType = iconType,
                        nextRoadName = nextRoadName,
                        nextRoadDistance = nextRoadDistance,
                        curStep = curStep)

            }
        }

        override fun hideLaneInfo() {
            laneInfo.value = null
        }

        override fun showLaneInfo(aMapLaneInfo: AMapLaneInfo?) {
            laneInfo.value = aMapLaneInfo;
        }

        override fun showCross(aMapNaviCross: AMapNaviCross?) {
            crossImage.value = aMapNaviCross
        }

        override fun hideCross() {
            crossImage.value = null
        }

        override fun showModeCross(aMapModelCross: AMapModelCross?) {
            if (aMapModelCross == null) {
                return
            }
            handler.post {
                try {
                    val context = getApplication<Application>()
                    val attr = GLCrossVector.AVectorCrossAttr()
                    // 设置显示区域
                    attr.stAreaRect = Rect(0, DensityUtils.dp2px(context, 50f),
                            DensityUtils.getScreenWidth(getApplication()),
                            DensityUtils.dp2px(context,
                                    300f))

                    //        attr.stAreaRect = new Rect(0, dp2px(48), nWidth, dp2px(290));
                    attr.stAreaColor = Color.argb(217, 95, 95, 95)/* 背景颜色 */
                    attr.fArrowBorderWidth = DensityUtils.dp2px(context, 22f)/* 箭头边线宽度 */
                    attr.stArrowBorderColor = Color.argb(0, 0, 50, 20)/* 箭头边线颜色 */
                    attr.fArrowLineWidth = DensityUtils.dp2px(context, 18f)/* 箭头内部宽度 */
                    attr.stArrowLineColor = Color.argb(255, 255, 253, 65)/* 箭头内部颜色 */
                    attr.dayMode = false
                    attr.fArrowLineWidth = 18/* 箭头内部宽度 */
                    attr.stArrowLineColor = Color.argb(255, 255, 253, 65)/* 箭头内部颜色 */
                    attr.dayMode = true
                    val inputStream = context
                            .resources
                            .assets
                            .open("vector3d_arrow_in.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    val newModelCross = ModeCross(
                            buffer = aMapModelCross.picBuf1,
                            attr = attr,
                            res = bitmap)

                    modeCross.postValue(newModelCross)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        override fun hideModeCross() {
            modeCross.value = null
        }

    }

    fun start(id: Int) {
        navi.selectRouteId(id)
        navi.startNavi(NaviType.EMULATOR)
    }
}
