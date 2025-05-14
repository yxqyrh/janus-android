package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.R
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.databinding.ActivityVideoCallSelectBinding
import com.yxqyrh.janusandroid.ui.activity.VideoCallActivity.Companion.startVideoCallActivity
import com.yxqyrh.janusandroid.ui.viewmodel.VideoCallSelectViewModel
import com.yxqyrh.janusandroid.utils.KeyboardUtil.hideKeyboard
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.launch

class VideoCallSelectActivity : BaseVMActivity<ActivityVideoCallSelectBinding, VideoCallSelectViewModel>() {

    companion object {
        fun Context.startVideoCallSelectActivity() {
            startActivity(Intent(this, VideoCallSelectActivity::class.java))
        }
    }

    override fun getActivityBinding(): ActivityVideoCallSelectBinding = ActivityVideoCallSelectBinding.inflate(layoutInflater)
    override fun getViewModelClass(): Class<VideoCallSelectViewModel> = VideoCallSelectViewModel::class.java

    private val userAdapter by lazy {
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_menu, null) {
            override fun convert(holder: BaseViewHolder, item: String) {
                holder.apply {
                    setText(R.id.text_menu, item)
                }
            }
        }.apply {
            setOnItemClickListener { adapter, _, position ->
                startVideoCallActivity(mViewModel.handleIdRef.get(), adapter.data[position] as String)
            }
        }
    }

    override fun initActivity() {
        mViewModel.createHandleLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.data?.id?.let { handleId -> mViewModel.handleIdRef.set(handleId) }
                    binding.textResult.text = "注册handle成功"
                }
                Status.ERROR -> {
                    binding.textResult.text = "注册handle失败"
                }
                else -> {
                }
            }
        }

        mViewModel.registerUserLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.textResult.text = "注册用户成功"
                    lifecycleScope.launch {
                        mViewModel.getVideoCallUserList()
                    }
                }
                Status.ERROR -> {
                    binding.textResult.text = "注册用户失败"
                }
                else -> {
                }
            }
        }

        mViewModel.videoCallUserListLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.textResult.text = "获取用户列表成功"

                    binding.textRegister.visibility = View.GONE
                    binding.buttonRegister.visibility = View.GONE

                    it.data?.pluginData?.data?.result?.list?.let { userList ->
                        userAdapter.setList(ArrayList<String>().apply {
                            addAll(userList)
                            remove(mViewModel.userNameRef.get())
                        })
                    }
                }
                Status.ERROR -> {
                    binding.textResult.text = "获取用户列表失败"
                }
                else -> {
                }
            }
        }

        mViewModel.incomingCallLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.jsep?.type?.let { type ->
                        it.data.jsep.sdp?.let { sdp ->
                            startVideoCallActivity(mViewModel.handleIdRef.get(), type, sdp)
                        }
                    }
                }
                else -> {
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            hideKeyboard()
            binding.textRegister.text?.toString()?.let { registerName ->
                if (registerName.isNotEmpty() && mViewModel.createHandleLiveData.value?.status != Status.ERROR) {
                    lifecycleScope.launch {
                        mViewModel.registerUser(registerName)
                    }
                }
            }
        }

        binding.recyclerViewUser.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@VideoCallSelectActivity, LinearLayoutManager.VERTICAL, false)
        }

        lifecycleScope.launch {
            mViewModel.createHandle()
        }
    }
}