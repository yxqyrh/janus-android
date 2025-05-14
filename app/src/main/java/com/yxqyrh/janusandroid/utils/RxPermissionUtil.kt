package com.yxqyrh.janusandroid.utils

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions


object RxPermissionUtil {
    val videoPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    fun AppCompatActivity.requestRxPermission(permissions: Array<String>, onSuccess: () -> Unit) = this.requestRxPermission(permissions, onSuccess, {})

    fun AppCompatActivity.requestRxPermission(permissions: Array<String>, onSuccess: () -> Unit, onFail: () -> Unit) {
        RxPermissions(this)
            .request(*permissions)
            .subscribe { granted ->
                if (granted) {
                    onSuccess()
                } else {
                    onFail()
                }
            }
    }
}