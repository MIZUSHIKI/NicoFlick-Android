package com.mizushiki.nicoflick_a

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
object EditorLauncher {
    fun show(activity: AppCompatActivity, music: musicData?, level: levelData?, requestCode: Int) {
        if (music == null) return
        val choices = if (level == null) arrayOf("新しい譜面を作成") else arrayOf("選択中の譜面を編集", "選択中の譜面を基に新規作成", "空の譜面から新規作成")
        AlertDialog.Builder(activity).setTitle("ゲームエディター").setItems(choices) { _, index ->
            val intent = Intent(activity, Activity_Editor::class.java).putExtra("musicID", music.sqlID)
            if (level != null && index == 0) intent.putExtra("levelID", level.sqlID)
            else if (level != null && index == 1) intent.putExtra("baseLevelID", level.sqlID)
            activity.startActivityForResult(intent, requestCode)
        }.setNegativeButton("キャンセル", null).show()
    }
}
