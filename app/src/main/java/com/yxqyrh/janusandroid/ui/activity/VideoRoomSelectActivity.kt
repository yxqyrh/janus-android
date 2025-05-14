package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.R
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.databinding.ActivityVideoRoomSelectBinding
import com.yxqyrh.janusandroid.model.ResponseModelListData
import com.yxqyrh.janusandroid.ui.activity.VideoRoomActivity.Companion.startVideoRoomActivity
import com.yxqyrh.janusandroid.ui.dialog.JoinVideoRoomDialog.Companion.showJoinVideoRoomDialog
import com.yxqyrh.janusandroid.ui.dialog.JoinVideoRoomWithRoomIdDialog.Companion.showJoinVideoRoomWithRoomIdDialog
import com.yxqyrh.janusandroid.ui.dialog.LaunchVideoRoomDialog.Companion.showLaunchVideoRoomDialog
import com.yxqyrh.janusandroid.ui.viewmodel.VideoRoomSelectViewModel
import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

class VideoRoomSelectActivity : BaseVMActivity<ActivityVideoRoomSelectBinding, VideoRoomSelectViewModel>() {

    companion object {
        fun Context.startVideoRoomSelectActivity() {
            startActivity(Intent(this, VideoRoomSelectActivity::class.java))
        }
    }

    override fun getActivityBinding(): ActivityVideoRoomSelectBinding = ActivityVideoRoomSelectBinding.inflate(layoutInflater)
    override fun getViewModelClass(): Class<VideoRoomSelectViewModel> = VideoRoomSelectViewModel::class.java

    private val roomAdapter by lazy {
        object : BaseQuickAdapter<ResponseModelListData, BaseViewHolder>(R.layout.item_menu, null) {
            override fun convert(holder: BaseViewHolder, item: ResponseModelListData) {
                holder.apply {
                    setText(R.id.text_menu, "${item.description}\n${item.room}")
                }
            }
        }.apply {
            setOnItemClickListener { adapter, _, position ->
                (adapter.data[position] as ResponseModelListData).let { item ->
                    item.room?.let { roomId ->
                        lifecycleScope.launch {
                            mViewModel.getPin(roomId)?.let { videoRoomPin ->
                                mViewModel.joinRoom(roomId, videoRoomPin.pin)
                            } ?: (Dispatchers.Main) {
                                showJoinVideoRoomWithRoomIdDialog(roomId) { pin: String? ->
                                    lifecycleScope.launch {
                                        mViewModel.joinRoom(roomId, pin)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun initActivity() {
        binding.buttonRoomList.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.getRoomList()
            }
        }

        binding.buttonLaunch.setOnClickListener {
            showLaunchVideoRoomDialog { description: String?, room: Long?, secret: String?, pin: String?, isPrivate: Boolean ->
                lifecycleScope.launch {
                    mViewModel.createRoom(description, room, secret, pin, isPrivate)
                }
            }
        }

        binding.buttonJoin.setOnClickListener {
            showJoinVideoRoomDialog { room: Long, pin: String? ->
                lifecycleScope.launch {
                    mViewModel.joinRoom(room, pin)
                }
            }
        }

        binding.recyclerRoomList.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(this@VideoRoomSelectActivity, LinearLayoutManager.VERTICAL, false)
        }

        mViewModel.createHandleLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.data?.id?.let { handleId -> mViewModel.handleIdRef.set(handleId) }
                    binding.textResult.text = "注册handle成功"
                    lifecycleScope.launch {
                        mViewModel.getRoomList()
                    }
                }
                Status.ERROR -> {
                    binding.textResult.text = "注册handle失败"
                }
                else -> {
                }
            }
        }

        mViewModel.getRoomListLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.textResult.text = "获取房间列表成功"
                    it?.data?.pluginData?.data?.list?.let { roomList -> roomAdapter.setList(roomList) }
                }
                Status.ERROR -> {
                    binding.textResult.text = "获取房间列表失败"
                }
                else -> {
                }
            }
        }

        mViewModel.createRoomLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.textResult.text = "创建房间成功"
                    it?.data?.pluginData?.data?.room?.let { room ->
                        startVideoRoomActivity(mViewModel.handleIdRef.get(), room, null, true)
                    }
                }
                Status.ERROR -> {
                    binding.textResult.text = "创建房间失败"
                }
                else -> {
                }
            }
        }

        mViewModel.joinRoomLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.textResult.text = "加入房间成功"
                    it.data?.pluginData?.data?.room?.let { room ->
                        it.data.pluginData.data.publishers?.let { publishers ->
                            startVideoRoomActivity(mViewModel.handleIdRef.get(), room, publishers, true)
                        }
                    }
                }
                Status.ERROR -> {
                    binding.textResult.text = "加入房间失败"
                }
                else -> {
                }
            }
        }

        lifecycleScope.launch {
            mViewModel.createHandle()
        }
    }
}