package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.os.HandlerThread
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.kexie.android.dng.navi.model.Point
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
import java.util.concurrent.TimeUnit


class InputTipViewModel(application: Application) : AndroidViewModel(application) {

    private val locationSource = AMapLocationClient(application)
            .apply {
                stopLocation()
                setLocationOption(AMapLocationClientOption().apply {
                    interval = 1000
                    locationMode = Hight_Accuracy
                    isNeedAddress = false
                })
                startLocation()
            }

    private val worker = HandlerThread(toString()).apply { start() }

    private val querySubject = PublishSubject.create<String>()
            .apply {
                debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.from(worker.looper))
                        .subscribe(this@InputTipViewModel::query0)
            }

    val inputTips = MutableLiveData<List<InputTip>>()
            .apply {
                value = emptyList()
            }

    val queryText = MutableLiveData<String>()

    val onError = PublishSubject.create<String>()

    val onSuccess = PublishSubject.create<String>()

    fun query(text: String) {
        querySubject.onNext(text)
    }

    private fun query0(text: String) {
        if (text.isEmpty()) {
            this.inputTips.postValue(emptyList())
            return
        }
        val location = locationSource.lastKnownLocation;

        try {
            val search = GeocodeSearch(getApplication());
            val query = RegeocodeQuery(Point.form(location.longitude, location.latitude)
                    .unBox(LatLonPoint::class.java),
                    200f,
                    GeocodeSearch.AMAP)
            val city = search.getFromLocation(query).city

            Logger.d("$text $city")

            val inputTipsQuery = InputtipsQuery(text, city)
            val inputTips = Inputtips(getApplication(), inputTipsQuery)
            val newTips = inputTips.requestInputtips()
                    .filter { tip -> !TextUtils.isEmpty(tip.poiID) }
                    .map { x -> InputTip(x.name, x.poiID) }

            this.inputTips.postValue(newTips)
        } catch (e: Exception) {
            e.printStackTrace()
            onError.onNext("输入提示查询失败,请检查网络连接")
        }
    }

    override fun onCleared() {
        locationSource.stopLocation()
        worker.quitSafely()
    }
}