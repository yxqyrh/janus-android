package com.yxqyrh.janusandroid.core

import com.yxqyrh.janusandroid.model.ResponseModel
import androidx.lifecycle.MutableLiveData

open class StateMutableLiveData<T> : MutableLiveData<StateData<T>>()
class ResponseMutableLiveData : StateMutableLiveData<ResponseModel>()

data class StateData<out T>(
    val status: Status,
    val data: T?,
    val transaction: String? = null,
    val message: String? = null
)

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    COMPLETE
}
