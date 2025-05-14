package com.yxqyrh.janusandroid.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_room_pin")
data class VideoRoomPin(

    @PrimaryKey
    val room: Long,

    @ColumnInfo(name = "pin")
    val pin: String?
)