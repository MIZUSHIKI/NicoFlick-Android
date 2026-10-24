package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class FlickTextChangeTest {
    @Test fun composingGrowthIsOneInputAndConfirmationIsNotAnotherInput() {
        assertEquals("あ", FlickTextChange.detect("", "あ"))
        assertEquals("い", FlickTextChange.detect("あ", "あい"))
        assertNull(FlickTextChange.detect("あい", "あい"))
        assertNull(FlickTextChange.detect("あい", "あい", "あい", "あい"))
    }

    @Test fun repeatedCommitOnlyCharactersAreSeparateInputs() {
        assertEquals("あ", FlickTextChange.detect("", "あ"))
        assertEquals("あ", FlickTextChange.detect("あ", "ああ"))
        assertEquals("あ", FlickTextChange.detect("ああ", "あああ"))
    }

    @Test fun appendDuringCommitCountsOnlyThePreviouslyUnreportedCharacter() {
        assertEquals("い", FlickTextChange.detect("あ", "あい", "あ", "あい"))
    }

    @Test fun candidateConversionDoesNotJudgeAlreadyTypedCharactersAgain() {
        assertNull(FlickTextChange.detect("きょう", "今日", "きょう", "今日"))
        assertNull(FlickTextChange.detect("か", "蚊", "か", "蚊"))
    }

    @Test fun deletionAndEmptyCompositionDoNotScoreNotes() {
        assertNull(FlickTextChange.detect("あい", "あ"))
        assertNull(FlickTextChange.detect("あい", ""))
        assertNull(FlickTextChange.detect("あいう", "あう"))
        assertNull(FlickTextChange.detect("", ""))
    }

    @Test fun oneCharacterReplacementAndInsertionUseTheChangedCharacter() {
        assertEquals("が", FlickTextChange.detect("か", "が"))
        assertEquals("う", FlickTextChange.detect("あい", "あうい"))
        assertEquals("え", FlickTextChange.detect("あいう", "あえう"))
    }

    @Test fun pastedOrPredictedTextIsOneInvalidInputInsteadOfSeveralPerfectNotes() {
        assertEquals(FlickTextChange.INVALID_INPUT, FlickTextChange.detect("", "あいう"))
        assertEquals(FlickTextChange.INVALID_INPUT, FlickTextChange.detect("あ", "あいう"))
        assertEquals(FlickTextChange.INVALID_INPUT, FlickTextChange.detect("きょう", "今日"))
    }

    @Test fun supplementaryCharactersAreNeverSplitIntoSurrogates() {
        assertEquals("😀", FlickTextChange.detect("", "😀"))
        assertEquals("😁", FlickTextChange.detect("😀", "😁"))
        val sameLowSurrogate = String(charArrayOf(0xD83E.toChar(), 0xDE00.toChar()))
        assertEquals(sameLowSurrogate, FlickTextChange.detect("😀", sameLowSurrogate))
        assertEquals("😀", FlickTextChange.detect("😀", "😀😀"))
        assertNull(FlickTextChange.detect("あ😀", "あ"))
        assertEquals(FlickTextChange.INVALID_INPUT, FlickTextChange.detect("", "😀😁"))
    }

    @Test fun uninterruptedComposingAndCommitOnlySequencesHaveNoDuplicateInputs() {
        val lyric = "あいうえおかきくけこさしすせそたちつてとなにぬねの"
        var composing = ""
        var judged = ""
        for (word in lyric) {
            val next = composing + word
            judged += FlickTextChange.detect(composing, next) ?: ""
            assertNull(FlickTextChange.detect(next, next, next, next))
            composing = next
        }
        assertEquals(lyric, judged)
        var committed = ""
        judged = ""
        for (word in lyric) {
            val next = committed + word
            judged += FlickTextChange.detect(committed, next) ?: ""
            committed = next
        }
        assertEquals(lyric, judged)
    }

    @Test fun identicalCharactersAfterEachClearAreSeparateInputs() {
        for (word in listOf("あ", "か", "い")) {
            repeat(3) { assertEquals(word, FlickTextChange.detect("", word)) }
        }
    }

    @Test fun oneCharacterBufferClearsImmediately() {
        assertEquals(1, FlickInputBufferPolicy.MAX_LENGTH)
        assertEquals(0L, FlickInputBufferPolicy.delayBeforeClear(1, 0, 0))
        assertTrue(FlickInputBufferPolicy.shouldClear(1, 0))
    }

    @Test fun bufferPolicyClearsContinuousInputBeforeThirtyCharacters() {
        for (length in 1 until FlickInputBufferPolicy.MAX_LENGTH) {
            assertFalse(FlickInputBufferPolicy.shouldClear(length, 0))
        }
        assertTrue(FlickInputBufferPolicy.shouldClear(24, 0))
        assertTrue(FlickInputBufferPolicy.shouldClear(30, 0))
    }

    @Test fun bufferPolicyClearsAfterIdleButNeverClearsAnEmptyBuffer() {
        assertTrue(FlickInputBufferPolicy.shouldClear(1, 749))
        assertTrue(FlickInputBufferPolicy.shouldClear(1, 750))
        assertTrue(FlickInputBufferPolicy.shouldClear(1, 1000))
        assertFalse(FlickInputBufferPolicy.shouldClear(0, 1000))
    }
}
