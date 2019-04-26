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
import com.amap.api.navi.model.*
import com.autonavi.ae.gmap.gloverlay.GLCrossVector
import io.reactivex.subjects.PublishSubject
import org.kexie.android.dng.common.app.PR
import org.kexie.android.dng.common.widget.navigationAs
import org.kexie.android.dng.common.model.TTSService
import org.kexie.android.dng.navi.viewmodel.entity.ModeCross
import org.kexie.android.dng.navi.viewmodel.entity.RunningInfo
import org.kexie.android.dng.navi.widget.DensityUtils
import org.kexie.android.dng.navi.widget.NaviCallback
import org.kexie.android.dng.navi.widget.NaviUtil


class RunningViewModel(
        application: Application,
        private val navi: NaviController
) : AndroidViewModel(application) {

    private val ttsService = navigationAs<TTSService>(PR.ai.tts_service)

    private val worker = HandlerThread(toString())
            .apply {
                start()
            }

    private val handler = Handler(worker.looper)

    init {
        with(navi)
        {
            addAMapNaviListener(object : NaviCallback() {
                override fun onNaviInfoUpdate(naviInfo: NaviInfo?) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
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
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    laneInfo.value = null
                }

                override fun showLaneInfo(aMapLaneInfo: AMapLaneInfo?) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    laneInfo.value = aMapLaneInfo;
                }

                override fun showCross(aMapNaviCross: AMapNaviCross?) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    crossImage.value = aMapNaviCross
                }

                override fun hideCross() {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    crossImage.value = null
                }

                override fun showModeCross(aMapModelCross: AMapModelCross?) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
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
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    modeCross.value = null
                }

                override fun notifyParallelRoad(i: Int) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    if (i == 0) {
                        onInfo.onNext("当前在主辅路过渡")
                        return
                    }
                    if (i == 1) {
                        onInfo.onNext("当前在主路");
                        return
                    }
                    if (i == 2) {
                        onInfo.onNext("当前在辅路");
                    }
                }

                override fun onLocationChange(aMapNaviLocation: AMapNaviLocation?) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    if (aMapNaviLocation != null) {
                        location.value = aMapNaviLocation
                    }
                }

                override fun updateCameraInfo(aMapNaviCameraInfos: Array<out AMapNaviCameraInfo>?) {
                    val isRun = isRunning.value;
                    if (isRun == null || !isRun) {
                        return
                    }
                    if (aMapNaviCameraInfos != null) {
                        cameraInfo.value = aMapNaviCameraInfos;
                    }
                }

                override fun onGetNavigationText(playText: String?) {
                    if (!playText.isNullOrEmpty()) {
                        ttsService.send(playText)
                    }
                }

            })
            setUseInnerVoice(false)
        }
    }

    val location = MutableLiveData<AMapNaviLocation>()

    val runningInfo = MutableLiveData<RunningInfo>()

    val laneInfo = MutableLiveData<AMapLaneInfo>()

    val crossImage = MutableLiveData<AMapNaviCross>()

    val isRunning = MutableLiveData<Boolean>()

    val modeCross = MutableLiveData<ModeCross>()

    val cameraInfo = MutableLiveData<Array<out AMapNaviCameraInfo>>()

    val isLockCamera = MutableLiveData<Boolean>()

    val onInfo = PublishSubject.create<String>()

    override fun onCleared() {
        ttsService.stop()
        navi.stopNavi()
    }

    fun start() {
        navi.setEmulatorNaviSpeed(40)
        navi.startNavi(NaviType.EMULATOR)
    }
}