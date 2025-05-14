package com.yxqyrh.janusandroid.rtc.model

import org.webrtc.SessionDescription

data class SessionDescriptionResource(
    val handleId: Long,
    val state: SessionDescriptionState,
    val sessionDescription: SessionDescription
)

enum class SessionDescriptionState {
    OFFER,
    ANSWER
}
