package org.kexie.android.dng.asr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.alibaba.android.arouter.launcher.ARouter
import io.reactivex.Observable
import org.kexie.android.dng.asr.viewmodel.entity.Message
import org.kexie.android.dng.common.app.PR
import org.kexie.android.dng.common.model.SpeakerService

class SpeakerViewModel(application: Application) : AndroidViewModel(application) {

    private var speakerService: SpeakerService = ARouter.getInstance()
            .build(PR.asr.service)
            .navigation() as SpeakerService

    val status: LiveData<SpeakerService.Status>
    val volume: LiveData<Int>
    val nextMessage: Observable<Message>
    val weakUp: Observable<String>

    init {
        nextMessage = speakerService.finalResult.map {
            Message(Message.TYPE_USER, it)
        }
        volume = speakerService.currentVolume
        status = speakerService.currentStatus
        weakUp = speakerService.weakUpResult
    }

    fun beginTransaction() {
        speakerService.beginTransaction()
    }

    fun endTransaction() {
        speakerService.endTransaction()
    }
}
