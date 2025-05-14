package com.yxqyrh.janusandroid.ui.viewmodel

import com.yxqyrh.janusandroid.core.ResponseMutableLiveData
import com.yxqyrh.janusandroid.core.StateData
import com.yxqyrh.janusandroid.core.StateMutableLiveData
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.datastore.NicknamePreferencesRepository
import com.yxqyrh.janusandroid.model.ResponseModelPublisher
import com.yxqyrh.janusandroid.room.database.AppDatabaseUtil
import com.yxqyrh.janusandroid.room.entity.VideoRoomPin
import com.yxqyrh.janusandroid.room.entity.VideoRoomSecret
import com.yxqyrh.janusandroid.rtc.repository.VideoRoomRepository
import com.yxqyrh.janusandroid.utils.Logger.log
import com.yxqyrh.janusandroid.utils.MoshiUtil
import androidx.lifecycle.viewModelScope
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class VideoRoomViewModel : BaseViewModel() {
    val publisherHandleIdRef = AtomicLong()
    val roomIdRef = AtomicLong()

    //    val pinRef = AtomicReference<String>()
    val publishersRef = AtomicReference<String>()

    private val secretRef = AtomicReference<String>()

    val secretLiveData = ResponseMutableLiveData()
    val participantsLiveData = StateMutableLiveData<List<ResponseModelPublisher>>()
    val moderationLiveData = ResponseMutableLiveData()

    val hangupLiveData = ResponseMutableLiveData()

    suspend fun joinRoom() {
        (Dispatchers.IO) {
            getPin()?.pin.let { pin ->
                publishersRef.get()?.let {
                    MoshiUtil.fromJson<List<ResponseModelPublisher>>(it, MoshiUtil.generateListType(ResponseModelPublisher::class.java))?.let { publishers ->
                        rtcRepository.joinRoom(publisherHandleIdRef.get(), roomIdRef.get(), pin, publishers)
                    }
                } ?: let {
                    scarletRepository.joinRoom(publisherHandleIdRef.get(), roomIdRef.get(), getPin()?.pin).let {
                        when (it.status) {
                            Status.SUCCESS -> {
                                it.data?.pluginData?.data?.publishers?.let { publishers ->
                                    rtcRepository.joinRoom(publisherHandleIdRef.get(), roomIdRef.get(), pin, publishers)
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun listForwarders(secret: String?) {
        (Dispatchers.IO) {
            scarletRepository.listForwarders(publisherHandleIdRef.get(), roomIdRef.get(), secret).let {
                when (it.status) {
                    Status.SUCCESS -> {
                        secretRef.set(secret)
                        insertSecret(secret)
                    }
                    Status.ERROR -> {
                        deleteSecret(secret)
                    }
                    else -> {
                    }
                }
                secretLiveData.postValue(it)
            }
        }
    }

    suspend fun moderate(
        id: Long,
        muteAudio: Boolean? = null,
        muteVideo: Boolean? = null,
        muteData: Boolean? = null
    ) {
        (Dispatchers.IO) {
            scarletRepository.moderate(publisherHandleIdRef.get(), roomIdRef.get(), secretRef.get(), id, muteAudio, muteVideo, muteData).let {

            }
        }
    }

    suspend fun kick(id: Long) {
        (Dispatchers.IO) {
            scarletRepository.kick(publisherHandleIdRef.get(), roomIdRef.get(), secretRef.get(), id)
        }
    }

    suspend fun getParticipants() {
        (Dispatchers.IO) {
            scarletRepository.getParticipants(publisherHandleIdRef.get(), roomIdRef.get()).let {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.pluginData?.data?.participants?.let { participants ->
                            val participantList = ArrayList<ResponseModelPublisher>().apply {
                                addAll(participants)
                            }
                            var publisherParticipant: ResponseModelPublisher? = null
                            var publisherIndex: Int? = null
                            for ((index, participant) in participantList.withIndex()) {
                                if (participant.display == NicknamePreferencesRepository.readNickname(mApplication)) {
                                    publisherParticipant = participant
                                    publisherIndex = index
                                    break
                                }
                            }
                            publisherParticipant?.let {
                                publisherIndex?.let {
                                    participantList.removeAt(publisherIndex)
                                    participantList.add(0, publisherParticipant)
                                }
                            }
                            participantsLiveData.postValue(StateData(Status.SUCCESS, participantList))
                        }
                    }
                    Status.ERROR -> {
                        participantsLiveData.postValue(StateData(Status.ERROR, null, it.transaction, it.message))
                    }
                    else -> {
                    }
                }
            }
        }
    }

    suspend fun destroyRoom() {
        (Dispatchers.IO) {
            scarletRepository.destroyRoom(publisherHandleIdRef.get(), roomIdRef.get(), secretRef.get())
        }
    }

    suspend fun getSecret(): VideoRoomSecret? {
        return (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomSecretDao.getSecretByRoom(roomIdRef.get())?.also {
                log(MoshiUtil.toJson(it))
            }
        }
    }

    private suspend fun insertSecret(secret: String?) {
        (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomSecretDao.insertSecret(VideoRoomSecret(roomIdRef.get(), secret))
        }
    }

    private suspend fun deleteSecret(secret: String?) {
        (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomSecretDao.deleteSecret(VideoRoomSecret(roomIdRef.get(), secret))
        }
    }

    private suspend fun getPin(): VideoRoomPin? {
        return (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomPinDao.getPinByRoom(roomIdRef.get())?.also {
                log(MoshiUtil.toJson(it))
            }
        }
    }

    /***
     * RTCRepository
     */
    private val rtcRepository = VideoRoomRepository()

    val streamProcessor get() = rtcRepository.streamProcessor

    suspend fun initClient() = rtcRepository.initClient()

    suspend fun switchAudio() = rtcRepository.switchAudio()
    suspend fun switchVideo() = rtcRepository.switchVideo()

    suspend fun abortClient() = rtcRepository.abortClient()
    suspend fun switchCamera() = rtcRepository.switchCamera()

    suspend fun hangup() {
        (Dispatchers.IO) {
            scarletRepository.publisherOnLeaving(publisherHandleIdRef.get()).let {
                hangupLiveData.postValue(it)
            }
        }
    }

    init {
        compositeDisposable.add(
            scarletRepository
                .publishersProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    it.data?.pluginData?.data?.publishers?.let { publishers ->
                        viewModelScope.launch(Dispatchers.IO) {
                            rtcRepository.attach(publishers)
                        }
                    }
                }
        )

        compositeDisposable.add(
            scarletRepository
                .moderationProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    moderationLiveData.postValue(it)
                }
        )

        compositeDisposable.add(
            scarletRepository
                .hangupProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    when (it.status) {
                        Status.SUCCESS -> {
                            it.data?.sender?.let { sender ->
                                viewModelScope.launch(Dispatchers.IO) {
                                    if (sender == publisherHandleIdRef.get()) {
                                        hangupLiveData.postValue(it)
                                    } else {
                                        scarletRepository.subscriberOnLeaving(sender).let { leavingResponse ->
                                            when (leavingResponse.status) {
                                                Status.SUCCESS -> {
                                                    rtcRepository.leaving(sender)
                                                }
                                                else -> {
                                                }
                                            }
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