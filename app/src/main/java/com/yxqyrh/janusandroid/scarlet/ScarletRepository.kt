package com.yxqyrh.janusandroid.scarlet

import com.yxqyrh.janusandroid.core.StateData
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.core.VideoRoomApplication
import com.yxqyrh.janusandroid.datastore.NicknamePreferencesRepository
import com.yxqyrh.janusandroid.datastore.TokenPreferencesRepository
import com.yxqyrh.janusandroid.model.*
import com.yxqyrh.janusandroid.model.enumModel.Janus
import com.yxqyrh.janusandroid.model.enumModel.Plugin
import com.yxqyrh.janusandroid.model.enumModel.Request
import com.yxqyrh.janusandroid.utils.Logger.log
import com.yxqyrh.janusandroid.utils.MoshiUtil
import com.yxqyrh.janusandroid.utils.RandomUtil
import com.yxqyrh.janusandroid.utils.ToastUtil.showToast
import android.annotation.SuppressLint
import android.util.Log
import com.tinder.scarlet.websocket.WebSocketEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@SuppressLint("CheckResult")
class ScarletRepository(private val scarletService: ScarletService) {
    private val context = VideoRoomApplication.application
    private val compositeDisposable = CompositeDisposable()

    /**
     * Observer对象只会在数据更新的时候传递数据
     * LiveData对象则无论数据是否更新都会传递数据
     * 因为这里的Repository使用了单例模式
     * 为了避免ViewModel创建时的重复响应
     * 改用PublishProcessor
     */
    private val responseProcessor = PublishProcessor.create<StateData<ResponseModel>>()

    val createSessionProcessor = PublishProcessor.create<StateData<ResponseModel>>()

    val publishersProcessor = PublishProcessor.create<StateData<ResponseModel>>()
    val moderationProcessor = PublishProcessor.create<StateData<ResponseModel>>()

    val acceptedProcessor = PublishProcessor.create<StateData<ResponseModel>>()
    val incomingCallProcessor = PublishProcessor.create<StateData<ResponseModel>>()

    val hangupProcessor = PublishProcessor.create<StateData<ResponseModel>>()

    init {
        scarletService
            .observeWebSocketEvent()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .filter { it is WebSocketEvent.OnConnectionOpened }
            .subscribe { openedEvent ->
                log("openedEvent:\n$openedEvent")
                CoroutineScope(Dispatchers.IO).launch {
                    createSession().let {
                        when (it.status) {
                            Status.SUCCESS -> {
                                it.data?.data?.id?.let { sessionId ->
                                    TokenPreferencesRepository.saveSessionId(context, sessionId)

                                    compositeDisposable.clear()
                                    compositeDisposable.add(
                                        Observable
                                            .interval(3000, TimeUnit.MILLISECONDS)
                                            .observeOn(Schedulers.io())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe {
                                                keepAlive(sessionId)
                                            }
                                    )
                                }
                            }
                            else -> {
                            }
                        }
                        createSessionProcessor.onNext(it)
                    }
                }
            }

        scarletService
            .observeReceivedData()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe { receivedData ->
                log("receivedData:\n$receivedData", if (!receivedData.contains("keep_alive")) Log.ASSERT else Log.INFO)

                kotlin.runCatching { MoshiUtil.fromJson<ResponseModel>(receivedData) }.getOrNull()?.let { responseModel: ResponseModel ->
                    when (responseModel.janus) {
                        "timeout" -> showToast("连接超时")
                        "error" -> {
                            responseModel.transaction?.let { transaction ->
                                responseModel.error?.reason?.let { errorReason ->
                                    showToast(errorReason)
                                    processReceivedData(transaction, Status.ERROR, null, errorReason)
                                }
                            }
                        }
                        "success" -> {
                            responseModel.transaction?.let { transaction ->
                                responseModel.pluginData?.data?.error?.let { errorReason ->
                                    showToast(errorReason)
                                    processReceivedData(transaction, Status.ERROR, null, errorReason)
                                } ?: processReceivedData(transaction, Status.SUCCESS, responseModel)
                            }
                        }
                        "ack" -> {
                        }
                        "event" -> {
                            responseModel.transaction?.let { transaction ->
                                responseModel.pluginData?.data?.error?.let { errorReason ->
                                    showToast(errorReason)
                                    processReceivedData(transaction, Status.ERROR, null, errorReason)
                                } ?: processReceivedData(transaction, Status.SUCCESS, responseModel)
                            } ?: let {
                                when (responseModel.pluginData?.plugin) {
                                    Plugin.VIDEO_ROOM.plugin -> {
                                        if (responseModel.pluginData.data?.publishers != null) {
                                            publishersProcessor.onNext(StateData(Status.SUCCESS, responseModel))
                                        } else if (responseModel.pluginData.data?.audioModeration != null
                                            && responseModel.pluginData.data.videoModeration != null
                                        ) {
                                            moderationProcessor.onNext(StateData(Status.SUCCESS, responseModel))
                                        } else {
                                        }
                                    }
                                    Plugin.VIDEO_CALL.plugin -> {
                                        responseModel.pluginData.data?.result?.event?.let { event ->
                                            when (event) {
                                                "accepted" -> {
                                                    acceptedProcessor.onNext(StateData(Status.SUCCESS, responseModel))
                                                }
                                                "incomingcall" -> {
                                                    incomingCallProcessor.onNext(StateData(Status.SUCCESS, responseModel))
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                    }
                                }
                            }
                        }
                        "hangup" -> {
                            responseModel.sender?.let {
                                hangupProcessor.onNext(StateData(Status.SUCCESS, responseModel))
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
    }

    private fun processReceivedData(transaction: String, status: Status, responseModel: ResponseModel?, message: String? = null) {
        responseModel?.let { responseProcessor.onNext(StateData(status, responseModel, transaction, message)) }
            ?: let { responseProcessor.onNext(StateData(status, null, transaction, message)) }
    }

    private suspend fun SendModel.sendWithResponse(): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            send()
            suspendCoroutine { continuation ->
                CompositeDisposable().apply {
                    add(responseProcessor
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .filter { it.transaction == transaction }
                        .doAfterNext { dispose() }
                        .doOnError {
                            continuation.resumeWithException(it)
                        }
                        .subscribe {
                            continuation.resume(it)
                        })
                }
            }
        }
    }

    private fun SendModel.send() {
        scarletService.sendData(
            MoshiUtil.toJson(this@send).apply {
                log("SendData", "${transaction}:\n$this", if (!contains("keep_alive")) Log.ASSERT else Log.INFO)
            })
    }

    private fun keepAlive(sessionId: Long) {
        SendModel(
            janus = Janus.KEEP_ALIVE.janus,
            transaction = "keep_alive",
            sessionId = sessionId
        ).send()
    }

    private suspend fun createSession(): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.CREATE.janus,
                transaction = RandomUtil.randomTransaction()
            ).sendWithResponse()
        }
    }

    suspend fun createHandle(plugin: Plugin): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.ATTACH.janus,
                transaction = RandomUtil.randomTransaction(),
                plugin = plugin.plugin,
                sessionId = TokenPreferencesRepository.readSessionId(context)
            ).sendWithResponse()
        }
    }

    suspend fun getRoomList(handleId: Long): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.LIST.request
                )
            ).sendWithResponse()
        }
    }

    suspend fun createRoom(handleId: Long, description: String?, room: Long?, secret: String?, pin: String?, isPrivate: Boolean): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.CREATE.request,
                    room = room,
                    permanent = false,
                    description = description,
                    secret = secret,
                    pin = pin,
                    isPrivate = isPrivate
                )
            ).sendWithResponse()
        }
    }

    suspend fun joinRoom(handleId: Long, roomId: Long, pin: String? = null): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.JOIN.request,
                    room = roomId,
                    pType = "publisher",
                    display = NicknamePreferencesRepository.readNickname(context),
                    pin = pin
                )
            ).sendWithResponse()
        }
    }

    suspend fun listForwarders(handleId: Long, room: Long, secret: String? = null): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.LIST_FORWARDERS.request,
                    room = room,
                    secret = secret
                )
            ).sendWithResponse()
        }
    }

    suspend fun kick(handleId: Long, room: Long, secret: String?, id: Long) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.KICK.request,
                    secret = secret,
                    room = room,
                    id = id
                )
            ).send()
        }
    }

    suspend fun getParticipants(handleId: Long, room: Long): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.LIST_PARTICIPANTS.request,
                    room = room
                )
            ).sendWithResponse()
        }
    }

    suspend fun moderate(
        handleId: Long,
        room: Long,
        secret: String?,
        id: Long,
        muteAudio: Boolean? = null,
        muteVideo: Boolean? = null,
        muteData: Boolean? = null
    ): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.MODERATE.request,
                    room = room,
                    secret = secret,
                    id = id,
                    muteAudio = muteAudio,
                    muteVideo = muteVideo,
                    muteData = muteData
                )
            )
        }.sendWithResponse()
    }

    suspend fun destroyRoom(handleId: Long, room: Long, secret: String?) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.DESTROY.request,
                    room = room,
                    secret = secret,
                    permanent = false
                )
            ).send()
        }
    }

    suspend fun attach(): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.ATTACH.janus,
                transaction = RandomUtil.randomTransaction(),
                plugin = Plugin.VIDEO_ROOM.plugin,
                sessionId = TokenPreferencesRepository.readSessionId(context)
            ).sendWithResponse()
        }
    }

    suspend fun subscriberJoinRoom(feedId: Long, handleId: Long, room: Long, pin: String?): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.JOIN.request,
                    room = room,
                    pType = "subscriber",
                    feed = feedId,
                    pin = pin
                )
            ).sendWithResponse()
        }
    }

    suspend fun publisherCreateOffer(handleId: Long, sessionDescription: SessionDescription): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.CONFIGURE.request,
                    audio = true,
                    video = true,
                ),
                jsep = SendModelJsep(
                    type = sessionDescription.type.canonicalForm(),
                    sdp = sessionDescription.description
                )
            ).sendWithResponse()
        }
    }

    suspend fun subscriberCreateAnswer(handleId: Long, room: Long, sessionDescription: SessionDescription) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.START.request,
                    room = room
                ),
                jsep = SendModelJsep(
                    type = sessionDescription.type.canonicalForm(),
                    sdp = sessionDescription.description
                )
            ).send()
        }
    }

    suspend fun registerUser(handleId: Long, userName: String): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.REGISTER.request,
                    username = userName,
                    device = "Android"
                )
            ).sendWithResponse()
        }
    }

    suspend fun subscriberOnLeaving(handleId: Long): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.DETACH.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId
            ).sendWithResponse()
        }
    }

    suspend fun publisherOnLeaving(handleId: Long): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.LEAVE.request
                )
            ).sendWithResponse()
        }
    }

    suspend fun getVideoCallUserList(handleId: Long): StateData<ResponseModel> {
        return (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.LIST.request
                )
            ).sendWithResponse()
        }
    }

    suspend fun call(handleId: Long, callee: String, sessionDescription: SessionDescription) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.CALL.request,
                    username = callee
                ),
                jsep = SendModelJsep(
                    type = sessionDescription.type.canonicalForm(),
                    sdp = sessionDescription.description
                )
            ).send()
        }
    }

    suspend fun accept(handleId: Long, sessionDescription: SessionDescription) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.ACCEPT.request,
                ),
                jsep = SendModelJsep(
                    type = sessionDescription.type.canonicalForm(),
                    sdp = sessionDescription.description
                )
            ).send()
        }
    }

    suspend fun trickleCandidate(handleId: Long, candidate: IceCandidate) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.TRICKLE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                candidate = SendModelCandidate(
                    candidate = candidate.sdp,
                    sdpMid = candidate.sdpMid,
                    sdpMLineIndex = candidate.sdpMLineIndex
                )
            ).send()
        }
    }

    suspend fun completeCandidate(handleId: Long) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.TRICKLE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                candidate = SendModelCandidate(
                    completed = true
                )
            ).send()
        }
    }

    suspend fun record(handleId: Long, record: Boolean, name: String) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.SET.request,
                    audio = true,
                    video = true,
                    bitrate = 12800000,
                    record = record,
                    filename = name
                )
            ).send()
        }
    }

    suspend fun hangup(handleId: Long) {
        (Dispatchers.IO) {
            SendModel(
                janus = Janus.MESSAGE.janus,
                transaction = RandomUtil.randomTransaction(),
                sessionId = TokenPreferencesRepository.readSessionId(context),
                handleId = handleId,
                body = SendModelBody(
                    request = Request.HANGUP.request
                )
            ).send()
        }
    }
}