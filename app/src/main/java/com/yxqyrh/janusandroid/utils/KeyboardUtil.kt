package com.yxqyrh.janusandroid.utils

import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object KeyboardUtil {
    fun AppCompatActivity.hideKeyboard() {
        lifecycleScope.launch(Dispatchers.Main) {
            currentFocus?.let {
                ContextCompat.getSystemService(this@hideKeyboard, InputMethodManager::class.java)?.apply {
                    hideSoftInputFromWindow(it.windowToken, 0)
                }
            }
        }
    }

    fun AppCompatActivity.showKeyboard() {
        lifecycleScope.launch(Dispatchers.Main) {
            currentFocus?.let {
                ContextCompat.getSystemService(this@showKeyboard, InputMethodManager::class.java)?.apply {
                    showSoftInput(it, 0)
                }
            }
        }
    }
}