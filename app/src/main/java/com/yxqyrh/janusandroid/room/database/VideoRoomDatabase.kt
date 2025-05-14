package com.yxqyrh.janusandroid.room.database

import com.yxqyrh.janusandroid.room.dao.VideoRoomPinDao
import com.yxqyrh.janusandroid.room.dao.VideoRoomSecretDao
import com.yxqyrh.janusandroid.room.entity.VideoRoomPin
import com.yxqyrh.janusandroid.room.entity.VideoRoomSecret
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [VideoRoomSecret::class, VideoRoomPin::class], version = 1, exportSchema = false)
abstract class VideoRoomDatabase : RoomDatabase() {
    abstract val videoRoomSecretDao: VideoRoomSecretDao

    abstract val videoRoomPinDao: VideoRoomPinDao
}