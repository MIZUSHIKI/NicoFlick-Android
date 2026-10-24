package com.mizushiki.nicoflick_a

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonValue

internal data class MusicScoreRecord(val label: String, val score: Long, val updatedAtMillis: Long)

/** nullは不正応答、空リストは正しい応答だが記録なし。部分反映しない。 */
internal fun parseMusicScoreResponse(response: JsonArray?): List<MusicScoreRecord>? {
    if (response == null || response.size() != 1 || !response[0].isObject) return null
    val json = response[0].asObject()
    val periods = listOf(
        Triple("全期間", "score", "updateTime"),
        Triple("年間", "scoreYear", "updateTimeYear"),
        Triple("月間", "scoreMonth", "updateTimeMonth"),
        Triple("週間", "scoreWeek", "updateTimeWeek")
    )
    var recognized = false
    val records = arrayListOf<MusicScoreRecord>()
    for ((label, scoreKey, timeKey) in periods) {
        val time = json.get(timeKey)
        val score = json.get(scoreKey)
        if (time == null) {
            if (score != null && !score.isNull) return null
            continue
        }
        recognized = true
        if (time.isNull) continue
        val seconds = parseScoreInteger(time) ?: return null
        val points = parseScoreInteger(score) ?: return null
        // 秒→ミリ秒のオーバーフローと、表示できない日付を防ぐ。
        if (seconds !in 0L..253402300799L || points < 0) return null
        records.add(MusicScoreRecord(label, points, seconds * 1000L))
    }
    return if (recognized) records else null
}

private fun parseScoreInteger(value: JsonValue?): Long? {
    if (value == null || value.isNull) return null
    return when {
        value.isString -> value.asString().toLongOrNull()
        value.isNumber -> value.toString().toLongOrNull()
        else -> null
    }
}
