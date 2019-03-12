package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.amap.api.maps.AMapException
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.navi.model.NaviPath
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiSearch
import com.blankj.utilcode.util.NetworkUtils
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.Exceptions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.kexie.android.dng.navi.model.Point
import org.kexie.android.dng.navi.model.Query
import org.kexie.android.dng.navi.viewmodel.entity.GuideInfo
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
import org.kexie.android.dng.navi.viewmodel.entity.RouteInfo
import org.kexie.android.dng.navi.widget.AMapCompat
import org.kexie.android.dng.navi.widget.NaviCallback
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
typealias NaviController = com.amap.api.navi.AMapNavi;

class QueryViewModel(application: Application,private val navi:NaviController)
    : AndroidViewModel(application) {

    private val worker = HandlerThread(toString())
            .apply {
                start()
            }

    var networkTest: Boolean = false
        private set

    val routes = MutableLiveData<Map<Int, RouteInfo>>()

    val currentSelect = MutableLiveData<Int>()

    val isLoading = MutableLiveData<Boolean>()

    val onError = PublishSubject.create<String>()

    val onSuccess = PublishSubject.create<String>()

    init {
        ping()
    }

    fun query(query: Query) {
        isLoading.value = true
        query0(Observable.just(query))
    }

    fun select(id: Int) {
        currentSelect.value = id;
        if (id != NO_SELECT) {
            navi.selectRouteId(id)
        }
    }

    fun query(inputTip: InputTip) {

        isLoading.value = true

        val query = Observable.just(inputTip)
                .observeOn(Schedulers.io())
                .map {
                    val query = PoiSearch.Query(it.text, "")
                            .apply {
                                isDistanceSort = false
                                requireSubPois(true)
                            }
                    val search = PoiSearch(getApplication(), query)
                    try {
                        val item = search.searchPOIId(it.poiId)
                        getPoiPoint(item)
                    } catch (e: AMapException) {
                        throw Exceptions.propagate(e)
                    }
                }.map {
                    Query.Builder()
                            .to(it)
                            .mode(10)
                            .build()!!
                }

        query0(query)

    }

    private fun query0(query: Observable<Query>) {

        query.observeOn(AndroidSchedulers.from(worker.looper))
                .map {
                    val ids = getRouteIds(it)
                    if (ids.isEmpty())
                        emptyMap()
                    else
                        ids.asSequence().map { x ->
                            x to getRouteInfo(x)
                        }.toMap()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Map<Int, RouteInfo>> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: Map<Int, RouteInfo>) {
                        routes.value = t
                        if (!t.isEmpty()) {
                            select(t.keys.first())
                            onSuccess.onNext("路径规划成功")
                        }
                    }

                    override fun onError(e: Throwable) {
                        onError.onNext("路径规划失败,请检查网络连接")
                    }

                    override fun onComplete() {
                        isLoading.value = false
                    }
                })
    }

    private fun getRouteIds(query: Query): Array<Int> {
        return try {
            val lock = ReentrantLock()

            val condition = lock.newCondition()

            var result: IntArray? = null

            navi.addAMapNaviListener(
                    object : NaviCallback() {
                        @Suppress("OverridingDeprecatedMember")
                        override fun onCalculateRouteFailure(i: Int) {
                            lock.withLock {
                                navi.removeAMapNaviListener(this)
                                Logger.d("error code $i")
                                condition.signalAll()
                            }
                        }

                        @Suppress("OverridingDeprecatedMember")
                        override fun onCalculateRouteSuccess(ints: IntArray) {
                            lock.withLock {
                                navi.removeAMapNaviListener(this)
                                result = ints
                                condition.signalAll()
                            }
                        }
                    })

            lock.withLock {

                val to = if (query.to == null)
                    emptyList()
                else
                    listOf(query.to.unBox(NaviLatLng::class.java))
                val ways = if (query.ways.isNullOrEmpty())
                    emptyList()
                else
                    query.ways.map { p -> p.unBox(NaviLatLng::class.java) }
                            .toList()

                navi.calculateDriveRoute(to, ways, query.mode)

                condition.await()

            }

            result?.toTypedArray() ?: emptyArray()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exceptions.propagate(e)
        }
    }

    override fun onCleared() {
        navi.destroy()
        worker.quit()
    }

    private fun getRouteInfo(id: Int): RouteInfo {
        val path = AMapCompat.getAllNaviPath(navi)[id]!!
        return RouteInfo.Builder()
                .length(getPathLength(path.allLength))
                .time(getPathTime(path.allTime))
                .name(path.amapNaviPath.labels)
                .path(path.amapNaviPath)
                .guideInfos(getGuideInfo(path))
                .build()
    }

    @Suppress("MissingPermission", "CheckResult")
    private fun ping() {
        Observable.just(Unit)
                .observeOn(Schedulers.io())
                .map { NetworkUtils.isAvailableByPing() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isAvailable ->
                    networkTest = isAvailable
                    if (!isAvailable) {
                        onError.onNext("无网络连接")
                    }
                }
    }

    companion object {

        const val NO_SELECT = Int.MIN_VALUE

        private fun getPoiPoint(item: PoiItem): Point {

            val point =
                    if (item.enter != null)
                        item.enter
                    else
                        if (item.exit != null)
                            item.exit
                        else
                            item.latLonPoint
            return Point.box(point)
        }

        private fun getGuideInfo(naviPath: NaviPath): List<GuideInfo> {
            val steps = ArrayList<GuideInfo>()
            val aMapNaviGuides = naviPath.guideList
            val path = naviPath.amapNaviPath
            val aMapNaviSteps = path.steps

            for (j in aMapNaviGuides.indices) {
                val g = aMapNaviGuides[j]
                val group = GuideInfo()
                group.groupIconType = g.iconType
                group.groupLen = g.length
                group.groupName = g.name
                group.groupToll = g.toll
                val count = g.segCount
                val startSeg = g.startSegId
                var traffics = 0
                for (i in startSeg until count + startSeg) {
                    val step = aMapNaviSteps[i]
                    traffics += step.trafficLightNumber
                    var roadName: String
                    if (i == count + startSeg - 1 && j == aMapNaviGuides.size - 1) {
                        roadName = "终点"
                    } else if (i == count + startSeg - 1 && j + 1 < aMapNaviGuides.size - 1) {
                        val ag = aMapNaviGuides[j + 1]
                        roadName = ag.name
                    } else {
                        roadName = step.links[0].roadName
                    }

                    val lbsGuidStep = GuideInfo.Step(step.iconType,
                            roadName, step.length)
                    group.steps.add(lbsGuidStep)

                }
                group.groupTrafficLights = traffics
                steps.add(group)
            }
            return steps
        }

        private fun getPathTime(time: Int): String {
            if (time.toLong() > 3600) {
                val hour = time.toLong() / 3600
                val miniate = time.toLong() % 3600 / 60
                return hour.toString() + "小时" + miniate + "分钟"
            }
            if (time.toLong() >= 60) {
                val miniate = time.toLong() / 60
                return miniate.toString() + "分钟"
            }
            return time.toLong().toString() + "秒"
        }

        private fun getPathLength(path: Int): String {
            if (path > 10000)
            // 10 km
            {
                val dis = (path / 1000).toFloat()
                return dis.toString() + "千米"
            }
            if (path > 1000) {
                val dis = path.toFloat() / 1000
                val fnum = DecimalFormat("##0.0")
                val dstr = fnum.format(dis.toDouble())
                return dstr + "千米"
            }
            if (path > 100) {
                val dis = (path / 50 * 50).toFloat()
                return dis.toString() + "米"
            }
            var dis = (path / 10 * 10).toFloat()
            if (dis == 0f) {
                dis = 10f
            }
            return dis.toString() + "米"
        }
    }
}