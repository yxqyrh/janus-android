package com.yxqyrh.janusandroid.room.database

import com.yxqyrh.janusandroid.core.VideoRoomApplication
import androidx.room.Room

object AppDatabaseUtil {
    val videoRoomDatabase by lazy {
        Room.databaseBuilder(VideoRoomApplication.application, VideoRoomDatabase::class.java, "app_database").build()
    }
}