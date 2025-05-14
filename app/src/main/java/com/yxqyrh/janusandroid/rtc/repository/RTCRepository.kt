package com.yxqyrh.janusandroid.rtc.repository

import com.yxqyrh.janusandroid.core.VideoRoomApplication
import com.yxqyrh.janusandroid.rtc.RTCClient
import com.yxqyrh.janusandroid.rtc.model.CandidateState
import androidx.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

abstract class RTCRepository {
    protected val mApplication by lazy { VideoRoomApplication.application }
    protected val scarletRepository by lazy { mApplication.scarletInstance.scarletRepository }
    protected val compositeDisposable by lazy { CompositeDisposable() }
    protected val rtcClient by lazy { RTCClient() }

    /**
     * Processor转化成LiveData时，会丢失其背压特性
     * 所以在本地流处理时 尽量使用Processor传递事件
     */
    val streamProcessor get() = rtcClient.streamProcessor
    val statsProcessor get() = rtcClient.statsProcessor

    @CallSuper
    protected open fun initDisposable() {
        compositeDisposable.add(
            rtcClient
                .candidateProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    when (it.state) {
                        CandidateState.TRICKLE -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                (it.candidate)?.let { candidate ->
                                    scarletRepository.trickleCandidate(it.handleId, candidate)
                                }
                            }
                        }
                        CandidateState.COMPLETE -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                scarletRepository.completeCandidate(it.handleId)
                            }
                        }
                    }
                }
        )
    }

    suspend fun initClient() {
        (Dispatchers.IO) {
            initDisposable()
            rtcClient.initClient()
        }
    }

    suspend fun switchAudio() = rtcClient.switchAudio()
    suspend fun switchVideo() = rtcClient.switchVideo()

    suspend fun abortClient() {
        (Dispatchers.IO) {
            compositeDisposable.dispose()
        }
        (Dispatchers.Main) {
            rtcClient.abort()
        }
    }

    suspend fun switchCamera() = rtcClient.switchCamera()

}