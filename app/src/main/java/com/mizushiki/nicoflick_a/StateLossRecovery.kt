package com.mizushiki.nicoflick_a

import android.app.Activity
import android.content.Intent
import android.widget.Toast

/**
 * GLOBALのプレイ情報はプロセス終了で消える。古い画面へ戻らないように
 * タスクを作り直し、データ取得の入口であるスタート画面へ退避する。
 */
internal fun Activity.returnToStartAfterStateLoss() {
    if (isFinishing || isDestroyed) return
    Toast.makeText(applicationContext,
        "プレイ情報を復元できなかったため、もう一度選曲してください。",
        Toast.LENGTH_LONG).show()
    startActivity(Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
    finish()
}
