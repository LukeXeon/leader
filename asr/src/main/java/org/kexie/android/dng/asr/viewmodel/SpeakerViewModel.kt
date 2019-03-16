package org.kexie.android.dng.asr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.alibaba.android.arouter.launcher.ARouter
import io.reactivex.Observable
import okhttp3.OkHttpClient
import org.kexie.android.dng.asr.model.AIService
import org.kexie.android.dng.asr.model.entity.AIRequest
import org.kexie.android.dng.asr.viewmodel.entity.Message
import org.kexie.android.dng.common.app.PR
import org.kexie.android.dng.common.model.SpeakerService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class SpeakerViewModel(application: Application) : AndroidViewModel(application) {

    private var speakerService: SpeakerService = ARouter.getInstance()
            .build(PR.asr.service)
            .navigation() as SpeakerService

    private val aiService: AIService
    val status: LiveData<SpeakerService.Status>
    val volume: LiveData<Int>
    val nextMessage: Observable<Message>
    val weakUp: Observable<String>

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://openapi.tuling123.com")
                .client(OkHttpClient())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        aiService = retrofit.create(AIService::class.java)

        nextMessage = speakerService.finalResult.map {
            Message(Message.TYPE_USER, it)
        }.flatMap {
            Observable.just(it).mergeWith(post(it.text))
        }

        volume = speakerService.currentVolume
        status = speakerService.currentStatus
        weakUp = speakerService.weakUpResult
    }

    private fun post(text: String): Observable<Message> {
        val request = AIRequest()
        return aiService.post(request)
                .flatMap {
                    Observable.fromArray(*it.results.toTypedArray())
                }.filter {
                    it.resultType == "text" && !it.values.text.isNullOrEmpty()
                }.map {
                    it.values.text
                }.map {
                    Message(Message.TYPE_AI, it)
                }.doOnError {
                    it.printStackTrace()
                }
    }

    fun beginTransaction() {
        speakerService.beginTransaction()
    }

    fun endTransaction() {
        speakerService.endTransaction()
    }
}
