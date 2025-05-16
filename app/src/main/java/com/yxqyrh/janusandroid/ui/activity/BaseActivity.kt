package com.yxqyrh.janusandroid.ui.activity

import com.yxqyrh.janusandroid.core.VideoRoomApplication
import com.yxqyrh.janusandroid.utils.Logger.log
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    protected val binding by lazy { getActivityBinding() }
    protected val mApplication by lazy { application as VideoRoomApplication }
    protected val scarletInstance by lazy { mApplication.scarletInstance }

    protected val compositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        log("ActivityName", this.javaClass.simpleName)

        setContentView(binding.root)
        initActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    protected abstract fun getActivityBinding(): B

    protected abstract fun initActivity()
}