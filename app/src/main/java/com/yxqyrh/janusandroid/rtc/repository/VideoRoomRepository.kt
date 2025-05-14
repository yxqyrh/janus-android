package com.yxqyrh.janusandroid.rtc.repository

import com.yxqyrh.janusandroid.datastore.NicknamePreferencesRepository
import com.yxqyrh.janusandroid.model.ResponseModelPublisher
import com.yxqyrh.janusandroid.rtc.RTCClient
import com.yxqyrh.janusandroid.rtc.model.SessionDescriptionState
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class VideoRoomRepository : RTCRepository() {
    private val roomIdRef = AtomicLong()
    private val pinRef = AtomicReference<String?>(null)
    private val publisherListRef = AtomicReference<ArrayList<ResponseModelPublisher>>(ArrayList())

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
                                scarletRepository.publisherCreateOffer(it.handleId, it.sessionDescription).let { offerResponse ->
                                    offerResponse.data?.sender?.let { sender ->
                                        offerResponse.data.jsep?.type?.let { type ->
                                            offerResponse.data.jsep.sdp?.let { sdp ->
                                                rtcClient.setRemoteDescription(sender, type, sdp)
                                            }
                                        }
                                    }
                                }
                                attach(publisherListRef.get())
                            }
                        }
                        SessionDescriptionState.ANSWER -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                scarletRepository.subscriberCreateAnswer(it.handleId, roomIdRef.get(), it.sessionDescription)
                            }
                        }
                    }
                }
        )
    }

    suspend fun attach(publishers: List<ResponseModelPublisher>) {
        (Dispatchers.IO) {
            for (publisher in publishers) {
                publisher.id?.let { feedId ->
                    scarletRepository.attach().let { attachResponse ->
                        attachResponse.data?.data?.id?.let { handleId ->
                            scarletRepository.subscriberJoinRoom(feedId, handleId, roomIdRef.get(), pinRef.get()).let { subscriberResponse ->
                                subscriberResponse.data?.sender?.let { sender ->
                                    subscriberResponse.data.jsep?.type?.let { type ->
                                        subscriberResponse.data.jsep.sdp?.let { sdp ->
                                            rtcClient.createPeerConnection(publisher.display ?: "", feedId, sender, RTCClient.PeerConnectionResourceType.SUBSCRIBER)
                                            rtcClient.createAnswer(sender, type, sdp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun joinRoom(handlerId: Long, room: Long, pin: String?, publisherList: List<ResponseModelPublisher>) {
        (Dispatchers.IO) {
            roomIdRef.set(room)
            pinRef.set(pin)
            // 获取协程锁
            Mutex().withLock(publisherListRef) {
                publisherListRef.set(publisherListRef.get().apply {
                    addAll(publisherList)
                })
            }
            rtcClient.createMediaPeerConnection(NicknamePreferencesRepository.readNickname(mApplication) ?: "", handlerId, RTCClient.PeerConnectionResourceType.PUBLISHER)
            rtcClient.createOffer(handlerId)
        }
    }

    suspend fun leaving(handleId: Long) = rtcClient.detach(handleId)
}