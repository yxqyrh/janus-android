package com.yxqyrh.janusandroid.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment

@SuppressLint("MissingSuperCall")
abstract class BaseBottomSheetFragment<B : ViewBinding> : SuperBottomSheetFragment() {
    private var _binding: B? = null
    protected val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = getActivityBinding(inflater, container)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected abstract fun getActivityBinding(inflater: LayoutInflater, container: ViewGroup?): B
}