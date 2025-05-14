package com.yxqyrh.janusandroid.ui.viewmodel

import com.yxqyrh.janusandroid.core.VideoRoomApplication
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {
    protected val mApplication by lazy { VideoRoomApplication.application }
    protected val scarletRepository by lazy { mApplication.scarletInstance.scarletRepository }

    protected val compositeDisposable by lazy { CompositeDisposable() }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}