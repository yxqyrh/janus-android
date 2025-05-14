package com.yxqyrh.janusandroid.rtc

import com.yxqyrh.janusandroid.utils.Logger.log
import android.content.Context
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.VideoCapturer

object VideoCapturerUtil {
    data class CapturerConfig(
        val bIsFrontCamera: Boolean = true,
        val captureToTexture: Boolean = true,
    )

    fun createVideoCapturer(context: Context, capturerConfig: CapturerConfig): VideoCapturer? {
        val cameraEnumerator = getCameraEnumerator(context, capturerConfig)

        // First, try to find front facing camera
        log("Looking for front facing cameras.")
        for (deviceName in cameraEnumerator.deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                log("Creating front facing camera capturer.")
                val videoCapturer = cameraEnumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) return videoCapturer
            }
        }

        // Front facing camera not found, try something else
        log("Looking for other cameras.")
        for (deviceName in cameraEnumerator.deviceNames) {
            if (!cameraEnumerator.isFrontFacing(deviceName)) {
                log("Creating other camera capturer.")
                val videoCapturer = cameraEnumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) return videoCapturer
            }
        }

        return null
    }

    private fun getCameraEnumerator(context: Context, capturerConfig: CapturerConfig): CameraEnumerator =
        if (Camera2Enumerator.isSupported(context)) {
            log("Creating capturer using camera2 API.")
            Camera2Enumerator(context)
        } else {
            log("Creating capturer using camera1 API.")
            Camera1Enumerator(capturerConfig.captureToTexture)
        }
}