package org.kexie.android.dng.navi.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.orhanobut.logger.Logger
import io.reactivex.subjects.PublishSubject
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
import java.util.concurrent.Executors

const val DEBUG_TEXT = "火车站"

const val CITY = "西安"

class InputTipViewModel(application: Application) : AndroidViewModel(application) {

    private val singleTask = Executors.newSingleThreadExecutor()

    val inputTips = MutableLiveData<List<InputTip>>()

    val onError = PublishSubject.create<String>()

    val onSuccess = PublishSubject.create<String>()

    init {
        inputTips.value = emptyList()
    }

    @MainThread
    fun query(text: String) {
        Logger.d(text)
        singleTask.execute {
            val inputTipsQuery = InputtipsQuery(text, CITY)
            val inputTips = Inputtips(getApplication(), inputTipsQuery)
            try {

                val newTips = inputTips.requestInputtips()
                        .filter { tip -> !TextUtils.isEmpty(tip.getPoiID()) }
                        .map { x -> InputTip(x.getName(), x.getPoiID()) }
                        .toList()

                this.inputTips.postValue(newTips)

                onSuccess.onNext("搜索成功")

            } catch (e: Exception) {
                e.printStackTrace()
                onError.onNext("输入提示查询失败,请检查网络连接")
            }
        }
    }
}