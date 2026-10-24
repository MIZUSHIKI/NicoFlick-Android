package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class FlickOnlyReminderTest {
    private fun note(word: String, judge: Int, flickable: Boolean = true) =
        Note(word).apply { this.judge = judge; isFlickable = flickable }

    @Test fun countsSafeWithinOneRunAndNormalizesKatakana() {
        assertTrue(FlickOnlyReminder.isSuspicious(listOf(
            note("あ", Note.GREAT), note("ア", Note.SAFE),
            note("あ", Note.SAFE), note("ア", Note.SAFE))))
    }

    @Test fun twoSafeAreInsufficient() {
        assertFalse(FlickOnlyReminder.isSuspicious(listOf(
            note("か", Note.GREAT), note("か", Note.SAFE), note("か", Note.SAFE))))
    }

    @Test fun excludesSafeImmediatelyAfterMissOrBad() {
        for (judge in listOf(Note.MISS, Note.BAD)) {
            assertFalse(FlickOnlyReminder.isSuspicious(listOf(
                note("あ", Note.GREAT), note("あ", Note.SAFE),
                note("あ", judge), note("あ", Note.SAFE), note("あ", Note.SAFE))))
        }
    }

    @Test fun countsSeparateRunsAndSkipsNonFlickableNotes() {
        assertTrue(FlickOnlyReminder.isSuspicious(listOf(
            note("あ", Note.GOOD), note("あ", Note.SAFE),
            note("か", Note.GREAT), note("歌", Note.NORMAL, false), note("カ", Note.SAFE),
            note("わ", Note.SAFE), note("ワ", Note.SAFE))))
    }

    @Test fun rejectsOtherCharactersAndNonSafeJudgments() {
        assertFalse(FlickOnlyReminder.isSuspicious(
            List(5) { note("い", Note.SAFE) } + List(5) { note("あ", Note.GREAT) }))
    }
}
