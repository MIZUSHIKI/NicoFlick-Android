package com.mizushiki.nicoflick_a

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder


// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
object EditorApi {
    fun form(values: Map<String, String>): String = values.entries.joinToString("&") {
        URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8")
    }
    fun succeeded(response: String?) = response?.contains("<!--EchoHtmlMes_OK-->") == true
    fun errorMessage(response: String?): String {
        if (response == null) return "通信に失敗しました。端末の下書きは残っています。新規投稿の場合は、再送前に選曲画面で登録済みか確認してください。"
        val text = Regex("<!--echo-->(.*?)<!--/echo-->", RegexOption.DOT_MATCHES_ALL)
            .find(response)?.groupValues?.get(1) ?: "サーバーが処理を受け付けませんでした。"
        return text.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<[^>]*>"), "").trim().take(1000)
    }
    suspend fun checkPassword(levelID: Int, password: String, userID: String): Boolean = withContext(Dispatchers.IO) {
        val query = form(linkedMapOf("req" to "level-passCheckz", "id" to "$levelID", "userPASS" to password, "userID" to userID))
        HttpUtil.httpGET(GLOBAL.PHP_URL + "?" + query, noCache = true)?.trim() == "true"
    }
    suspend fun save(music: musicData, levelID: Int, document: EditorDocument, password: String, draft: Boolean): String? {
        val fields = linkedMapOf("req" to if (levelID > 0) "level-update" else "levelz-insert",
            "movieURL" to music.movieURL, "level" to document.level.toString(),
            "creator" to (document.tags["Creator"] ?: "NO_NAME"),
            "description" to if (draft) "【編集中:${USERDATA.UserID.take(8)}】" else (document.tags["Description"] ?: ""),
            "speed" to document.speed.toString(),
            "notes" to document.export(if (draft) EditorDocument.Export.DRAFT else EditorDocument.Export.PUBLISHED),
            "userPASS" to password)
        if (levelID > 0) fields["id"] = levelID.toString() else fields["userID"] = USERDATA.UserID
        return withContext(Dispatchers.IO) { HttpUtil.httpPOST(GLOBAL.PHP_URL, form(fields), noCache = true) }
    }
    suspend fun delete(levelID: Int, password: String): String? = withContext(Dispatchers.IO) {
        HttpUtil.httpPOST(GLOBAL.PHP_URL, form(linkedMapOf("req" to "level-delete", "id" to "$levelID", "userPASS" to password)), noCache = true)
    }
}
