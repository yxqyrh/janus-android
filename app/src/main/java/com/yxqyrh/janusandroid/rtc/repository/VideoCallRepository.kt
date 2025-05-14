package com.yxqyrh.janusandroid.rtc.repository

import com.yxqyrh.janusandroid.rtc.RTCClient
import com.yxqyrh.janusandroid.rtc.model.SessionDescriptionState
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class VideoCallRepository : RTCRepository() {
    private val calleeRef = AtomicReference(String())

    override fun initDisposable() {
        super.initDisposable()
        compositeDisposable.add(
            rtcClient
                .sessionDescriptionProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    when (it.state) {
                        SessionDescriptionState.OFFER -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                scarletRepository.call(it.handleId, calleeRef.get(), it.sessionDescription)
                            }
                        }
                        SessionDescriptionState.ANSWER -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                scarletRepository.accept(it.handleId, it.sessionDescription)
                            }
                        }
                    }
                }
        )
    }

    suspend fun outCall(handleId: Long, callee: String) {
        (Dispatchers.IO) {
            calleeRef.set(callee)
            rtcClient.createMediaPeerConnection("", handleId, RTCClient.PeerConnectionResourceType.PUBLISHER)
            rtcClient.createOffer(handleId)
        }
    }

    suspend fun incomingCall(handleId: Long, typeString: String, sdpString: String) {
        (Dispatchers.IO) {
            rtcClient.createMediaPeerConnection("", handleId, RTCClient.PeerConnectionResourceType.SUBSCRIBER)
            rtcClient.createAnswer(handleId, typeString, sdpString)
        }
    }

    suspend fun setRemoteDescription(handleId: Long, type: String, sdp: String) {
        (Dispatchers.IO) {
            rtcClient.setRemoteDescription(handleId, type, sdp)
        }
    }
}