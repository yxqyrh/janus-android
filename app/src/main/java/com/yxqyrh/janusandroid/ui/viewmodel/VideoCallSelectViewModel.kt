package com.yxqyrh.janusandroid.ui.viewmodel

import androidx.lifecycle.toLiveData
import com.yxqyrh.janusandroid.core.ResponseMutableLiveData
import com.yxqyrh.janusandroid.model.enumModel.Plugin
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class VideoCallSelectViewModel : BaseViewModel() {
    val userNameRef = AtomicReference<String>()
    val handleIdRef = AtomicLong()

    val createHandleLiveData = ResponseMutableLiveData()
    val registerUserLiveData = ResponseMutableLiveData()
    val videoCallUserListLiveData = ResponseMutableLiveData()

    val incomingCallLiveData get() = scarletRepository.incomingCallProcessor.toLiveData()

    suspend fun createHandle() {
        createHandleLiveData.postValue(scarletRepository.createHandle(Plugin.VIDEO_CALL))
    }

    suspend fun registerUser(userName: String) {
        userNameRef.set(userName)
        registerUserLiveData.postValue(scarletRepository.registerUser(handleIdRef.get(), userName))
    }

    suspend fun getVideoCallUserList() {
        videoCallUserListLiveData.postValue(scarletRepository.getVideoCallUserList(handleIdRef.get()))
    }
}