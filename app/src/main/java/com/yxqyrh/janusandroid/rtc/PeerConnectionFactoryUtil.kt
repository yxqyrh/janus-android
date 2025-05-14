package com.yxqyrh.janusandroid.rtc

import com.yxqyrh.janusandroid.utils.Logger.log
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import org.webrtc.*

object PeerConnectionFactoryUtil {
    private const val LOCAL_MEDIA_ID = "ARDAMS"
    private const val VIDEO_TRACK_ID = "ARDAMSv0"
    private const val AUDIO_TRACK_ID = "ARDAMSa0"

    suspend fun createLocalMediaStream(
        context: Context,
        eglBaseContext: EglBase.Context?,
        factory: PeerConnectionFactory?,
        createVideoCapturerFun: suspend () -> VideoCapturer?
    ): MediaStream? {
        log("create local stream")
        factory?.apply {
            return createLocalMediaStream(LOCAL_MEDIA_ID).apply {
                addTrack(createAudioTrack(AUDIO_TRACK_ID, createAudioSource(MediaConstraints())))
                addTrack(getCameraVideoTrack(context, eglBaseContext, factory, createVideoCapturerFun))
            }
        }
        return null
    }

    private suspend fun getCameraVideoTrack(
        context: Context,
        eglBaseContext: EglBase.Context?,
        factory: PeerConnectionFactory?,
        createVideoCapturerFun: suspend () -> VideoCapturer?
    ): VideoTrack? {
        factory?.apply {
            return (Dispatchers.Main) {
                val videoSource = createVideoSource(true)

                createVideoCapturerFun()?.apply {
                    initialize(SurfaceTextureHelper.create("CaptureThread", eglBaseContext), context, videoSource.capturerObserver)
                    startCapture(1920, 1080, 60)
                }

                createVideoTrack(VIDEO_TRACK_ID, videoSource)
            }
        }
        return null
    }
}