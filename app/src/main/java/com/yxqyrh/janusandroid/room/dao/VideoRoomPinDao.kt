package com.yxqyrh.janusandroid.room.dao

import com.yxqyrh.janusandroid.room.entity.VideoRoomPin
import androidx.room.*

@Dao
interface VideoRoomPinDao {

    @Query("SELECT * from video_room_pin where room = :room LIMIT 1")
    suspend fun getPinByRoom(room: Long): VideoRoomPin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(videoRoomPin: VideoRoomPin)

    @Delete
    suspend fun deletePin(videoRoomPin: VideoRoomPin)
}