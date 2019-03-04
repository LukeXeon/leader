package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.orhanobut.logger.Logger
import io.reactivex.subjects.PublishSubject
import org.kexie.android.common.widget.LifecycleIdleWorker
import org.kexie.android.dng.navi.viewmodel.entity.InputTip

const val DEBUG_TEXT = "火车站"

const val CITY = "西安"

class InputTipViewModel(application: Application) : AndroidViewModel(application) {

    private val worker = LifecycleIdleWorker<String>(Observer {

        if (it.isNullOrEmpty())
        {
            this@InputTipViewModel.inputTips.postValue(emptyList())
            return@Observer
        }

        val inputTipsQuery = InputtipsQuery(it, CITY)
        val inputTips = Inputtips(getApplication(), inputTipsQuery)
        try {
            val newTips = inputTips.requestInputtips()
                    .filter { tip -> !TextUtils.isEmpty(tip.poiID) }
                    .map { x -> InputTip(x.name, x.poiID) }

            Logger.d("post")

            this@InputTipViewModel.inputTips.postValue(newTips)
        } catch (e: Exception) {
            e.printStackTrace()
            onError.onNext("输入提示查询失败,请检查网络连接")
        }
    })

    val inputTips = MutableLiveData<List<InputTip>>()
            .apply {
                value = emptyList()
            }

    val queryText = MutableLiveData<String>().apply {
        value = DEBUG_TEXT
        value = "车"
    }

    val onError = PublishSubject.create<String>()

    val onSuccess = PublishSubject.create<String>()

    init {
        queryText.observe(worker, worker);
    }

    override fun onCleared() {
        worker.close()
    }
}