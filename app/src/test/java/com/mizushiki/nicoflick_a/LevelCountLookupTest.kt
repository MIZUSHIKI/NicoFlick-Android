package com.mizushiki.nicoflick_a

import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

class LevelCountLookupTest {
    @Before fun prepare() { MusicDataLists.reset() }
    @After fun cleanUp() { MusicDataLists.reset() }

    private fun add(id: Int, url: String): levelData {
        MusicDataLists.setLevel(id, url, 3, "creator", "", 100, "", 1, 1,
            10, 20, 0, 0, 30, 40, 5, 6, 50)
        return MusicDataLists.levels[url]!!.first { it.sqlID == id }
    }

    private fun update(id: Int, plays: Int = 11, playTime: Int = 21,
                       commentTime: Int = 31, scoreTime: Int = 41,
                       likes: Int = 7, dislikes: Int = 8, likeTime: Int = 51) {
        MusicDataLists.setLevel_PlaycountLike(id, plays, playTime, commentTime,
            scoreTime, likes, dislikes, likeTime)
    }

    @Test fun calculatedLevelIsIndependentAndDefaultsToZeroOnReplacement() {
        assertEquals(0, levelData().calcLv)
        fun set(calcLv: Int = 0) {
            MusicDataLists.setLevel(1, "song", 3, "creator", "", 100, "", 1, 1,
                10, 20, 0, 0, 30, 40, calcLv = calcLv)
        }
        set(1234)
        val target = MusicDataLists.levels.getValue("song").single()
        assertEquals(1234, target.calcLv)
        assertEquals(3, target.level)
        set(950)
        assertEquals(950, target.calcLv)
        update(1)
        assertEquals(950, target.calcLv)
        set()
        assertEquals(0, target.calcLv)
        assertEquals(3, target.level)
    }

    @Test fun updatesOnlyTheRequestedLevelAcrossSongsAndSiblingLevels() {
        val otherSong = add(1, "song-a")
        val sibling = add(2, "song-b")
        val target = add(3, "song-b")
        update(3)
        assertEquals(11, target.playCount)
        assertEquals(21, target.playCountTime)
        assertEquals(31, target.commentTime)
        assertEquals(41, target.scoreTime)
        assertEquals(7, target.likeCount)
        assertEquals(8, target.dislikeCount)
        assertEquals(51, target.likeCountTime)
        for (other in listOf(otherSong, sibling)) {
            assertEquals(10, other.playCount)
            assertEquals(5, other.likeCount)
            assertEquals(30, other.commentTime)
        }
    }

    @Test fun unchangedFieldsRetainTheirValuesAndTimes() {
        val target = add(1, "song")
        update(1, -1, -1, -1, -1, -1, -1, -1)
        assertEquals(10, target.playCount)
        assertEquals(20, target.playCountTime)
        assertEquals(30, target.commentTime)
        assertEquals(40, target.scoreTime)
        assertEquals(5, target.likeCount)
        assertEquals(6, target.dislikeCount)
        assertEquals(50, target.likeCountTime)
        update(1, -1, -1, -1, -1, -1, 9, 60)
        assertEquals(5, target.likeCount)
        assertEquals(9, target.dislikeCount)
        assertEquals(60, target.likeCountTime)
    }

    @Test fun missingAndDeletedLevelsDoNotChangeRemainingRecords() {
        val remaining = add(1, "song")
        val deleted = add(2, "song")
        MusicDataLists.setLevel(2, "song", -1, "", "", 0, "", 0, 0,
            0, 0, 0, 0, 0, 0)
        assertFalse(MusicDataLists.levelsSqlIDtoMovieURL.containsKey(2))
        update(2)
        update(999)
        assertEquals(10, remaining.playCount)
        assertEquals(10, deleted.playCount)
        MusicDataLists.reset()
        update(1)
        assertTrue(MusicDataLists.levels.isEmpty())
    }

    @Test fun findsRecordsWithMissingOrIncorrectIndex() {
        val target = add(1, "target")
        add(2, "other")
        MusicDataLists.levelsSqlIDtoMovieURL.clear()
        update(1)
        assertEquals(11, target.playCount)
        MusicDataLists.levelsSqlIDtoMovieURL[1] = "other"
        update(1, plays = 12)
        assertEquals(12, target.playCount)
    }

    @Test fun stopsBeforeTraversingListsAfterTheMatch() {
        val target = add(1, "target")
        MusicDataLists.levels["unrelated"] = object : ArrayList<levelData>() {
            override fun iterator(): MutableIterator<levelData> {
                throw AssertionError("An unrelated song must not be scanned")
            }
        }
        update(1)
        assertEquals(11, target.playCount)
    }
}
