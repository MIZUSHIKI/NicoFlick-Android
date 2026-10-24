package com.mizushiki.nicoflick_a

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/** 名前の送信中は、背面の操作・戻る操作・ダイアログ外のタップを防ぐ。 */
fun AppCompatActivity.registerUserNameBlocking(name: String, onResult: (Boolean) -> Unit) {
    if (isFinishing || isDestroyed) return
    val progress = AlertDialog.Builder(this).setMessage("プレイヤー名を登録中です…")
        .setCancelable(false).create()
    progress.setCanceledOnTouchOutside(false)
    val observer = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            progress.dismiss()
            lifecycle.removeObserver(this)
        }
    }
    lifecycle.addObserver(observer)
    progress.show()
    ServerDataHandler().postUserName(name, USERDATA.UserID) { success ->
        if (success) USERDATA.UserName = name
        lifecycle.removeObserver(observer)
        progress.dismiss()
        if (!isFinishing && !isDestroyed) onResult(success)
    }
}
