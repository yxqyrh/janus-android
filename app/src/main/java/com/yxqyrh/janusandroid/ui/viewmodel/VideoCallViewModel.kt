package com.yxqyrh.janusandroid.ui.viewmodel

import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.rtc.repository.VideoCallRepository
import androidx.lifecycle.toLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class VideoCallViewModel : BaseViewModel() {
    val handleIdRef = AtomicLong()
    private val calleeRef = AtomicReference(String())

    val hangupLiveData get() = scarletRepository.hangupProcessor.toLiveData()

    /***
     * RTCRepository
     */
    private val rtcRepository = VideoCallRepository()

    val streamProcessor get() = rtcRepository.streamProcessor
    val statsProcessor get() = rtcRepository.statsProcessor

    suspend fun initClient() {
        (Dispatchers.IO) {
            initDisposable()
            rtcRepository.initClient()
        }
    }

    suspend fun abortClient() = rtcRepository.abortClient()

    suspend fun switchCamera() = rtcRepository.switchCamera()

    suspend fun outCall(callee: String) {
        (Dispatchers.IO) {
            calleeRef.set(callee)
            rtcRepository.outCall(handleIdRef.get(), callee)
        }
    }

    suspend fun incomingCall(typeString: String, sdpString: String) =
        rtcRepository.incomingCall(handleIdRef.get(), typeString, sdpString)

    suspend fun hangup() = scarletRepository.hangup(handleIdRef.get())

    private suspend fun initDisposable() {
        (Dispatchers.IO) {
            compositeDisposable.add(
                scarletRepository
                    .acceptedProcessor
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        when (it.status) {
                            Status.SUCCESS -> {
                                (it.data)?.let { responseModel ->
                                    responseModel.jsep?.type?.let { type ->
                                        responseModel.jsep.sdp?.let { sdp ->
                                            viewModelScope.launch {
                                                rtcRepository.setRemoteDescription(handleIdRef.get(), type, sdp)
                                                scarletRepository.record(handleIdRef.get(), true, "${responseModel.pluginData?.data?.result?.cid ?: ""}${calleeRef.get()?.let { "-caller" } ?: let { "-callee" }}")
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                            }
                        }
                    }
            )
        }
    }
}