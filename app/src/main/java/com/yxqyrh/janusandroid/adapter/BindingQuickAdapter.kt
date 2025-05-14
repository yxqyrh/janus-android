package com.yxqyrh.janusandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter

abstract class BindingQuickAdapter<T, B : ViewBinding>(
    data: MutableList<T>? = null
) : BaseQuickAdapter<T, BindingViewHolder<B>>(0, data) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<B> =
        BindingViewHolder(getAdapterBinding(LayoutInflater.from(parent.context)))

    override fun convert(holder: BindingViewHolder<B>, item: T) {
        convert(holder.adapterViewBinding, item)
    }

    protected abstract fun getAdapterBinding(layoutInflater: LayoutInflater): B
    protected abstract fun convert(adapterBinding: B, item: T)
}