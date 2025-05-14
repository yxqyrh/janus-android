package com.yxqyrh.janusandroid.utils

import com.yxqyrh.janusandroid.BuildConfig
import android.util.Log

object Logger {
    private fun logSwitch(): Boolean = BuildConfig.DEBUG

    fun Any.log(text: String, priority: Int = Log.ASSERT) {
        if (logSwitch()) Log.println(priority, this.javaClass.simpleName, text)
    }

    fun Any.log(tag: String, text: String, priority: Int = Log.ASSERT) {
        if (logSwitch()) Log.println(priority, tag, text)
    }
}