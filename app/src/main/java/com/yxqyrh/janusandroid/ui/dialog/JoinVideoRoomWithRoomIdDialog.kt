package com.yxqyrh.janusandroid.ui.dialog

import com.yxqyrh.janusandroid.R
import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class JoinVideoRoomWithRoomIdDialog {
    companion object {
        @SuppressLint("InflateParams")
        fun AppCompatActivity.showJoinVideoRoomWithRoomIdDialog(roomId: Long, positiveFun: (String?) -> Unit) {
            AlertDialog.Builder(this).apply {
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_join_video_room_with_room_id, null)
                val textRoom = dialogView.findViewById<TextView>(R.id.text_room)
                val editTextPassword = dialogView.findViewById<EditText>(R.id.edit_text_password)

                textRoom.text = roomId.toString()

                setView(dialogView)
                setPositiveButton(getString(R.string.OK)) { _, _ ->
                    positiveFun(
                        if (TextUtils.isEmpty(editTextPassword.text)) null else editTextPassword.text.toString()
                    )
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                show()
            }
        }
    }
}