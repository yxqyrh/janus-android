package com.yxqyrh.janusandroid.ui.fragment

import com.yxqyrh.janusandroid.databinding.FragmentOperationBinding
import com.yxqyrh.janusandroid.ui.activity.VideoRoomActivity
import com.yxqyrh.janusandroid.ui.viewmodel.VideoRoomViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity

class OperationFragment private constructor() : BaseBottomSheetFragment<FragmentOperationBinding>() {
    companion object {
        @UiThread
        fun AppCompatActivity.showOperationFragment() {
            OperationFragment().show(supportFragmentManager, OperationFragment::class.java.simpleName)
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentOperationBinding.inflate(inflater, container, false)

    private val mViewModel: VideoRoomViewModel get() = (activity as VideoRoomActivity).mViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun isSheetAlwaysExpanded() = true
    override fun animateCornerRadius() = false
}