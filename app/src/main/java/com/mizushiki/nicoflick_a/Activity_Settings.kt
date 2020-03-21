package com.mizushiki.nicoflick_a

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.activity_settings.*


class Activity_Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        edit_Mail.setText(USERDATA.NicoMail)
        edit_Pass.setText(USERDATA.NicoPass)
        edit_registName.setText(USERDATA.UserName)
        textView_uniqueUserID.text = USERDATA.UserIDxxx
    }

    fun Button_Back(view: View) {
        finish()
    }

    fun Button_Regist(view: View) {
        progress_circular.isVisible = true
        USERDATA.UserName = edit_registName.text.toString()

        ServerDataHandler().postUserName(edit_registName.text.toString(),USERDATA.UserID){
            if(it){
                progress_circular.isVisible = false
                AlertDialog.Builder(this)
                    .setMessage("登録しました。")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    //GoBack時
    override fun onStop() {
        USERDATA.NicoMail = edit_Mail.text.toString()
        USERDATA.NicoPass = edit_Pass.text.toString()
        super.onStop()
    }

    fun ButtonLoadDataDelete(view: View) {
        AlertDialog.Builder(this)
            .setTitle("データベースからロードしたデータを全て削除")
            .setMessage("・何か不具合があった場合の初期化用です。")
            .setPositiveButton("Yes", { dialog, which ->
                USERDATA.MusicsJson = ""
                USERDATA.LevelsJson = ""
                USERDATA.UserNamesJson = ""
            })
            .setNegativeButton("No", { dialog, which ->
            })
            .show()
    }
}
