package org.kexie.android.dng.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.alibaba.android.arouter.launcher.ARouter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.kexie.android.dng.ai.R
import org.kexie.android.dng.ai.model.AIService
import org.kexie.android.dng.ai.model.entity.AIRequest
import org.kexie.android.dng.ai.model.entity.AIResponse
import org.kexie.android.dng.ai.viewmodel.entity.Message
import org.kexie.android.dng.ai.widget.CookieCache
import org.kexie.android.dng.common.app.PR
import org.kexie.android.dng.common.model.ASRService
import org.kexie.android.dng.common.model.TTSService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SpeakerViewModel(application: Application) : AndroidViewModel(application) {

    private val asrService = ARouter.getInstance()
            .build(PR.ai.asr_service)
            .navigation() as ASRService
    private val ttsService = ARouter.getInstance()
            .build(PR.ai.tts_service)
            .navigation() as TTSService

    private val aiService: AIService

    val status: LiveData<ASRService.Status>
    val volume: LiveData<Int>
    val weakUp: Observable<String>
    val nextMessage: Observable<Message>
    val partialResult:Observable<String>


    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(getApplication<Application>().getString(R.string.ai_open_api_url))
                .client(OkHttpClient.Builder()
                        .callTimeout(3, TimeUnit.SECONDS)
                        .cookieJar(CookieCache())
                        .build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        aiService = retrofit.create(AIService::class.java)
        nextMessage = asrService.finalResult
                .map {
                    Message(Message.TYPE_USER, it)
                }.observeOn(Schedulers.io())
                .flatMap {
                    Observable.just(it).mergeWith(sendToAI(it.text))
                }.observeOn(AndroidSchedulers.mainThread())

        volume = asrService.currentVolume
        status = asrService.currentStatus
        weakUp = asrService.weakUpResult
        partialResult = asrService.partialResult
    }

    private fun sendToAI(text: String): Observable<Message> {
        val context = getApplication<Application>()
        val request = AIRequest()
        request.reqType = 0
        request.perception = AIRequest.Perception()
        request.perception.inputText = AIRequest.Perception.InputText()
        request.perception.inputText.text = text
        request.userInfo = AIRequest.UserInfo()
        request.userInfo.apiKey = context.getString(R.string.ai_api_key)
        request.userInfo.userId = context.getString(R.string.ai_user_id)
        return aiService.post(request)
                .flatMap {
                    Observable.fromArray(*it.results.toTypedArray())
                }.onErrorReturn {
                    AIResponse.Result()
                }
                .filter {
                    it.resultType == "text" && !it.values.text.isNullOrEmpty()
                }
                .map {
                    it.values.text
                }
                .doOnNext {
                    ttsService.send(it)
                }
                .map {
                    Message(Message.TYPE_AI, it)
                }
    }

    fun beginTransaction() {
        asrService.beginTransaction()
    }

    fun endTransaction() {
        asrService.endTransaction()
    }

    override fun onCleared() {
        ttsService.stop()
    }
}
