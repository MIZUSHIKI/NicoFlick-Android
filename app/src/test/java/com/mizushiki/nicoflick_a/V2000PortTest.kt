package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class V2000PortTest {
    @Test fun acknowledgmentRetainsChangesMadeDuringRequest() {
        val data = mutableMapOf<String, MutableMap<Int,Int>>()
        val read:(String) -> MutableMap<Int,Int> = { data[it]?.toMutableMap() ?: mutableMapOf() }
        val write:(String, Map<Int,Int>) -> Unit = { key, value -> data[key] = value.toMutableMap() }
        val likes = PendingCounter("LikeCount", read, write)
        likes.changeLikeCount(12, 2)
        val sent = likes.counter.toMap()
        likes.changeLikeCount(12, -1)
        likes.acknowledge(sent)
        assertEquals(mapOf(12 to -1), likes.counter)
        likes.changeLikeCount(12, 1)
        assertTrue(likes.counter.isEmpty())
        likes.changeLikeCount(3, -5)
        assertEquals("<3,-5>", likes.getSendLikeCountStr())
        assertTrue(PendingCounter("DislikeCount", read, write).counter.isEmpty())
    }
    @Test fun canceledFavoriteDoesNotSendAChange() {
        var data = mutableMapOf<Int,Int>()
        val counter = PendingCounter("MusicFavoriteCount", { data.toMutableMap() }, { _, value -> data = value.toMutableMap() })
        counter.addMusicFavoriteCount(7); counter.subMusicFavoriteCount(7)
        assertEquals("", counter.getSendMusicFavoriteCountStr())
    }
    @Test fun practiceRejudgmentDoesNotDuplicatePoints() {
        val score = Score(); score.flickableNoteNum = 100
        repeat(15) { score.addScore(Note.GREAT) }
        val before = score.totalScore
        val previousCombo = score.comboCounter
        score.subScore(Note.GREAT, previousCombo)
        score.comboCounter = previousCombo - 1
        score.addScore(Note.GREAT)
        assertEquals(before, score.totalScore)
    }
    @Test fun gaugeCannotBecomeNegative() {
        val score = Score(); score.flickableNoteNum = 1
        repeat(3) { score.addScore(Note.MISS) }
        assertEquals(0.0, score.borderScore, 0.0)
    }
    @Test fun wordJudgmentUsesSameKanaNormalizationForAutoplayAndInput() {
        val note = Note("ガ")
        assertEquals("か", note.judgeWorder(note.word))
        assertEquals(Note.NORMAL, note.judgeWord("か"))
        assertEquals(Note.SAFE, note.judgeWord("き"))
        assertEquals(Note.BAD, note.judgeWord("さ"))
        assertEquals(Note.NORMAL, Note("ァ").judgeWord("あ"))
    }
}
