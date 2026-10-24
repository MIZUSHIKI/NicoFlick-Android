package com.mizushiki.nicoflick_a

import com.eclipsesource.json.Json
import org.junit.Assert.*
import org.junit.Test

class MusicScoreResponseTest {
    private fun parse(text: String) = parseMusicScoreResponse(Json.parse(text).asArray())

    @Test fun acceptsStringAndNumberIntegersAndConvertsSeconds() {
        val records = parse("""[{"score":"123","updateTime":"1700000000","scoreYear":42,"updateTimeYear":1700000001}]""")!!
        assertEquals(listOf("全期間", "年間"), records.map { it.label })
        assertEquals(123L, records[0].score)
        assertEquals(1700000000000L, records[0].updatedAtMillis)
        assertEquals(42L, records[1].score)
    }

    @Test fun explicitNullDatesMeanNoRecords() {
        assertEquals(emptyList<MusicScoreRecord>(), parse("""[{"updateTime":null,"updateTimeYear":null}]"""))
    }

    @Test fun missingResponseEmptyArrayAndWrongShapeAreErrors() {
        assertNull(parseMusicScoreResponse(null))
        for (text in listOf("[]", "[null]", "[1]", "[[]]", "[{}]", "[{},{}]")) {
            assertNull(text, parse(text))
        }
    }

    @Test fun incompleteRecordIsAnError() {
        for (text in listOf("""[{"score":"10"}]""", """[{"updateTime":"1"}]""",
            """[{"updateTime":"1","score":null}]""")) assertNull(text, parse(text))
    }

    @Test fun rejectsWrongTypesDecimalsAndOverflow() {
        for (value in listOf("true", "{}", "[]", "1.5", "\"\"",
            "\"abc\"", "\"9223372036854775808\"")) {
            assertNull(value, parse("""[{"updateTime":"1","score":$value}]"""))
            assertNull(value, parse("""[{"updateTime":$value,"score":"10"}]"""))
        }
    }

    @Test fun rejectsNegativeScoresAndInvalidDateRange() {
        assertNull(parse("""[{"updateTime":"1","score":"-1"}]"""))
        assertNull(parse("""[{"updateTime":"-1","score":"10"}]"""))
        assertNull(parse("""[{"updateTime":"9223372036854775807","score":"10"}]"""))
    }

    @Test fun badLaterPeriodDoesNotReturnPartialRecords() {
        assertNull(parse("""[{"updateTime":"1","score":"10","updateTimeWeek":"2"}]"""))
    }
}
