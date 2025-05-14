package com.yxqyrh.janusandroid.ui.fragment

import com.yxqyrh.janusandroid.R
import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.databinding.FragmentHostBinding
import com.yxqyrh.janusandroid.model.ResponseModelPublisher
import com.yxqyrh.janusandroid.ui.activity.VideoRoomActivity
import com.yxqyrh.janusandroid.ui.viewmodel.VideoRoomViewModel
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.launch

@SuppressLint("MissingSuperCall")
class HostFragment private constructor() : BaseBottomSheetFragment<FragmentHostBinding>() {
    companion object {
        @UiThread
        fun AppCompatActivity.showHostFragment() {
            HostFragment().show(supportFragmentManager, HostFragment::class.java.simpleName)
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHostBinding.inflate(inflater, container, false)

    private val mViewModel: VideoRoomViewModel get() = (activity as VideoRoomActivity).mViewModel

    private val participantAdapter by lazy {
        object : BaseQuickAdapter<ResponseModelPublisher, BaseViewHolder>(R.layout.item_participant) {
            override fun convert(holder: BaseViewHolder, item: ResponseModelPublisher) {
                holder.apply {
                    setText(R.id.text_name, item.display)

                    val id: Long = item.id ?: 0

                    getView<Button>(R.id.button_kick).apply {
                        if (0 == data.indexOf(item)) visibility = View.GONE

                        setOnClickListener {
                            lifecycleScope.launch {
                                mViewModel.kick(id)
                                remove(item)
                            }
                        }
                    }

                    val audioMuted = item.audioModerated != null && item.audioModerated == true
                    val videoMuted = item.videoModerated != null && item.videoModerated == true

                    getView<Button>(R.id.button_audio).apply {
                        text = when (audioMuted) {
                            true -> "开启音频"
                            false -> "关闭音频"
                        }

                        setOnClickListener {
                            lifecycleScope.launch {
                                item.audioModerated = !audioMuted
                                mViewModel.moderate(id, !audioMuted, videoMuted)

                                notifyItemChanged(data.indexOf(item))
                            }
                        }
                    }

                    getView<Button>(R.id.button_video).apply {
                        text = when (videoMuted) {
                            true -> "开启视频"
                            false -> "关闭视频"
                        }

                        setOnClickListener {
                            lifecycleScope.launch {
                                item.videoModerated = !videoMuted
                                mViewModel.moderate(id, audioMuted, !videoMuted)

                                notifyItemChanged(data.indexOf(item))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewParticipants.apply {
            adapter = participantAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }

        binding.buttonDestroy.setOnClickListener {
            lifecycleScope.launch {
                mViewModel.destroyRoom()
            }
        }

        mViewModel.participantsLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    it?.data?.let { participantList ->
                        participantAdapter.setList(participantList)
                    }
                    binding.refreshLayout.isRefreshing = false
                }
                Status.ERROR -> {
                }
                else -> {
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            binding.refreshLayout.isRefreshing = true
            mViewModel.getParticipants()
        }
    }

    override fun isSheetAlwaysExpanded() = true
    override fun animateCornerRadius() = false
}