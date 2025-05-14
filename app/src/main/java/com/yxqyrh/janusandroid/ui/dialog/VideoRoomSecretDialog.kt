package com.yxqyrh.janusandroid.ui.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class VideoRoomSecretDialog {
    companion object {
        @SuppressLint("InflateParams")
        fun AppCompatActivity.showEditTextDialog(title: String, showHint: Boolean, positiveFun: (String?) -> Unit) {
            AlertDialog.Builder(this).apply {
                val editText = EditText(this@showEditTextDialog).apply {
                    if (showHint) hint = "选填"
                }
                setTitle(title)
                setView(editText)

                setPositiveButton("确定") { _, _ ->
                    positiveFun(
                        if (TextUtils.isEmpty(editText.text)) null else editText.text.toString()
                    )
                }
                setNegativeButton("取消") { _, _ -> }
                show()
            }
        }
    }
}