package com.yxqyrh.janusandroid.rtc.model

data class StatsResource(
    val localFps: Double = 0.0,
    val remoteFps: Double = 0.0,
    val localRate: Int = 0,
    val remoteRate: Int = 0
)
