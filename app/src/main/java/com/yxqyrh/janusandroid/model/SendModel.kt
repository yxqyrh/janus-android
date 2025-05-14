package com.yxqyrh.janusandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SendModel(
    val janus: String? = null,
    val transaction: String? = null,
    val plugin: String? = null,
    @Json(name = "session_id") val sessionId: Long? = null,
    @Json(name = "handle_id") val handleId: Long? = null,
    val body: SendModelBody? = null,
    val candidate: SendModelCandidate? = null,
    val jsep: SendModelJsep? = null
)

@JsonClass(generateAdapter = true)
class SendModelBody(
    val request: String? = null,
    val audio: Boolean? = null,
    val video: Boolean? = null,
    val bitrate: Int? = null,
    val record: Boolean? = null,
    val filename: String? = null,
    val room: Long? = null,
    val id: Long? = null,
    @Json(name = "mute_audio") val muteAudio: Boolean? = null,
    @Json(name = "mute_video") val muteVideo: Boolean? = null,
    @Json(name = "mute_data") val muteData: Boolean? = null,
    val permanent: Boolean? = null,
    val description: String? = null,
    val secret: String? = null,
    val pin: String? = null,
    @Json(name = "is_private") val isPrivate: Boolean? = null,
    @Json(name = "ptype") val pType: String? = null,
    val display: String? = null,
    val token: String? = null,
    val username: String? = null,
    val device: String? = null,
    val feed: Long? = null
)

@JsonClass(generateAdapter = true)
data class SendModelCandidate(
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val completed: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class SendModelJsep(
    val type: String? = null,
    val sdp: String? = null
)
