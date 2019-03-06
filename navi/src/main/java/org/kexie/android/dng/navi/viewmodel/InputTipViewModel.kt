package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.os.HandlerThread
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
import java.util.concurrent.TimeUnit

const val DEBUG_TEXT = "火车站"

const val CITY = "西安"

class InputTipViewModel(application: Application) : AndroidViewModel(application) {

    private val worker = HandlerThread(toString()).apply { start() }

    private val querySubject = PublishSubject.create<String>().apply {
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

    fun query(it: String) {
        querySubject.onNext(it)
    }

    private fun query0(it: String) {
        Logger.d(it)
        if (it.isEmpty()) {
            this.inputTips.postValue(emptyList())
            return
        }

        val inputTipsQuery = InputtipsQuery(it, CITY)
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