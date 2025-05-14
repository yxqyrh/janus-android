package com.yxqyrh.janusandroid.room.dao

import com.yxqyrh.janusandroid.room.entity.VideoRoomSecret
import androidx.room.*

@Dao
interface VideoRoomSecretDao {
    @Query("SELECT * from video_room_secret where room = :room LIMIT 1")
    suspend fun getSecretByRoom(room: Long): VideoRoomSecret?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecret(videoRoomSecret: VideoRoomSecret)

    @Delete
    suspend fun deleteSecret(videoRoomSecret: VideoRoomSecret)
}