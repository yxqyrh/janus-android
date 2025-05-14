package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.databinding.ActivityAudioBridgeBinding
import com.yxqyrh.janusandroid.ui.viewmodel.AudioBridgeViewModel

class AudioBridgeActivity : BaseVMActivity<ActivityAudioBridgeBinding, AudioBridgeViewModel>() {
    override fun getActivityBinding(): ActivityAudioBridgeBinding = ActivityAudioBridgeBinding.inflate(layoutInflater)
    override fun getViewModelClass(): Class<AudioBridgeViewModel> = AudioBridgeViewModel::class.java

    override fun initActivity() {

    }

}