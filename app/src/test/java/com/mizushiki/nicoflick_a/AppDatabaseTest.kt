package com.mizushiki.nicoflick_a

import android.app.Application
import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class AppDatabaseTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var databaseName: String
    private lateinit var legacy: File

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
        databaseName = "migration-" + UUID.randomUUID() + ".db"
        legacy = File(context.applicationInfo.dataDir, "shared_prefs")
        legacy.deleteRecursively()
        legacy.mkdirs()
        db = AppDatabase(context, databaseName)
    }

    @After fun tearDown() {
        db.close()
        context.deleteDatabase(databaseName)
        legacy.deleteRecursively()
    }

    private fun xml(namespace: String, contents: String) {
        File(legacy, "$namespace.xml").writeText("<?xml version='1.0' encoding='utf-8'?><map>$contents</map>")
    }

    private fun jsonPreference(namespace: String, key: String, json: String) {
        val escaped = json.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        xml(namespace, "<string name='$key'>$escaped</string>")
    }

    @Test fun migratesSettingsPersonalRecordsAndAllFiveCachesAndKeepsOriginalFiles() {
        xml("DataStore", """<int name="MyVersion" value="1994"/>
            <string name="UniqueUserID">old-user</string>
            <string name="UserName">旧名前 &amp; 日本語</string>
            <float name="SoundVolumeMovie" value="0.25"/>
            <boolean name="thumbMoviePlay" value="false"/>""")
        xml("DataStore_UserScore", """<string name="UserScore">8,12345,2,0|</string>""")
        xml("DataStore_Util", """<string name="MyFavorite3">1,9</string>
            <string name="MyLike">8,-2|9,1</string>
            <string name="levelPlayHistories">8,4,1234567890000</string>
            <string name="JustPlayedScore">8,12000,3,1|</string>
            <int name="UserNamesServerJsonNumCount" value="-1"/>
            <set name="testSet"><string>A</string><string>日本語</string></set>""")
        jsonPreference("DataStore_MusicsJson", "MusicsJson",
            """[{"id":"1","movieURL":"https://example/watch/sm1","thumbnailURL":"https://image","title":"楽曲","artist":"作者","movieLength":"3:00","tags":"タグ","updateTime":"12","createTime":"3"}]""")
        jsonPreference("DataStore_LevelsJson", "LevelsJson",
            """[{"id":"8","movieURL":"https://example/watch/sm1","level":"4","creator":"作者","description":"説明","speed":"100","notes":"[00:01.00]あ","updateTime":"12","createTime":"3","playCount":"5"}]""")
        jsonPreference("DataStore_UserNamesJson", "UserNamesJson",
            """[{"id":"2","userID":"other","name":"名前","updateTime":"10"}]""")
        jsonPreference("DataStore_ScoresJson", "ScoresJson",
            """[{"id":"3","levelID":"8","score":"123","userID":"other","updateTime":"11"}]""")
        jsonPreference("DataStore_CommentsJson", "CommentsJson",
            """[{"id":"4","levelID":"8","comment":"改行\n引用\"と日本語","userID":"other","updateTime":"13"}]""")
        val originals = legacy.listFiles()!!.associate { it.name to it.readText() }

        db.migrateLegacy()

        val settings = SQLitePreferences(db, "DataStore")
        assertEquals("old-user", settings.getString("UniqueUserID", ""))
        assertEquals("旧名前 & 日本語", settings.getString("UserName", ""))
        assertEquals(1994, settings.getInt("MyVersion", 0))
        assertEquals(0.25f, settings.getFloat("SoundVolumeMovie", 1f), 0f)
        assertFalse(settings.getBoolean("thumbMoviePlay", true))
        assertEquals("8,12345,2,0|", SQLitePreferences(db, "DataStore_UserScore").getString("UserScore", ""))
        val util = SQLitePreferences(db, "DataStore_Util")
        assertEquals("1,9", util.getString("MyFavorite3", ""))
        assertEquals("8,-2|9,1", util.getString("MyLike", ""))
        assertEquals("8,4,1234567890000", util.getString("levelPlayHistories", ""))
        assertEquals("8,12000,3,1|", util.getString("JustPlayedScore", ""))
        assertEquals(-1, util.getInt("UserNamesServerJsonNumCount", 0))
        assertEquals(setOf("A", "日本語"), util.getStringSet("testSet", null))
        AppDatabase.schemas.keys.forEach { assertEquals(it, 1, db.count(it)) }
        db.forEachRow("levels") {
            assertEquals("[00:01.00]あ", it.text("notes"))
            assertEquals(0, it.number("likeTime"))
        }
        db.forEachRow("comments") { assertEquals("改行\n引用\"と日本語", it.text("comment")) }
        assertEquals(originals, legacy.listFiles()!!.associate { it.name to it.readText() })

        // New values survive reopening, even though the old XML is deliberately retained.
        settings.edit().putString("UserName", "新しい名前").commit()
        db.close()
        db = AppDatabase(context, databaseName)
        db.migrateLegacy()
        assertEquals("新しい名前", SQLitePreferences(db, "DataStore").getString("UserName", ""))
        assertEquals(1, db.count("rankings"))
    }

    @Test fun upgradesVersionOneWithoutLosingLevelsOrSettings() {
        db.close()
        context.openOrCreateDatabase(databaseName, 0, null).use { old ->
            old.execSQL("CREATE TABLE levels (id INTEGER PRIMARY KEY, level INTEGER NOT NULL, updateTime INTEGER NOT NULL)")
            old.execSQL("INSERT INTO levels VALUES(8,4,123)")
            old.execSQL("CREATE TABLE settings (namespace TEXT, pref_key TEXT, type TEXT, value TEXT)")
            old.execSQL("INSERT INTO settings VALUES('DataStore','UserName','string','keep')")
            old.version = 1
        }
        db = AppDatabase(context, databaseName)
        db.forEachRow("levels") {
            assertEquals(8, it.number("id"))
            assertEquals(4, it.number("level"))
            assertEquals(123, it.number("updateTime"))
            assertEquals(0, it.number("calcLv"))
        }
        assertEquals("keep", db.readSetting("DataStore", "UserName")!!.second)
        assertEquals(2, db.readableDatabase.version)
        db.close()
        db = AppDatabase(context, databaseName)
        assertEquals(1, db.count("levels"))
    }

    @Test fun importsOptionalCalculatedLevelAndPreservesIntegersOnReopen() {
        db.importJson("levels", """[{"id":"1","level":"4"},{"id":"2","level":"5","calcLv":"1234"},{"id":"3","level":"6","calcLv":950}]""")
        db.close()
        db = AppDatabase(context, databaseName)
        val expected = mapOf(1 to 0, 2 to 1234, 3 to 950)
        db.forEachRow("levels") {
            assertEquals(expected.getValue(it.number("id")),
                it.number("calcLv"))
            assertEquals(it.number("id") + 3, it.number("level"))
        }
    }

    @Test fun failedMigrationRollsBackAndCanBeRetriedWithoutLosingLegacyData() {
        xml("DataStore", """<string name="UniqueUserID">keep-me</string>""")
        jsonPreference("DataStore_ScoresJson", "ScoresJson",
            """[{"id":"1","levelID":"8","score":"10"},{"id":"2","levelID":"8","score":"broken"}]""")
        val original = File(legacy, "DataStore_ScoresJson.xml").readText()
        try {
            db.migrateLegacy()
            fail("Invalid legacy data must not be marked as migrated")
        } catch (expected: NumberFormatException) { }
        assertNull(db.readSetting("DataStore", "UniqueUserID"))
        assertEquals(0, db.count("rankings"))
        assertEquals(original, File(legacy, "DataStore_ScoresJson.xml").readText())
        jsonPreference("DataStore_ScoresJson", "ScoresJson",
            """[{"id":"1","levelID":"8","score":"10","userID":"u","updateTime":"1"}]""")
        db.migrateLegacy()
        assertEquals("keep-me", SQLitePreferences(db, "DataStore").getString("UniqueUserID", ""))
        assertEquals(1, db.count("rankings"))
    }

    @Test fun splitStoreTakesPrecedenceAndAndroidBackupIsPreferred() {
        jsonPreference("DataStore", "ScoresJson",
            """[{"id":"1","levelID":"8","score":"1","userID":"u","updateTime":"1"}]""")
        jsonPreference("DataStore_ScoresJson", "ScoresJson",
            """[{"id":"2","levelID":"8","score":"2","userID":"u","updateTime":"2"}]""")
        File(legacy, "DataStore_ScoresJson.xml").copyTo(File(legacy, "DataStore_ScoresJson.xml.bak"))
        jsonPreference("DataStore_ScoresJson", "ScoresJson", "[]")
        db.migrateLegacy()
        assertEquals(1, db.count("rankings"))
        db.forEachRow("rankings") { assertEquals(2, it.number("id")) }
    }

    @Test fun rankingDeltaUpdatesOnlyItsRowsAndRollsBackInvalidResponses() {
        db.migrateLegacy()
        db.transaction {
            db.importJson("rankings", """[{"id":"1","score":"100","userID":"a","updateTime":"1"}]""", 8)
            db.importJson("rankings", """[{"id":"2","score":"200","userID":"b","updateTime":"2"}]""", 9)
        }
        db.transaction {
            db.importJson("rankings", """[{"id":"1","score":"300","userID":"a","updateTime":"3"}]""", 8)
        }
        assertEquals(2, db.count("rankings"))
        assertEquals(3, db.lastUpdate("rankings", 8))
        assertEquals(2, db.lastUpdate("rankings", 9))
        db.forEachRow("rankings", "levelID=?", arrayOf("8")) { assertEquals(300, it.number("score")) }
        try {
            db.transaction {
                db.importJson("rankings", """[{"id":"1","score":"999","userID":"a","updateTime":"4"},{"id":"3","score":"bad"}]""", 8)
            }
            fail("Partial response must roll back")
        } catch (expected: NumberFormatException) { }
        assertEquals(3, db.lastUpdate("rankings", 8))
        assertEquals(2, db.count("rankings"))
    }

    @Test fun clearingCacheDoesNotImportOldJsonAgainOnNextLaunch() {
        jsonPreference("DataStore_ScoresJson", "ScoresJson",
            """[{"id":"1","levelID":"8","score":"1","userID":"u","updateTime":"1"}]""")
        db.migrateLegacy()
        db.clear("rankings")
        db.close()
        db = AppDatabase(context, databaseName)
        db.migrateLegacy()
        assertEquals(0, db.count("rankings"))
        assertTrue(File(legacy, "DataStore_ScoresJson.xml").exists())
    }

    @Test fun migratesLargeRankingWithoutBuildingJsonObjectTree() {
        val records = (1..20000).joinToString(",") {
            """{"id":"$it","levelID":"8","score":"$it","userID":"user$it","updateTime":"$it"}"""
        }
        jsonPreference("DataStore_ScoresJson", "ScoresJson", "[$records]")
        db.migrateLegacy()
        assertEquals(20000, db.count("rankings"))
        assertEquals(20000, db.lastUpdate("rankings", 8))
    }
}
