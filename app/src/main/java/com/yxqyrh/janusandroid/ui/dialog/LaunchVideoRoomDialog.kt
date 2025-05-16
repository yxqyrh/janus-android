package com.yxqyrh.janusandroid.ui.dialog

import com.yxqyrh.janusandroid.R
import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class LaunchVideoRoomDialog {
    companion object {
        @SuppressLint("InflateParams")
        fun AppCompatActivity.showLaunchVideoRoomDialog(positiveFun: (String?, Long?, String?, String?, Boolean) -> Unit) {
            AlertDialog.Builder(this).apply {
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_launch_video_room, null)
                val editTextTheme = dialogView.findViewById<EditText>(R.id.edit_text_theme)
                val editTextRoom = dialogView.findViewById<EditText>(R.id.edit_text_room)
                val editTextAdminPassword = dialogView.findViewById<EditText>(R.id.edit_text_admin_password)
                val editTextPassword = dialogView.findViewById<EditText>(R.id.edit_text_password)
                val switchPrivate = dialogView.findViewById<SwitchCompat>(R.id.switch_private)

                setView(dialogView)
                setPositiveButton(getString(R.string.OK)) { _, _ ->
                    positiveFun(
                        if (TextUtils.isEmpty(editTextTheme.text)) null else editTextTheme.text.toString(),
                        if (TextUtils.isEmpty(editTextRoom.text)) null else editTextRoom.text.toString().toLong(),
                        if (TextUtils.isEmpty(editTextAdminPassword.text)) null else editTextAdminPassword.text.toString(),
                        if (TextUtils.isEmpty(editTextPassword.text)) null else editTextPassword.text.toString(),
                        !switchPrivate.isChecked
                    )
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                show()
            }
        }
    }
}