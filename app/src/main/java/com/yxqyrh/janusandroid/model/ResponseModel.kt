package com.yxqyrh.janusandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseModel(
    val janus: String? = null,
    val transaction: String? = null,
    @Json(name = "session_id") val sessionId: Long? = null,
    val data: ResponseModelId? = null,
    val sender: Long? = null,
    @Json(name = "plugindata") val pluginData: ResponseModelPlugin? = null,
    val jsep: ResponseModelJsep? = null,
    val error: ResponseModelError? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelError(
    val code: Int? = null,
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelId(
    val id: Long? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelPlugin(
    val plugin: String? = null,
    val data: ResponseModelPluginData? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelPluginData(
    @Json(name = "error_code") val errorCode: Int? = null,
    val error: String? = null,
    @Json(name = "videoroom") val videoRoom: String? = null,
    @Json(name = "videocall") val videoCall: String? = null,
    val list: List<ResponseModelListData>? = null,
    val result: ResponseModelResult? = null,
    val room: Long? = null,
    val description: String? = null,
    val id: Long? = null,
    @Json(name = "private_id") val privateId: String? = null,
    val publishers: List<ResponseModelPublisher>? = null,
    val participants: List<ResponseModelPublisher>? = null,
    @Json(name = "audio-moderation") var audioModeration: String? = null,
    @Json(name = "video-moderation") var videoModeration: String? = null,
    @Json(name = "data-moderation") var dataModeration: String? = null,
    val leaving: String? = null,
    val kicked: Long? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelListData(
    val room: Long? = null,
    val description: String? = null,
    @Json(name = "pin_required") val pinRequired: Boolean? = null,
    @Json(name = "max_publishers") val maxPublishers: Int? = null,
    val bitrate: Long? = null,
    @Json(name = "fir_freq") val firFreq: Int? = null,
    @Json(name = "require_pvtid") val requirePvtId: Boolean? = null,
    @Json(name = "require_e2ee") val requireE2ee: Boolean? = null,
    @Json(name = "notify_joining") val notifyJoining: Boolean? = null,
    @Json(name = "audiocodec") val audioCodec: String? = null,
    @Json(name = "videocodec") val videoCodec: String? = null,
    @Json(name = "video_svc") val videoSvc: Boolean? = null,
    val record: Boolean? = null,
    @Json(name = "lock_record") val lockRecord: Boolean? = null,
    @Json(name = "num_participants") val numParticipants: Int? = null,
    @Json(name = "audiolevel_ext") val audioLevelExt: Boolean? = null,
    @Json(name = "audiolevel_event") val audioLevelEvent: Boolean? = null,
    @Json(name = "videoorient_ext") val videoOrientExt: Boolean? = null,
    @Json(name = "playoutdelay_ext") val playOutDelayExt: Boolean? = null,
    @Json(name = "transport_wide_cc_ext") val transportWideCcExt: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class ResponseModelResult(
    val event: String? = null,
    val username: String? = null,
    val cid: String? = null,
    val reason: String? = null,
    val list: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelPublisher(
    val id: Long? = null,
    val display: String? = null,
    @Json(name = "audio_codec") val audioCodec: String? = null,
    @Json(name = "video_codec") val videoCodec: String? = null,
    @Json(name = "audio_moderated") var audioModerated: Boolean? = null,
    @Json(name = "video_moderated") var videoModerated: Boolean? = null,
    @Json(name = "data_moderated") var dataModerated: Boolean? = null,
    val publisher: Boolean? = null,
    val talking: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class ResponseModelJsep(
    val type: String? = null,
    val sdp: String? = null
)
