package com.yxqyrh.janusandroid.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseVMActivity<B : ViewBinding, VM : ViewModel> : BaseActivity<B>() {
    val mViewModel by lazy { ViewModelProvider(this)[getViewModelClass()] }

    protected abstract fun getViewModelClass(): Class<VM>
}