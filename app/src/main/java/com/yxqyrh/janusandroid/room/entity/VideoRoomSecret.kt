package com.yxqyrh.janusandroid.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_room_secret")
data class VideoRoomSecret(
    @PrimaryKey
    val room: Long,

    @ColumnInfo(name = "secret")
    val secret: String?
)