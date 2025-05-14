package com.yxqyrh.janusandroid.core

import com.yxqyrh.janusandroid.scarlet.ScarletModule
import androidx.multidex.MultiDexApplication

class VideoRoomApplication : MultiDexApplication() {

    companion object {
        lateinit var application: VideoRoomApplication
            private set
    }

    val scarletInstance: ScarletModule by lazy { ScarletModule() }

    override fun onCreate() {
        super.onCreate()
        application = this
    }
}