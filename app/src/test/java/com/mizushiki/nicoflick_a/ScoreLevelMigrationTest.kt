package com.mizushiki.nicoflick_a

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreLevelMigrationTest {
    @Test fun usesCurrentDifficultyAndDefaultsMissingChartsToFull() {
        assertEquals(mapOf(1 to 7, 2 to 100), migratedScoreLevels(setOf(1, 2), emptyMap(), mapOf(1 to 7)))
    }

    @Test fun allLegacyExceptionsAreFullEvenWhenDifficultyHasChanged() {
        val ids = setOf(
            6751, 9009, 4533, 3346, 3575, 3831, 794, 3143, 3145, 3322, 3500, 3501,
            3523, 3524, 4919, 5253, 6370, 7374, 4035, 8344, 8190, 7684, 6082, 1486,
            2745, 3925, 6376, 6462, 7342, 7343, 7845, 8973, 8970, 9481, 9477, 9005,
            9007, 6459, 708, 704, 688, 5985, 6325, 4423, 7580, 5522
        )
        assertEquals(ids.associateWith { 100 }, migratedScoreLevels(ids, emptyMap(), ids.associateWith { 5 }))
    }

    @Test fun preservesRecordedDifficultyOnRepeatedMigration() {
        val saved = mapOf(6370 to 8, 1 to 3)
        assertEquals(saved, migratedScoreLevels(saved.keys, saved, mapOf(6370 to 10, 1 to 9)))
    }

    @Test fun migratesEachScoreCollectionIndependently() {
        val current = mapOf(1 to 3, 2 to 8)
        assertEquals(mapOf(1 to 3), migratedScoreLevels(setOf(1), emptyMap(), current))
        assertEquals(mapOf(2 to 8), migratedScoreLevels(setOf(2), emptyMap(), current))
    }
}
