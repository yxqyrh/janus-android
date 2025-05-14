package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.core.Status
import com.yxqyrh.janusandroid.databinding.ActivityLaunchBinding
import com.yxqyrh.janusandroid.ui.activity.MenuActivity.Companion.startMenuActivity
import com.yxqyrh.janusandroid.ui.viewmodel.LaunchActivityViewModel

class LaunchActivity : BaseVMActivity<ActivityLaunchBinding, LaunchActivityViewModel>() {
    override fun getActivityBinding() = ActivityLaunchBinding.inflate(layoutInflater)

    override fun getViewModelClass() = LaunchActivityViewModel::class.java

    override fun initActivity() {
        mViewModel.createSessionLiveData.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    startMenuActivity()
                    finish()
                }
                else -> {

                }
            }
        }
    }
}