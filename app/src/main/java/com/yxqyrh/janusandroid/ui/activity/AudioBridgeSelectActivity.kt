package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.databinding.ActivityAudioBridgeSelectBinding
import com.yxqyrh.janusandroid.ui.viewmodel.AudioBridgeSelectViewModel
import android.content.Context
import android.content.Intent

class AudioBridgeSelectActivity : BaseVMActivity<ActivityAudioBridgeSelectBinding, AudioBridgeSelectViewModel>() {

    companion object {
        fun Context.startAudioBridgeSelectActivity() {
            startActivity(Intent(this, AudioBridgeSelectActivity::class.java))
        }
    }

    override fun getActivityBinding(): ActivityAudioBridgeSelectBinding = ActivityAudioBridgeSelectBinding.inflate(layoutInflater)
    override fun getViewModelClass(): Class<AudioBridgeSelectViewModel> = AudioBridgeSelectViewModel::class.java

    override fun initActivity() {

    }
}