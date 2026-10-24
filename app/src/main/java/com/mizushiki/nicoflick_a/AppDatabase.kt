package com.mizushiki.nicoflick_a

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.JsonReader
import android.util.JsonToken
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.StringReader

/**
 * Persistent data is stored as rows, never as a JSON snapshot of the whole cache.
 * The old preference files are read only and remain available as migration backups.
 */
internal class AppDatabase(context: Context, name: String = "nicoflick.db") :
    SQLiteOpenHelper(context.applicationContext, name, null, 2) {

    private val legacyDirectory = File(context.applicationInfo.dataDir, "shared_prefs")

    init {
        // ランキング保存中も、再生音量などの設定を別スレッドから読めるようにする。
        setWriteAheadLoggingEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE settings (namespace TEXT NOT NULL, pref_key TEXT NOT NULL, type TEXT NOT NULL, value TEXT NOT NULL, PRIMARY KEY(namespace,pref_key))")
        db.execSQL("CREATE TABLE migrations (name TEXT PRIMARY KEY)")
        for ((table, columns) in schemas) {
            db.execSQL("CREATE TABLE $table (" + columns.entries.joinToString(",") { "\"${it.key}\" ${it.value} NOT NULL" } + ")")
        }
        db.execSQL("CREATE UNIQUE INDEX user_names_user ON user_names(userID)")
        db.execSQL("CREATE INDEX rankings_level_score ON rankings(levelID,score DESC)")
        db.execSQL("CREATE INDEX comments_level_id ON comments(levelID,id DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE levels ADD COLUMN calcLv INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun transaction(action: () -> Unit) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            action()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun upsert(table: String, values: ContentValues) {
        require(schemas.containsKey(table))
        writableDatabase.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE).also {
            check(it != -1L) { "Unable to save $table" }
        }
    }

    fun clear(table: String) {
        require(schemas.containsKey(table))
        writableDatabase.delete(table, null, null)
    }

    fun count(table: String): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM $table", null).use {
        it.moveToFirst(); it.getInt(0)
    }

    fun lastUpdate(table: String, levelID: Int? = null): Int {
        require(schemas.containsKey(table))
        val where = if (levelID == null) "" else " WHERE levelID=?"
        return readableDatabase.rawQuery("SELECT MAX(updateTime) FROM $table$where",
            levelID?.let { arrayOf(it.toString()) }).use { it.moveToFirst(); it.getInt(0) }
    }

    fun forEachRow(table: String, where: String? = null, args: Array<String>? = null,
                   order: String? = null, action: (Cursor) -> Unit) {
        require(schemas.containsKey(table))
        readableDatabase.query(table, null, where, args, null, null, order).use {
            while (it.moveToNext()) action(it)
        }
    }

    /** Read one object at a time, including during legacy migration. */
    fun importJson(table: String, text: String, levelID: Int? = null) {
        if (text.isBlank()) return
        JsonReader(StringReader(text)).use { reader ->
            reader.beginArray()
            while (reader.hasNext()) {
                val values = ContentValues()
                val columns = schemas.getValue(table)
                columns.forEach { (key, type) ->
                    if (type.startsWith("INTEGER")) values.put(key, 0) else values.put(key, "")
                }
                var hasID = false
                reader.beginObject()
                while (reader.hasNext()) {
                    val key = reader.nextName()
                    val type = columns[key]
                    if (type == null) {
                        reader.skipValue()
                    } else {
                        check(reader.peek() != JsonToken.NULL) { "Null $table.$key" }
                        val value = reader.nextString()
                        if (type.startsWith("INTEGER")) values.put(key, value.toInt()) else values.put(key, value)
                        if (key == "id") hasID = true
                    }
                }
                reader.endObject()
                check(hasID) { "Missing $table.id" }
                if (levelID != null) values.put("levelID", levelID)
                upsert(table, values)
            }
            reader.endArray()
            check(reader.peek() == JsonToken.END_DOCUMENT) { "Trailing data in $table" }
        }
    }

    /**
     * All records and the completion marker commit together. Failure rolls back everything;
     * the next launch retries from the unchanged XML. Split stores override older root keys.
     */
    @Synchronized
    fun migrateLegacy(onMigrationStarted: (() -> Unit)? = null) {
        transaction {
            val done = readableDatabase.rawQuery("SELECT 1 FROM migrations WHERE name='preferences-v1'", null)
                .use { it.moveToFirst() }
            if (done) return@transaction
            if (legacyStores.any {
                    File(legacyDirectory, "$it.xml").exists() || File(legacyDirectory, "$it.xml.bak").exists()
                }) {
                onMigrationStarted?.invoke()
            }
            for (namespace in legacyStores) {
                val backup = File(legacyDirectory, "$namespace.xml.bak")
                val source = if (backup.exists()) backup else File(legacyDirectory, "$namespace.xml")
                if (source.exists()) source.inputStream().use { stream ->
                    val parser = Xml.newPullParser()
                    parser.setInput(stream, "UTF-8")
                    parser.nextTag()
                    parser.require(XmlPullParser.START_TAG, null, "map")
                    while (parser.nextTag() == XmlPullParser.START_TAG) {
                        val type = parser.name
                        val key = checkNotNull(parser.getAttributeValue(null, "name"))
                        val value = when (type) {
                            "string" -> parser.nextText()
                            "set" -> {
                                val entries = linkedSetOf<String>()
                                while (parser.nextTag() == XmlPullParser.START_TAG) {
                                    parser.require(XmlPullParser.START_TAG, null, "string")
                                    entries.add(parser.nextText())
                                }
                                SQLitePreferences.encodeSet(entries)
                            }
                            "int", "long", "float", "boolean" -> {
                                val result = checkNotNull(parser.getAttributeValue(null, "value"))
                                parser.nextTag()
                                result
                            }
                            else -> error("Unsupported legacy preference type: $type")
                        }
                        val cache = legacyCaches[key]
                        if (cache != null && (namespace == "DataStore" || namespace == "DataStore_$key")) {
                            clear(cache)
                            importJson(cache, value)
                        } else {
                            writeSetting(namespace, key, type, value)
                        }
                    }
                    parser.require(XmlPullParser.END_TAG, null, "map")
                    while (parser.eventType != XmlPullParser.END_DOCUMENT) parser.next()
                }
            }
            writableDatabase.execSQL("INSERT INTO migrations(name) VALUES('preferences-v1')")
        }
    }

    fun readSetting(namespace: String, key: String): Pair<String, String>? =
        readableDatabase.query("settings", arrayOf("type", "value"), "namespace=? AND pref_key=?",
            arrayOf(namespace, key), null, null, null).use {
            if (it.moveToFirst()) it.getString(0) to it.getString(1) else null
        }

    fun writeSetting(namespace: String, key: String, type: String, value: String) {
        val values = ContentValues().apply {
            put("namespace", namespace); put("pref_key", key); put("type", type); put("value", value)
        }
        check(writableDatabase.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L)
    }

    fun removeSetting(namespace: String, key: String) {
        writableDatabase.delete("settings", "namespace=? AND pref_key=?", arrayOf(namespace, key))
    }

    fun clearSettings(namespace: String) {
        writableDatabase.delete("settings", "namespace=?", arrayOf(namespace))
    }

    companion object {
        val schemas = linkedMapOf(
        "musics" to linkedMapOf("id" to "INTEGER PRIMARY KEY", "movieURL" to "TEXT", "thumbnailURL" to "TEXT", "title" to "TEXT", "artist" to "TEXT", "movieLength" to "TEXT", "tags" to "TEXT", "updateTime" to "INTEGER", "createTime" to "INTEGER", "favorite" to "INTEGER", "favoriteTime" to "INTEGER"),
        "levels" to linkedMapOf("id" to "INTEGER PRIMARY KEY", "movieURL" to "TEXT", "level" to "INTEGER", "calcLv" to "INTEGER DEFAULT 0", "creator" to "TEXT", "description" to "TEXT", "speed" to "INTEGER", "notes" to "TEXT", "updateTime" to "INTEGER", "createTime" to "INTEGER", "playCount" to "INTEGER", "playCountTime" to "INTEGER", "favorite" to "INTEGER", "favoriteTime" to "INTEGER", "commentTime" to "INTEGER", "scoreTime" to "INTEGER", "poslike" to "INTEGER", "dislike" to "INTEGER", "likeTime" to "INTEGER"),
        "user_names" to linkedMapOf("id" to "INTEGER PRIMARY KEY", "userID" to "TEXT", "name" to "TEXT", "updateTime" to "INTEGER"),
        "rankings" to linkedMapOf("id" to "INTEGER PRIMARY KEY", "levelID" to "INTEGER", "score" to "INTEGER", "userID" to "TEXT", "updateTime" to "INTEGER"),
        "comments" to linkedMapOf("id" to "INTEGER PRIMARY KEY", "levelID" to "INTEGER", "comment" to "TEXT", "userID" to "TEXT", "updateTime" to "INTEGER")
        )
        val legacyCaches = linkedMapOf("MusicsJson" to "musics", "LevelsJson" to "levels",
            "UserNamesJson" to "user_names", "ScoresJson" to "rankings", "CommentsJson" to "comments")
        val legacyStores = listOf("DataStore", "DataStore_Util", "DataStore_UserScore") +
            legacyCaches.keys.map { "DataStore_$it" }
    }
}

internal object LocalStorage {
    @Volatile private var instance: AppDatabase? = null

    fun database(onMigrationStarted: (() -> Unit)? = null): AppDatabase = instance ?: synchronized(this) {
        instance ?: AppDatabase(GLOBAL.APPLICATIONCONTEXT).also { database ->
            try {
                database.migrateLegacy(onMigrationStarted)
                instance = database
            } catch (error: Throwable) {
                database.close()
                throw error
            }
        }
    }
}
