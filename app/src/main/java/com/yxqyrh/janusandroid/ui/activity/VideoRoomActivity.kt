package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.R
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.databinding.ActivityVideoRoomBinding
import com.yxqyrh.janusandroid.model.ResponseModelPublisher
import com.yxqyrh.janusandroid.rtc.model.StreamResource
import com.yxqyrh.janusandroid.rtc.model.StreamState
import com.yxqyrh.janusandroid.ui.dialog.VideoRoomSecretDialog.Companion.showEditTextDialog
import com.yxqyrh.janusandroid.ui.fragment.HostFragment.Companion.showHostFragment
import com.yxqyrh.janusandroid.ui.viewmodel.VideoRoomViewModel
import com.yxqyrh.janusandroid.utils.Logger.log
import com.yxqyrh.janusandroid.utils.MoshiUtil
import com.yxqyrh.janusandroid.utils.RxPermissionUtil
import com.yxqyrh.janusandroid.utils.RxPermissionUtil.requestRxPermission
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

class VideoRoomActivity : BaseVMActivity<ActivityVideoRoomBinding, VideoRoomViewModel>() {

    companion object {
        private const val HANDLE_ID = "handle_id"
        private const val ROOM_ID = "room_id"
        private const val PUBLISHERS = "publishers"

        fun AppCompatActivity.startVideoRoomActivity(handleId: Long, roomId: Long, publishers: List<ResponseModelPublisher>? = null, finishActivity: Boolean = false) {
            requestRxPermission(RxPermissionUtil.videoPermissions) {
                startActivity(Intent(this, VideoRoomActivity::class.java).apply {
                    putExtra(HANDLE_ID, handleId)
                    putExtra(ROOM_ID, roomId)
                    putExtra(PUBLISHERS, MoshiUtil.toJson(publishers))
                })
                if (finishActivity) finish()
            }
        }
    }

    override fun getActivityBinding() = ActivityVideoRoomBinding.inflate(layoutInflater)
    override fun getViewModelClass() = VideoRoomViewModel::class.java

    private val renderAdapter by lazy {
        object : BaseQuickAdapter<StreamResource, BaseViewHolder>(R.layout.item_remote_video, null) {
            override fun convert(holder: BaseViewHolder, item: StreamResource) {
                holder.getView<SurfaceViewRenderer>(R.id.remote_video_render).let { videoRenderer ->
                    videoRenderer.init(item.eglBaseContext, null)

                    holder.setText(R.id.text_display, item.display)

                    if (item.videoModeration) {
                        videoRenderer.visibility = View.GONE
                    } else {
                        item.stream?.videoTracks?.get(0)?.addSink(videoRenderer)
                        videoRenderer.setMirror(true)
                        videoRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                    }
                }
            }
        }
    }

    override fun initActivity() {
        mViewModel.publisherHandleIdRef.set(intent.getLongExtra(HANDLE_ID, 0))
        mViewModel.roomIdRef.set(intent.getLongExtra(ROOM_ID, 0))
        intent.getStringExtra(PUBLISHERS)?.let {
            mViewModel.publishersRef.set(it)
        }

        binding.recyclerViewRender.apply {
            adapter = renderAdapter
            layoutManager = GridLayoutManager(this@VideoRoomActivity, 2)
        }

        binding.buttonAudio.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.switchAudio()
                (Dispatchers.Main) {
                    binding.buttonAudio.text =
                        when (binding.buttonAudio.text) {
                            "开启音频" -> "关闭音频"
                            else -> "开启音频"
                        }
                }
            }
        }

        binding.buttonVideo.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.switchVideo()
                (Dispatchers.Main) {
                    binding.buttonVideo.text =
                        when (binding.buttonVideo.text) {
                            "打开摄像头" -> "关闭摄像头"
                            else -> "打开摄像头"
                        }
                }
            }
        }

        binding.buttonHost.setOnClickListener {
            if (mViewModel.secretLiveData.value?.status == Status.SUCCESS) {
                showHostFragment()
            } else {
                lifecycleScope.launch {
                    mViewModel.getSecret()?.let { videoRoomSecret ->
                        mViewModel.listForwarders(videoRoomSecret.secret)
                    } ?: (Dispatchers.Main) {
                        showEditTextDialog("请输入管理密码", true) { secret ->
                            lifecycleScope.launch {
                                mViewModel.listForwarders(secret)
                            }
                        }
                    }
                }
            }
        }

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

        mViewModel.secretLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    showHostFragment()
                }
                Status.ERROR -> {
                }
                else -> {
                }
            }
        }

        mViewModel.moderationLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    for (resource in renderAdapter.data) {
                        if (it.data?.pluginData?.data?.id == resource.feedId) {
                            it.data?.pluginData?.data?.audioModeration?.let { audioModeration ->
                                resource.audioModeration = audioModeration == "muted"
                            }
                            it.data?.pluginData?.data?.videoModeration?.let { videoModeration ->
                                resource.videoModeration = videoModeration == "muted"
                            }
                            renderAdapter.notifyItemChanged(renderAdapter.data.indexOf(resource))
                            break
                        }
                    }
                }
                else -> {
                }
            }
        }

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

        compositeDisposable.add(mViewModel
            .streamProcessor
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                when (it.state) {
                    StreamState.LOCAL -> {
                        log("video room on local")
                        lifecycleScope.launch(Dispatchers.Main) {
                            renderAdapter.addData(0, it)
                        }
                    }
                    StreamState.REMOTE -> {
                        log("video room on remote")
                        lifecycleScope.launch(Dispatchers.Main) {
                            renderAdapter.addData(it)
                        }
                    }
                    StreamState.REMOVE_REMOTE -> {
                        log("video room on remove remote")
                        for (resource in renderAdapter.data) {
                            if (it.handleId == resource.handleId) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    renderAdapter.remove(resource)
                                }
                                break
                            }
                        }
                    }
                }
            }
        )

        lifecycleScope.launch {
            mViewModel.initClient()
            mViewModel.joinRoom()
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