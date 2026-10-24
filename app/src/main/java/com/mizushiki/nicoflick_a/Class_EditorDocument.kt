package com.mizushiki.nicoflick_a

import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** UI-independent counterpart of the iPhone Spoon/Spoons model. */

// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
data class EditorCharacter(var word: String, var timeMs: Int = -1, var note: Boolean = false) {
    val checkable: Boolean get() {
        val kana = Normalizer.normalize(word, Normalizer.Form.NFC).map {
            if (it in '\u30a1'..'\u30f6') (it.code - 0x60).toChar() else it
        }.joinToString("")
        return kana.length == 1 && kana[0] in '\u3041'..'\u3096' &&
            kana !in listOf("ぁ", "ぃ", "ぅ", "ぇ", "ぉ", "っ", "ゃ", "ゅ", "ょ", "ー")
    }
}

class EditorDocument(text: String = "") {
    enum class Export { LOCAL, DRAFT, PUBLISHED }
    var characters = mutableListOf<EditorCharacter>()
        private set
    var tags = linkedMapOf<String, String>()
        private set
    var cursor = 0
        set(value) { field = value.coerceIn(0, maxOf(0, characters.lastIndex)) }
    private data class Snapshot(val characters: List<EditorCharacter>, val tags: Map<String, String>, val cursor: Int)
    private val undo = mutableListOf<Snapshot>()
    private val redo = mutableListOf<Snapshot>()
    val canUndo get() = undo.isNotEmpty()
    val canRedo get() = redo.isNotEmpty()
    val offsetMs get() = tags["Offset"]?.toIntOrNull() ?: 0
    val speed get() = (tags["Speed"]?.toIntOrNull() ?: 150).coerceIn(50, 500)
    val level get() = tags["Level"]?.toIntOrNull() ?: 100

    init { parse(text) }

    private fun parse(text: String) {
        characters.clear(); tags.clear()
        val normalized = text.replace("\r\n", "\n").replace('\r', '\n')
        val body = normalized.lines().filter { line ->
            val match = Regex("^@([^=\\s]+)\\s*=\\s*(.*?)\\s*$").matchEntire(line)
            if (match != null) { tags[match.groupValues[1]] = match.groupValues[2]; false } else true
        }.joinToString("\n")
        val postedOffset = tags["OffsetWhenPosted"]?.toIntOrNull() ?: 0
        val pattern = Regex("\\[([\\d*]{2}):([\\d*]{2})([:.])([\\d*]{2})\\]")
        var pending: Pair<Int, Boolean>? = null
        var position = 0
        fun addWords(words: String) {
            val iterator = BreakIterator.getCharacterInstance(Locale.JAPANESE)
            iterator.setText(words)
            var start = iterator.first(); var end = iterator.next()
            while (end != BreakIterator.DONE) {
                val p = pending
                characters.add(EditorCharacter(words.substring(start, end), p?.first ?: -1, p?.second ?: false))
                pending = null; start = end; end = iterator.next()
            }
        }
        for (match in pattern.findAll(body)) {
            addWords(body.substring(position, match.range.first))
            pending?.let { characters.add(EditorCharacter("", it.first, it.second)) }
            val g = match.groupValues
            val time = if (g[1].contains('*') || g[2].contains('*') || g[4].contains('*')) -1
                else (g[1].toInt() * 60000 + g[2].toInt() * 1000 + g[4].toInt() * 10 - postedOffset).coerceAtLeast(0)
            pending = time to (g[3] == ".")
            position = match.range.last + 1
        }
        addWords(body.substring(position))
        pending?.let { characters.add(EditorCharacter("", it.first, it.second)) }
        while (characters.size > 1 && characters.last().word == "\n" && characters[characters.lastIndex - 1].word == "\n") characters.removeAt(characters.lastIndex)
        if (characters.isEmpty() || characters.last().word != "\n") characters.add(EditorCharacter("\n"))
        if (tags.containsKey("OffsetWhenPosted")) {
            tags["Offset"] = postedOffset.toString(); tags.remove("OffsetWhenPosted")
        }
        cursor = cursor
    }

    private fun snapshot() = Snapshot(characters.map { it.copy() }, tags.toMap(), cursor)
    private fun restore(s: Snapshot) {
        characters = s.characters.map { it.copy() }.toMutableList()
        tags = LinkedHashMap(s.tags); cursor = s.cursor
    }
    private fun change(operation: () -> Unit) {
        val before = snapshot(); operation(); cursor = cursor
        if (before != snapshot()) {
            undo.add(before); if (undo.size > 100) undo.removeAt(0)
            redo.clear()
        }
    }
    fun undo(): Boolean {
        if (undo.isEmpty()) return false
        redo.add(snapshot()); restore(undo.removeAt(undo.lastIndex)); return true
    }
    fun redo(): Boolean {
        if (redo.isEmpty()) return false
        undo.add(snapshot()); restore(redo.removeAt(redo.lastIndex)); return true
    }
    fun setMetadata(values: Map<String, String>) = change { tags.putAll(values) }
    fun replaceText(text: String, keepMetadata: Boolean = false) = change {
        val saved = tags.toMap(); parse(text)
        if (keepMetadata) tags.putAll(saved)
        cursor = 0
    }
    fun insertText(text: String) = change {
        val part = EditorDocument(text).characters.map { it.copy() }.toMutableList()
        if (!text.endsWith("\n") && part.lastOrNull()?.word == "\n") part.removeAt(part.lastIndex)
        characters.addAll(cursor, part); cursor += part.size
    }
    fun backDelete() = change {
        if (cursor > 0) { characters.removeAt(cursor - 1); cursor-- }
    }
    fun editCharacter(word: String, time: Int, note: Boolean) = change {
        characters[cursor] = EditorCharacter(word, if (time < 0) -1 else time, note)
    }
    fun stamp(timeMs: Int) = change {
        characters[cursor].timeMs = timeMs.coerceAtLeast(0)
        val next = (cursor + 1..characters.lastIndex).firstOrNull { characters[it].checkable }
        if (next != null) cursor = next
    }
    fun markAt(timeMs: Int, playing: Boolean): Boolean {
        var index = cursor
        if (playing) {
            val timed = characters.indices.filter { characters[it].timeMs >= 0 }
            index = timed.minByOrNull { abs(characters[it].timeMs.toLong() - timeMs) } ?: return false
            val first = timed.minOf { characters[it].timeMs }; val last = timed.maxOf { characters[it].timeMs }
            if (timeMs < first - 1000 || timeMs > last + 1000) return false
        }
        change { cursor = index; characters[index].note = true }
        return true
    }
    fun removePrevious(noteMode: Boolean): Int? {
        val index = (cursor downTo 0).firstOrNull { if (noteMode) characters[it].note else characters[it].timeMs >= 0 } ?: return null
        val time = characters[index].timeMs
        change { cursor = index; if (noteMode) characters[index].note = false else characters[index].timeMs = -1 }
        return time
    }
    fun export(mode: Export): String {
        val metadata = when (mode) {
            Export.LOCAL -> LinkedHashMap(tags).apply { put("NicoFlick", "3") }
            Export.DRAFT -> LinkedHashMap(tags.filterKeys { it in listOf("Description", "Offset") }).apply { put("NicoFlick", "2") }
            Export.PUBLISHED -> linkedMapOf("NicoFlick" to "1").apply { if (offsetMs != 0) put("OffsetWhenPosted", offsetMs.toString()) }
        }
        val offset = if (mode == Export.PUBLISHED) offsetMs else 0
        val body = buildString {
            for (c in characters) {
                if (c.timeMs >= 0) append(formatTime((c.timeMs.toLong() + offset).coerceAtLeast(0), c.note))
                else if (c.note && mode != Export.PUBLISHED) append("[**:**.**]")
                append(c.word)
            }
            if (!endsWith("\n")) append('\n')
            metadata.forEach { (key, value) -> if (value.isNotEmpty()) append("@$key=$value\n") }
        }
        return body
    }
    fun publicationProblem(): String? {
        val timedKana = characters.filter { it.timeMs >= 0 && it.word.any { ch -> ch in '\u3041'..'\u30ff' } }
        if (timedKana.isEmpty()) return "時刻を付けたかな文字がありません。"
        if (level <= 10 && timedKana.none { it.note && it.word != "ー" }) return "フリック対象がありません。ノーツモードで対象を指定してください。"
        if (characters.any { it.note && it.timeMs < 0 }) return "時刻のないフリック対象があります。先に時刻を付けてください。"
        if (characters.any { it.timeMs >= 0 && it.timeMs.toLong() + offsetMs < 0 }) return "オフセット適用後の時刻が負数になります。"
        var previous = -1
        for (c in characters) if (c.timeMs >= 0) {
            if (c.timeMs < previous) return "時刻が歌詞の順番と逆転している箇所があります。"
            if (c.timeMs >= 6000000) return "タイムタグの範囲（100分未満）を超えています。"
            previous = c.timeMs
        }
        return null
    }
    fun estimatedLevel(): Int {
        val times = characters.filter { it.timeMs >= 0 && it.checkable && (level == 100 || it.note) }.map { it.timeMs }
        val gaps = times.zipWithNext { a, b -> b - a }.filter { it in 0..2999 }
        if (gaps.isEmpty()) return 1
        return ((15 - sqrt((gaps.average() + 50) / 10)) * (1 + (speed - 150) / 500.0)).roundToInt().coerceIn(1, 10)
    }
    companion object {
        fun formatTime(timeMs: Long, note: Boolean = false): String {
            val ms = timeMs.coerceIn(0, 5999990)
            return "[%02d:%02d%s%02d]".format(Locale.US, ms / 60000, ms / 1000 % 60, if (note) "." else ":", ms / 10 % 100)
        }
        fun correctedStamp(positionMs: Long, rate: Float, offsetMs: Int, rateFactor: Double): Int =
            (positionMs + rateFactor * (1.0 - rate) * 1000 + offsetMs).toInt().coerceAtLeast(0)
    }
}
