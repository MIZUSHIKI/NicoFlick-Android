package com.mizushiki.nicoflick_a

/** 結果画面でのみ判定する。プレイ中の入力処理には関与しない。 */
internal object FlickOnlyReminder {
    private const val SHOWN = "FlickOnlyReminderShown_v1"
    private const val PENDING = "FlickOnlyReminderPending_v1"
    val shown: Boolean get() = USERDATA.dataStore_Util.getBoolean(SHOWN, false)
    val pending: Boolean get() = !shown && USERDATA.dataStore_Util.getBoolean(PENDING, false)

    fun inspectCompletedResult(notes: List<Note>, savedScoreCount: Int) {
        if (shown || savedScoreCount >= 20) return
        if (isSuspicious(notes)) USERDATA.dataStore_Util.edit().putBoolean(PENDING, true).commit()
    }

    fun markShown() {
        USERDATA.dataStore_Util.edit().putBoolean(SHOWN, true).putBoolean(PENDING, false).commit()
    }

    fun isSuspicious(notes: List<Note>): Boolean {
        var previousWord: String? = null
        var previousJudge = Note.NORMAL
        var safeCount = 0
        for (note in notes) {
            if (!note.isFlickable) continue
            val word = note.word.toHiragana()
            val previousWasHit = previousJudge == Note.GREAT ||
                previousJudge == Note.GOOD || previousJudge == Note.SAFE
            if (word.length == 1 && word[0] in "あかさたなはまやらわ" &&
                word == previousWord && previousWasHit && note.judge == Note.SAFE) {
                if (++safeCount >= 3) return true
            }
            previousWord = word
            previousJudge = note.judge
        }
        return false
    }
}
