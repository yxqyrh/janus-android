package com.yxqyrh.janusandroid.ui.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yxqyrh.janusandroid.R

class VideoRoomSecretDialog {
    companion object {
        @SuppressLint("InflateParams")
        fun AppCompatActivity.showEditTextDialog(title: String, showHint: Boolean, positiveFun: (String?) -> Unit) {
            AlertDialog.Builder(this).apply {
                val editText = EditText(this@showEditTextDialog).apply {
                    if (showHint) hint = getString(R.string.string_optional)
                }
                setTitle(title)
                setView(editText)

                setPositiveButton(getString(R.string.OK)) { _, _ ->
                    positiveFun(
                        if (TextUtils.isEmpty(editText.text)) null else editText.text.toString()
                    )
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                show()
            }
        }
    }
}