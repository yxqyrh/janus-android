package com.yxqyrh.janusandroid.ui.viewmodel

import com.yxqyrh.janusandroid.core.ResponseMutableLiveData
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.model.enumModel.Plugin
import com.yxqyrh.janusandroid.room.database.AppDatabaseUtil
import com.yxqyrh.janusandroid.room.entity.VideoRoomPin
import com.yxqyrh.janusandroid.room.entity.VideoRoomSecret
import com.yxqyrh.janusandroid.utils.Logger.log
import com.yxqyrh.janusandroid.utils.MoshiUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import java.util.concurrent.atomic.AtomicLong

class VideoRoomSelectViewModel : BaseViewModel() {
    val handleIdRef = AtomicLong()

    val createHandleLiveData = ResponseMutableLiveData()
    val getRoomListLiveData = ResponseMutableLiveData()
    val createRoomLiveData = ResponseMutableLiveData()
    val joinRoomLiveData = ResponseMutableLiveData()

    suspend fun createHandle() {
        (Dispatchers.IO) {
            createHandleLiveData.postValue(scarletRepository.createHandle(Plugin.VIDEO_ROOM))
        }
    }

    suspend fun getRoomList() {
        (Dispatchers.IO) {
            getRoomListLiveData.postValue(scarletRepository.getRoomList(handleIdRef.get()))
        }
    }

    suspend fun createRoom(description: String?, room: Long?, secret: String?, pin: String?, isPrivate: Boolean) {
        (Dispatchers.IO) {
            scarletRepository.createRoom(handleIdRef.get(), description, room, secret, pin, isPrivate).let {
                if (it.status == Status.SUCCESS) {
                    it.data?.pluginData?.data?.room?.let { room ->
                        insertPin(room, pin)
                        insertSecret(room, secret)
                    }
                }
                createRoomLiveData.postValue(it)
            }
        }
    }

    suspend fun joinRoom(roomId: Long, pin: String?) {
        (Dispatchers.IO) {
            scarletRepository.joinRoom(handleIdRef.get(), roomId, pin).let {
                when (it.status) {
                    Status.SUCCESS -> {
                        insertPin(roomId, pin)
                    }
                    Status.ERROR -> {
                        deletePin(roomId, pin)
                    }
                    else -> {
                    }
                }
                joinRoomLiveData.postValue(it)
            }
        }
    }

    suspend fun getPin(room: Long): VideoRoomPin? {
        return (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomPinDao.getPinByRoom(room)?.also {
                log(MoshiUtil.toJson(it))
            }
        }
    }

    private suspend fun deletePin(room: Long, pin: String?) {
        (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomPinDao.deletePin(VideoRoomPin(room, pin))
        }
    }

    private suspend fun insertPin(room: Long, pin: String?) {
        (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomPinDao.insertPin(VideoRoomPin(room, pin))
        }
    }

    private suspend fun insertSecret(room: Long, secret: String?) {
        (Dispatchers.IO) {
            AppDatabaseUtil.videoRoomDatabase.videoRoomSecretDao.insertSecret(VideoRoomSecret(room, secret))
        }
    }
}