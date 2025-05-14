package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.R
import com.yxqyrh.janusandroid.databinding.ActivityMenuBinding
import com.yxqyrh.janusandroid.datastore.NicknamePreferencesRepository
import com.yxqyrh.janusandroid.ui.activity.AudioBridgeSelectActivity.Companion.startAudioBridgeSelectActivity
import com.yxqyrh.janusandroid.ui.activity.VideoCallSelectActivity.Companion.startVideoCallSelectActivity
import com.yxqyrh.janusandroid.ui.activity.VideoRoomSelectActivity.Companion.startVideoRoomSelectActivity
import com.yxqyrh.janusandroid.ui.dialog.VideoRoomSecretDialog.Companion.showEditTextDialog
import com.yxqyrh.janusandroid.utils.ToastUtil.showToast
import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

class MenuActivity : BaseActivity<ActivityMenuBinding>() {

    companion object {
        fun Context.startMenuActivity() {
            startActivity(Intent(this, MenuActivity::class.java))
        }
    }

    private val menuAdapter by lazy {
        object : BaseQuickAdapter<MenuEnum, BaseViewHolder>(R.layout.item_menu, null) {
            override fun convert(holder: BaseViewHolder, item: MenuEnum) {
                holder.apply {
                    setText(R.id.text_menu, item.menuName)
                }
            }
        }.apply {
            setOnItemClickListener { adapter, _, position ->
                when (adapter.data[position]) {
                    MenuEnum.VIDEO_ROOM -> {
                        lifecycleScope.launch {
                            NicknamePreferencesRepository.readNickname(this@MenuActivity)?.let {
                                (Dispatchers.Main) {
                                    startVideoRoomSelectActivity()
                                }
                            } ?: (Dispatchers.Main) {
                                showEditTextDialog("请输入昵称", false) { nickname ->
                                    nickname?.let {
                                        lifecycleScope.launch {
                                            NicknamePreferencesRepository.saveNickname(this@MenuActivity, nickname)
                                            (Dispatchers.Main) {
                                                startVideoRoomSelectActivity()
                                            }
                                        }
                                    } ?: showToast("昵称不能为空！")
                                }
                            }
                        }
                    }
                    MenuEnum.VIDEO_CALL -> {
                        startVideoCallSelectActivity()
                    }
                    MenuEnum.AUDIO_BRIDGE -> {
                        startAudioBridgeSelectActivity()
                    }
                    MenuEnum.WEB_VIEW -> {

                    }
                }
            }
        }
    }

    override fun getActivityBinding(): ActivityMenuBinding = ActivityMenuBinding.inflate(layoutInflater)

    override fun initActivity() {
        menuAdapter.setList(ArrayList<MenuEnum>().apply { addAll(MenuEnum.values()) })

        binding.recyclerViewMenu.adapter = menuAdapter
        binding.recyclerViewMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private enum class MenuEnum(val menuName: String) {
        VIDEO_ROOM("video room"),
        VIDEO_CALL("video call"),
        AUDIO_BRIDGE("audio bridge"),
        WEB_VIEW("webView")
    }
}

