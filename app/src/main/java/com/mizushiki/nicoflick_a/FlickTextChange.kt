package com.mizushiki.nicoflick_a

/**
 * Converts an actual editor change into at most one game input.
 * A composition resend/confirmation and deletion are not new flicks.
 * Paste, prediction and other multi-character edits must not score several notes at once.
 */
internal object FlickTextChange {
    const val INVALID_INPUT = "x"

    fun detect(before: String, after: String, confirmedComposition: String? = null,
               submittedText: String? = null): String? {
        if (before == after) return null
        // A candidate confirmation replaces already judged composing characters.
        if (confirmedComposition != null && submittedText != null &&
            !submittedText.startsWith(confirmedComposition)) return null

        var prefix = 0
        val commonLength = minOf(before.length, after.length)
        while (prefix < commonLength && before[prefix] == after[prefix]) prefix++
        // Keep a surrogate pair together when only its low surrogate changed.
        if (prefix > 0 && prefix < after.length && Character.isLowSurrogate(after[prefix])) prefix--

        var oldEnd = before.length
        var newEnd = after.length
        while (oldEnd > prefix && newEnd > prefix && before[oldEnd - 1] == after[newEnd - 1]) {
            oldEnd--; newEnd--
        }
        // A matching low surrogate alone is not a matching complete character.
        if (newEnd > prefix && newEnd < after.length &&
            Character.isHighSurrogate(after[newEnd - 1]) && Character.isLowSurrogate(after[newEnd])) {
            oldEnd++; newEnd++
        }
        if (newEnd == prefix) return null // deletion
        val added = after.substring(prefix, newEnd)
        return if (added.codePointCount(0, added.length) == 1 &&
            before.codePointCount(prefix, oldEnd) <= 1) added else INVALID_INPUT
    }
}

/** Small enough to roll over before the previously observed 30-character IME boundary. */
internal object FlickInputBufferPolicy {
    const val MAX_LENGTH = 1  // 1文字毎クリアで問題ないみたい
    const val IDLE_CLEAR_MS = 750L
    const val MIN_CLEAR_INTERVAL_MS = 0L  // 1文字毎クリアにしたので

    fun shouldClear(length: Int, idleMs: Long): Boolean =
        length > 0 && (length >= MAX_LENGTH || idleMs >= IDLE_CLEAR_MS)

    fun delayBeforeClear(length: Int, idleMs: Long, sinceClearMs: Long): Long =
        if (length >= MAX_LENGTH) (MIN_CLEAR_INTERVAL_MS - sinceClearMs).coerceAtLeast(0L)
        else (IDLE_CLEAR_MS - idleMs).coerceAtLeast(0L)
}

