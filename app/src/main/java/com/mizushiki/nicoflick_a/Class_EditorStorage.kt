package com.mizushiki.nicoflick_a

import android.content.Context
import android.util.AtomicFile
import com.eclipsesource.json.Json
import java.io.File

/** Separate files keep lyric drafts out of the large SharedPreferences caches. */

// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
class EditorStorage(context: Context, musicID: Int, levelID: Int) {
    private val directory = File(context.filesDir, "editor_drafts").apply { mkdirs() }
    private val file = AtomicFile(File(directory, if (levelID > 0) "level_$levelID.json" else "new_music_$musicID.json"))
    data class Draft(val text: String, val cursor: Int, val positionMs: Long, val rate: Float,
                     val stampOffsetMs: Int, val rateFactor: Double, val noteMode: Boolean)
    fun read(): Draft? {
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return null
        val json = Json.parse(file.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() }).asObject()
        return Draft(json.getString("text", ""), json.getInt("cursor", 0), json.getLong("positionMs", 0),
            json.getDouble("rate", 1.0).toFloat(), json.getInt("stampOffsetMs", -115),
            json.getDouble("rateFactor", 0.07), json.getBoolean("noteMode", false))
    }
    fun write(draft: Draft) {
        val text = Json.`object`().add("text", draft.text).add("cursor", draft.cursor)
            .add("positionMs", draft.positionMs).add("rate", draft.rate.toDouble())
            .add("stampOffsetMs", draft.stampOffsetMs).add("rateFactor", draft.rateFactor)
            .add("noteMode", draft.noteMode).toString()
        val output = file.startWrite()
        try { output.write(text.toByteArray(Charsets.UTF_8)); file.finishWrite(output) }
        catch (e: Exception) { file.failWrite(output); throw e }
    }
    fun delete() = file.delete()
}
