package com.yxqyrh.janusandroid.ui.dialog

import com.yxqyrh.janusandroid.R
import com.yxqyrh.janusandroid.utils.ToastUtil.showToast
import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class JoinVideoRoomDialog {
    companion object {
        @SuppressLint("InflateParams")
        fun AppCompatActivity.showJoinVideoRoomDialog(positiveFun: (Long, String?) -> Unit) {
            AlertDialog.Builder(this).apply {
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_join_video_room, null)
                val editTextRoom = dialogView.findViewById<EditText>(R.id.edit_text_room)
                val editTextPassword = dialogView.findViewById<EditText>(R.id.edit_text_password)

                setView(dialogView)
                setPositiveButton("确定") { _, _ ->
                    if (TextUtils.isEmpty(editTextRoom.text?.toString()?.trim())) {
                        showToast("未填写会议号！")
                    } else {
                        positiveFun(
                            editTextRoom.text.toString().toLong(),
                            if (TextUtils.isEmpty(editTextPassword.text)) null else editTextPassword.text.toString()
                        )
                    }
                }
                setNegativeButton("取消") { _, _ -> }
                show()
            }
        }
    }
}