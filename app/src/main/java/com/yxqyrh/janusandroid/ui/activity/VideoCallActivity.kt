package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.databinding.ActivityVideoCallBinding
import com.yxqyrh.janusandroid.rtc.model.StreamState
import com.yxqyrh.janusandroid.ui.viewmodel.VideoCallViewModel
import com.yxqyrh.janusandroid.utils.Logger.log
import com.yxqyrh.janusandroid.utils.RxPermissionUtil
import com.yxqyrh.janusandroid.utils.RxPermissionUtil.requestRxPermission
import android.content.Intent
import android.util.DisplayMetrics
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoCallActivity : BaseVMActivity<ActivityVideoCallBinding, VideoCallViewModel>() {
    companion object {
        private const val HANDLE_ID = "handle_id"
        private const val CALLEE = "callee"
        private const val TYPE = "type"
        private const val SDP = "sdp"

        fun AppCompatActivity.startVideoCallActivity(handleId: Long, callee: String) {
            requestRxPermission(RxPermissionUtil.videoPermissions) {
                startActivity(Intent(this, VideoCallActivity::class.java).apply {
                    putExtra(HANDLE_ID, handleId)
                    putExtra(CALLEE, callee)
                })
            }
        }

        fun AppCompatActivity.startVideoCallActivity(handleId: Long, typeString: String, sdpString: String) {
            requestRxPermission(RxPermissionUtil.videoPermissions) {
                startActivity(Intent(this, VideoCallActivity::class.java).apply {
                    putExtra(HANDLE_ID, handleId)
                    putExtra(TYPE, typeString)
                    putExtra(SDP, sdpString)
                })
            }
        }
    }

    override fun getViewModelClass(): Class<VideoCallViewModel> = VideoCallViewModel::class.java
    override fun getActivityBinding(): ActivityVideoCallBinding = ActivityVideoCallBinding.inflate(layoutInflater)

    override fun initActivity() {
        mViewModel.handleIdRef.set(intent.getLongExtra(HANDLE_ID, 0))

        binding.buttonHangup.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.hangup()
            }
        }

        binding.buttonSwitchCamera.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.switchCamera()
            }
        }

        compositeDisposable.add(
            mViewModel
                .streamProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    when (it.state) {
                        StreamState.LOCAL -> {
                            log("video call on local")
                            it.eglBaseContext?.let { eglContext ->
                                it.stream?.let { stream ->
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        stream.videoTracks[0].addSink(binding.localVideoRender)

                                        binding.localVideoRender.apply {
                                            setEnableHardwareScaler(true)
                                            setZOrderOnTop(true)
                                            setZOrderMediaOverlay(true)
                                            setMirror(true)

                                            init(eglContext, null)
                                        }

                                        binding.buttonSwitchCamera.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                        StreamState.REMOTE -> {
                            log("video call on remote")
                            it.eglBaseContext?.let { eglContext ->
                                it.stream?.let { stream ->
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        stream.videoTracks[0].addSink(binding.remoteVideoRender)

                                        binding.remoteVideoRender.apply {
                                            init(eglContext, null)

                                            binding.localVideoRender.layoutParams = (binding.localVideoRender.layoutParams as RelativeLayout.LayoutParams).apply {
                                                DisplayMetrics().let { metrics ->
                                                    display.getRealMetrics(metrics)

                                                    width = metrics.widthPixels / 3
                                                    height = metrics.heightPixels / 4
                                                }
                                                setMargins(0, 30, 30, 0)
                                            }

                                            binding.buttonHangup.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            }
                        }
                        StreamState.REMOVE_REMOTE -> {
                            log("video call on remove remote")
                            lifecycleScope.launch(Dispatchers.Main) {
                                it.stream?.let { stream ->
                                    stream.videoTracks[0].removeSink(binding.remoteVideoRender)
                                }
                            }
                        }
                    }
                }
        )

        compositeDisposable.add(
            mViewModel
                .statsProcessor
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.textStats.text = "localFps: ${it.localFps}\nremoteFps: ${it.remoteFps}\nlocalRate: ${it.localRate} kb/s\nremoteRate: ${it.remoteRate} kb/s"
                    }
                }
        )

        mViewModel.hangupLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    lifecycleScope.launch {
                        mViewModel.abortClient()
                        finish()
                    }
                }
                else -> {
                }
            }
        }

        lifecycleScope.launch {
            mViewModel.initClient()

            intent.getStringExtra(CALLEE)?.let { callee ->
                mViewModel.outCall(callee)
            } ?: let {
                intent.getStringExtra(TYPE)?.let { typeString ->
                    intent.getStringExtra(SDP)?.let { sdpString ->
                        mViewModel.incomingCall(typeString, sdpString)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        lifecycleScope.launch {
            mViewModel.hangup()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            mViewModel.abortClient()
        }
    }
}