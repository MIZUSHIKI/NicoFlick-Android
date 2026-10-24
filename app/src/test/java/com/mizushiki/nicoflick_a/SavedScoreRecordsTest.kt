package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class SavedScoreRecordsTest {
    @Test fun readsExistingFormatAndTrailingSeparator() {
        val records = readSavedScoreRecords("10,1000,2,0|11,2000,1,1|") {
            fail("Valid records must not trigger a backup")
        }
        assertEquals(mapOf(10 to arrayListOf(1000, 2, 0), 11 to arrayListOf(2000, 1, 1)), records)
    }

    @Test fun emptyStoreDoesNotNeedBackup() {
        assertTrue(readSavedScoreRecords("") { fail("Empty store is valid") }.isEmpty())
    }

    @Test fun malformedMiddleRowDoesNotHideLaterRecords() {
        var backups = 0
        val records = readSavedScoreRecords("1,10,2,0|broken|2,20,3,1|") { backups++ }
        assertEquals(setOf(1, 2), records.keys)
        assertEquals(1, backups)
    }

    @Test fun invalidIntegersAndOverflowAreSkippedAndReportedOnce() {
        var backups = 0
        val records = readSavedScoreRecords(
            "abc,10,2,0|1,x,2,0|2,10,x,0|3,10,2,x|2147483648,10,2,0|4,2147483648,2,0|5,10,2,0"
        ) { backups++ }
        assertEquals(mapOf(5 to arrayListOf(10, 2, 0)), records)
        assertEquals(1, backups)
    }

    @Test fun missingAndExtraFieldsAreRejected() {
        var backups = 0
        val records = readSavedScoreRecords("1,10,2|2,10,2,0,9|3,,2,0|4,10,2,0") { backups++ }
        assertEquals(setOf(4), records.keys)
        assertEquals(1, backups)
    }

    @Test fun preservesNumericConventionsAndLastDuplicateWins() {
        val records = readSavedScoreRecords("1,0,0,0|1,100,7,1|-2,-1,-3,-4") {
            fail("Existing integer conventions must be preserved")
        }
        assertEquals(arrayListOf(100, 7, 1), records[1])
        assertEquals(arrayListOf(-1, -3, -4), records[-2])
    }
}
