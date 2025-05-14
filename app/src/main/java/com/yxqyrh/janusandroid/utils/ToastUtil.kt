package com.yxqyrh.janusandroid.utils

import com.yxqyrh.janusandroid.core.VideoRoomApplication
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ToastUtil {
    fun Any.showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(VideoRoomApplication.application, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun Any.showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}