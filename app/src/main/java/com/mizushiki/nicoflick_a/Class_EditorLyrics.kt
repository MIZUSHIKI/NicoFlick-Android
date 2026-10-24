package com.mizushiki.nicoflick_a

import com.atilika.kuromoji.ipadic.Tokenizer


// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
object EditorLyrics {
    private val tokenizer by lazy { Tokenizer() }
    /** Preserve punctuation, whitespace, unknown words and existing time tags. */
    fun hiragana(text: String): String {
        val tag = Regex("\\[[\\d*]{2}:[\\d*]{2}[:.][\\d*]{2}\\]|(?m)^@.*$")
        fun convert(part: String) = part.split('\n').joinToString("\n") { line ->
            buildString {
                var position = 0
                for (token in tokenizer.tokenize(line)) {
                    if (token.position > position) append(line.substring(position, token.position))
                    val reading = token.reading.takeUnless { it.isNullOrEmpty() || it == "*" } ?: token.surface
                    append(reading.map { if (it in '\u30a1'..'\u30f6') (it.code - 0x60).toChar() else it }.joinToString(""))
                    position = token.position + token.surface.length
                }
                if (position < line.length) append(line.substring(position))
            }
        }
        return buildString {
            var start = 0
            for (match in tag.findAll(text)) {
                append(convert(text.substring(start, match.range.first))); append(match.value)
                start = match.range.last + 1
            }
            append(convert(text.substring(start)))
        }
    }
    fun wikiSection(html: String): String? {
        val match = Regex("<h[23][^>]*>\\s*歌詞\\s*</h[23]>(.*?)(?=<h[234]\\b|<table\\b|$)",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)).find(html) ?: return null
        return match.groupValues[1]
            .replace(Regex("<ruby[^>]*>.*?<rt[^>]*>(.*?)</rt>.*?</ruby>", RegexOption.DOT_MATCHES_ALL), "$1")
            .replace(Regex("<(?:rt|rp)[^>]*>.*?</(?:rt|rp)>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<br\\s*/?>|</(?:p|div)>", RegexOption.IGNORE_CASE), "\n")
    }
}
