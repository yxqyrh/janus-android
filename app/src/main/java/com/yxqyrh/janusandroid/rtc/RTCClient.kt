package com.yxqyrh.janusandroid.rtc

import com.yxqyrh.janusandroid.core.VideoRoomApplication
import com.yxqyrh.janusandroid.rtc.model.*
import com.yxqyrh.janusandroid.utils.Logger.log
import android.content.Context
import android.media.AudioManager
import android.util.Log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import org.webrtc.*
import org.webrtc.MediaConstraints.KeyValuePair
import org.webrtc.PeerConnection.IceGatheringState
import java.math.BigInteger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class RTCClient(
    private val capturerConfig: VideoCapturerUtil.CapturerConfig = VideoCapturerUtil.CapturerConfig()
) {
    private data class PeerConnectionResource(
        val peerConnection: PeerConnection?,
        val type: PeerConnectionResourceType
    )

    enum class PeerConnectionResourceType {
        PUBLISHER,
        SUBSCRIBER
    }

    private val context: Context by lazy { VideoRoomApplication.application }

    private val compositeDisposable = CompositeDisposable()

    val candidateProcessor = PublishProcessor.create<CandidateResource>()
    val streamProcessor = PublishProcessor.create<StreamResource>()
    val sessionDescriptionProcessor = PublishProcessor.create<SessionDescriptionResource>()
    val statsProcessor = PublishProcessor.create<StatsResource>()

    private val peerConnectionMap = ConcurrentHashMap<Long, PeerConnectionResource>()
    private var eglBaseContext: EglBase.Context? = null
    private var factory: PeerConnectionFactory? = null
    private var localstream: MediaStream? = null
    private var videoCapturer: VideoCapturer? = null

    suspend fun initClient() {
        (Dispatchers.IO) {
//            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true)
//            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true)
//            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true)
//            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true)

            eglBaseContext = EglBase.create().eglBaseContext
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions
                    .builder(context)
                    .setEnableInternalTracer(true)
                    .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                    .createInitializationOptions()
            )
            factory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
                .createPeerConnectionFactory()
        }
    }

    suspend fun switchAudio() {
        (Dispatchers.IO) {
            localstream?.audioTracks?.get(0)?.apply {
                setEnabled(!enabled())
            }
        }
    }

    suspend fun switchVideo() {
        return (Dispatchers.IO) {
            localstream?.videoTracks?.get(0)?.apply {
                setEnabled(!enabled())
            }
        }
    }

    suspend fun createOffer(handleId: Long) {
        (Dispatchers.IO) {
            log("createOffer")
            peerConnectionMap[handleId]?.peerConnection?.createOffer(object : CustomPeerSdpObserver() {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    log("create offer success")
                    peerConnectionMap[handleId]?.peerConnection?.setLocalDescription(object : CustomPeerSdpObserver() {
                        override fun onSetSuccess() {
                            log("set offer success")
                            sessionDescriptionProcessor.onNext(SessionDescriptionResource(handleId, SessionDescriptionState.OFFER, sessionDescription))
                        }
                    }, sessionDescription)
                }
            }, createDefaultConstraints())
        }
    }

    suspend fun createAnswer(handleId: Long, typeString: String, sdpString: String) {
        (Dispatchers.IO) {
            kotlin.runCatching {
                SessionDescription(SessionDescription.Type.fromCanonicalForm(typeString), sdpString)
            }.getOrNull().let { sdp ->
                log("createAnswer")
                peerConnectionMap[handleId]?.peerConnection?.apply {
                    setRemoteDescription(CustomPeerSdpObserver(), sdp)
                    createAnswer(object : CustomPeerSdpObserver() {
                        override fun onCreateSuccess(sessionDescription: SessionDescription) {
                            super.onCreateSuccess(sessionDescription)
                            setLocalDescription(CustomPeerSdpObserver(), sessionDescription)
                            sessionDescriptionProcessor.onNext(SessionDescriptionResource(handleId, SessionDescriptionState.ANSWER, sessionDescription))
                        }
                    }, createDefaultConstraints())
                }
            }
        }
    }

    suspend fun setRemoteDescription(handleId: Long, type: String, sdp: String) {
        (Dispatchers.IO) {
            kotlin.runCatching {
                SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
            }.getOrNull().let {
                peerConnectionMap[handleId]?.peerConnection?.setRemoteDescription(CustomPeerSdpObserver(), it)
            }
        }
    }

    suspend fun detach(handleId: Long) {
        (Dispatchers.IO) {
            peerConnectionMap[handleId]?.peerConnection?.close()
            peerConnectionMap.remove(handleId)
            streamProcessor.onNext(StreamResource(StreamState.REMOVE_REMOTE, "", null, handleId, eglBaseContext, null))
        }
    }

    private fun createDefaultConstraints(): MediaConstraints =
        MediaConstraints().apply {
            optional.add(KeyValuePair("DtlsSrtpKeyAgreement", "true"))
            mandatory.add(KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(KeyValuePair("googEchoCancellation", "true"))
        }

    suspend fun createMediaPeerConnection(display: String, handleId: Long, type: PeerConnectionResourceType): PeerConnection? {
        return (Dispatchers.IO) {
            log("create media PeerConnection")
            val peerConnection = createPeerConnection(display, null, handleId, type)

            localstream = PeerConnectionFactoryUtil.createLocalMediaStream(context, eglBaseContext, factory) {
                (Dispatchers.Main) {
                    kotlin.runCatching { videoCapturer?.stopCapture() }
                    videoCapturer = VideoCapturerUtil.createVideoCapturer(context, capturerConfig)
                    videoCapturer
                }
            }
            log("process local stream")
            streamProcessor.onNext(StreamResource(StreamState.LOCAL, display, null, handleId, eglBaseContext, localstream))
            peerConnection?.apply {
                log("add local stream")
                addStream(localstream)
//                startStatsInterval()
            }
            peerConnection
        }
    }

    suspend fun createPeerConnection(display: String, feedId: Long?, handleId: Long, type: PeerConnectionResourceType): PeerConnection? {
        return (Dispatchers.IO) {
            log("create peer connection")
            val iceServers = LinkedList<PeerConnection.IceServer>().apply {
                add(PeerConnection.IceServer("stun:139.224.43.144:3478"))
            }
            val peerConnection = factory?.createPeerConnection(
                PeerConnection.RTCConfiguration(LinkedList()).apply {
//                    enableCpuOveruseDetection = false
//                    candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.LOW_COST
                },
                MediaConstraints().apply { optional.add(KeyValuePair("DtlsSrtpKeyAgreement", "true")) },
                CustomPeerConnectionObserver(display, feedId, handleId)
            )
            peerConnectionMap[handleId] = PeerConnectionResource(peerConnection, type)
            peerConnection
        }
    }

    suspend fun switchCamera() {
        (Dispatchers.Main) {
            if (videoCapturer != null && videoCapturer is CameraVideoCapturer) {
                (videoCapturer as CameraVideoCapturer).switchCamera(null)
            }
        }
    }

    private fun PeerConnection.startStatsInterval() {
        var localBytesBefore = 0L
        var remoteBytesBefore = 0L
        var localTimestampUsBefore = 0L
        var remoteTimestampUsBefore = 0L
        compositeDisposable.add(
            Observable
                .interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .subscribe {
                    getStats { rtcStatsReport ->
                        var localFps = 0.0
                        var remoteFps = 0.0

                        var localBytesNow = 0L
                        var remoteBytesNow = 0L
                        var localTimestampUsNow = 0L
                        var remoteTimestampUsNow = 0L
                        kotlin.runCatching {
                            for (statsEntry in rtcStatsReport.statsMap) {
                                if (statsEntry.key.contains("RTCOutboundRTPVideoStream")) {
                                    (statsEntry.value.members["bytesSent"] as BigInteger?)?.let { localBytesNow = it.toLong() }
                                    localTimestampUsNow = statsEntry.value.timestampUs.toLong()
                                    (statsEntry.value.members["framesPerSecond"] as Double?)?.let { localFps = it }
                                } else if (statsEntry.key.contains("RTCInboundRTPVideoStream")) {
                                    (statsEntry.value.members["bytesReceived"] as BigInteger?)?.let { remoteBytesNow = it.toLong() }
                                    remoteTimestampUsNow = statsEntry.value.timestampUs.toLong()
                                    (statsEntry.value.members["framesPerSecond"] as Double?)?.let { remoteFps = it }
                                }
                            }
                        }.onFailure {
                            log("$it")
                        }

                        log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", Log.INFO)
                        var localRate = 0L
                        var remoteRate = 0L

                        if (localBytesBefore == 0L) localBytesBefore = localBytesNow
                        if (remoteBytesBefore == 0L) remoteBytesBefore = remoteBytesNow
                        if (localTimestampUsBefore == 0L) localTimestampUsBefore = localTimestampUsNow
                        if (remoteTimestampUsBefore == 0L) remoteTimestampUsBefore = remoteTimestampUsNow

                        val localTimePassed = localTimestampUsNow - localTimestampUsBefore
                        if (localTimePassed > 0L) localRate = ((localBytesNow - localBytesBefore) * 1000) / localTimePassed

                        log("localTimePassed: $localTimePassed", Log.INFO)
                        log("localBytesNow: $localBytesNow", Log.INFO)
                        log("localBytesBefore: $localBytesBefore", Log.INFO)

                        val remoteTimePassed = remoteTimestampUsNow - remoteTimestampUsBefore
                        if (remoteTimePassed > 0L) remoteRate = ((remoteBytesNow - remoteBytesBefore) * 1000) / remoteTimePassed

                        log("remoteTimePassed: $remoteTimePassed", Log.INFO)
                        log("remoteBytesNow: $remoteBytesNow", Log.INFO)
                        log("remoteBytesBefore: $remoteBytesBefore", Log.INFO)

                        log("localRate: $localRate", Log.INFO)
                        log("remoteRate: $remoteRate", Log.INFO)
                        log("localFps: $localFps", Log.INFO)
                        log("remoteFps: $remoteFps", Log.INFO)

                        localBytesBefore = localBytesNow
                        remoteBytesBefore = remoteBytesNow
                        localTimestampUsBefore = localTimestampUsNow
                        remoteTimestampUsBefore = remoteTimestampUsNow

                        statsProcessor.onNext(
                            StatsResource(
                                localFps = localFps,
                                localRate = localRate.toInt(),
                                remoteFps = remoteFps,
                                remoteRate = remoteRate.toInt()
                            )
                        )
                    }
                }
        )
    }

    suspend fun abort() {
        (Dispatchers.Main) {
            videoCapturer?.runCatching { stopCapture() }
            for (peerConnectionEntry in peerConnectionMap) {
                peerConnectionEntry.value.peerConnection?.close()
            }
            (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).isSpeakerphoneOn = false
            eglBaseContext = null
            videoCapturer = null
            localstream = null
            peerConnectionMap.clear()
            factory = null
            compositeDisposable.dispose()
        }
    }

    private inner class CustomPeerConnectionObserver(
        private val display: String,
        private val feedId: Long?,
        private val handleId: Long
    ) : PeerConnection.Observer {
        override fun onSignalingChange(signalingState: PeerConnection.SignalingState?) {}

        override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {}

        override fun onIceConnectionReceivingChange(b: Boolean) {}

        override fun onIceGatheringChange(state: IceGatheringState?) {
            when (state) {
                IceGatheringState.NEW -> {
                }
                IceGatheringState.GATHERING -> {
                }
                IceGatheringState.COMPLETE -> {
                    candidateProcessor.onNext(CandidateResource(handleId, CandidateState.COMPLETE))
                }
                null -> {}
            }
        }

        override fun onIceCandidate(candidate: IceCandidate) {
            candidateProcessor.onNext(CandidateResource(handleId, CandidateState.TRICKLE, candidate))
        }

        override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>?) {}

        override fun onAddStream(stream: MediaStream?) {
            (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).isSpeakerphoneOn = true
            streamProcessor.onNext(StreamResource(StreamState.REMOTE, display, feedId, handleId, eglBaseContext, stream))
        }

        override fun onRemoveStream(stream: MediaStream?) {
        }

        override fun onDataChannel(dataChannel: DataChannel?) {}

        override fun onRenegotiationNeeded() {}

        override fun onAddTrack(rtpReceiver: RtpReceiver?, mediaStreams: Array<MediaStream>?) {}
    }
}