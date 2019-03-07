package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.os.HandlerThread
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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

    private val worker = HandlerThread(toString()).apply { start() }

    private val querySubject = PublishSubject.create<Pair<String, Point>>()
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

    fun query(text: String, location: Point) {
        querySubject.onNext(text to location)
    }

    private fun query0(pair: Pair<String, Point>) {
        val text = pair.first

        if (text.isEmpty()) {
            this.inputTips.postValue(emptyList())
            return
        }

        val point = pair.second.unBox(LatLonPoint::class.java)

        val search = GeocodeSearch(getApplication());
        val query = RegeocodeQuery(point, 200f, GeocodeSearch.AMAP)
        val city = search.getFromLocation(query).city

        Logger.d("$text $city")


        val inputTipsQuery = InputtipsQuery(text, city)
        val inputTips = Inputtips(getApplication(), inputTipsQuery)
        try {
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
        worker.quitSafely()
    }
}