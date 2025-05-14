package com.yxqyrh.janusandroid.rtc.model

import org.webrtc.EglBase
import org.webrtc.MediaStream

data class StreamResource(
    val state: StreamState,
    val display: String,
    val feedId: Long?,
    val handleId: Long,
    val eglBaseContext: EglBase.Context?,
    val stream: MediaStream?,
    var audioModeration: Boolean = false,
    var videoModeration: Boolean = false
)

enum class StreamState {
    LOCAL,
    REMOTE,
    REMOVE_REMOTE
}
