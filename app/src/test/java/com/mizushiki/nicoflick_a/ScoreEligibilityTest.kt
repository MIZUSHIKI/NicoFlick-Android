package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class ScoreEligibilityTest {
    @Test fun normalConditionsRemainEligible() {
        val score = Score()
        score.recordPlayConditions(false, 1.0f, 1.0f)
        assertFalse(score.usedNonStandardConditions)
    }

    @Test fun eachNonStandardConditionIsSticky() {
        val conditions: List<(Score) -> Unit> = listOf(
            { it.recordPlayConditions(true, 1.0f, 1.0f) },
            { it.recordPlayConditions(false, 0.5f, 1.0f) },
            { it.recordPlayConditions(false, 1.0f, 0.8f) },
            { it.recordPlayConditions(false, 1.0f, 1.0f, seeking = true) },
            { it.recordPlayConditions(false, 1.0f, 1.0f, autoPlay = true) },
            { it.recordPlayConditions(false, 1.0f, 1.0f, editing = true) }
        )
        conditions.forEach { apply ->
            val score = Score()
            apply(score)
            score.recordPlayConditions(false, 1.0f, 1.0f)
            assertTrue(score.usedNonStandardConditions)
        }
    }

    @Test fun retryClearsPointsButRetainsIneligibility() {
        val notes = Notes()
        notes.score.flickableNoteNum = 10
        notes.score.addScore(Note.GREAT)
        notes.score.markNonStandardConditions()
        notes.noteReset()
        assertEquals(0, notes.score.totalScore)
        assertTrue(notes.score.usedNonStandardConditions)
        assertFalse(Notes().score.usedNonStandardConditions)
    }

    @Test fun normalRetryRemainsEligible() {
        val notes = Notes()
        notes.noteReset()
        assertFalse(notes.score.usedNonStandardConditions)
    }
}
