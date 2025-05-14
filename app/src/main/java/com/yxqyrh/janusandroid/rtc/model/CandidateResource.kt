package com.yxqyrh.janusandroid.rtc.model

import org.webrtc.IceCandidate

data class CandidateResource(
    val handleId: Long,
    val state: CandidateState,
    val candidate: IceCandidate? = null
)

enum class CandidateState {
    COMPLETE,
    TRICKLE
}
