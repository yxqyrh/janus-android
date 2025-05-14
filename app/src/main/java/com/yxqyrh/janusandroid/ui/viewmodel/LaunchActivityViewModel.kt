package com.yxqyrh.janusandroid.ui.viewmodel

import androidx.lifecycle.toLiveData

class LaunchActivityViewModel : BaseViewModel() {
    val createSessionLiveData
        get() = scarletRepository.createSessionProcessor.toLiveData()


}