package com.mizushiki.nicoflick_a

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Small settings retain the existing USERDATA API, backed by typed SQLite entries.
 * An editor commits all of its changes in one database transaction.
 */
class SQLitePreferences internal constructor(private val db: AppDatabase, private val namespace: String) {
    private fun value(key: String, type: String): String? {
        val entry = db.readSetting(namespace, key) ?: return null
        check(entry.first == type) { "Wrong type for $namespace.$key" }
        return entry.second
    }
    fun contains(key: String) = db.readSetting(namespace, key) != null
    fun getString(key: String, default: String?): String? {
        if (!SelectorDiagnostics.ENABLED || key !in diagnosticKeys) return value(key, "string") ?: default
        return SelectorDiagnostics.measure("storage.read $namespace.$key") {
            (value(key, "string") ?: default).also {
                SelectorDiagnostics.event { "STORAGE_SIZE $namespace.$key chars=${it?.length ?: 0}" }
            }
        }
    }

    // TEMPORARY investigation: whitelist prevents logging credentials or arbitrary values.
    private val diagnosticKeys by lazy {
        setOf("UserScore", "JustPlayedScore", "ScoreLevel", "JustPlayedScoreLevel",
            "levelPlayHistories", "PlayCount", "LikeCount", "DislikeCount", "MusicFavoriteCount")
    }
    fun getInt(key: String, default: Int): Int = value(key, "int")?.toInt() ?: default
    fun getLong(key: String, default: Long): Long = value(key, "long")?.toLong() ?: default
    fun getFloat(key: String, default: Float): Float = value(key, "float")?.toFloat() ?: default
    fun getBoolean(key: String, default: Boolean): Boolean = value(key, "boolean")?.toBoolean() ?: default
    fun getStringSet(key: String, default: Set<String>?): Set<String>? =
        value(key, "set")?.let { decodeSet(it) } ?: default
    fun edit() = Editor()

    inner class Editor {
        private val changes = linkedMapOf<String, Pair<String, String>?>()
        private var clearAll = false
        private fun put(key: String, type: String, value: String): Editor {
            changes[key] = type to value
            return this
        }
        fun putString(key: String, value: String?): Editor =
            if (value == null) remove(key) else put(key, "string", value)
        fun putInt(key: String, value: Int) = put(key, "int", value.toString())
        fun putLong(key: String, value: Long) = put(key, "long", value.toString())
        fun putFloat(key: String, value: Float) = put(key, "float", value.toString())
        fun putBoolean(key: String, value: Boolean) = put(key, "boolean", value.toString())
        fun putStringSet(key: String, value: Set<String>?): Editor =
            if (value == null) remove(key) else put(key, "set", encodeSet(value))
        fun remove(key: String): Editor { changes[key] = null; return this }
        fun clear(): Editor { clearAll = true; return this }
        fun commit(): Boolean {
            db.transaction {
                if (clearAll) db.clearSettings(namespace)
                changes.forEach { (key, entry) ->
                    if (entry == null) db.removeSetting(namespace, key)
                    else db.writeSetting(namespace, key, entry.first, entry.second)
                }
            }
            changes.clear()
            clearAll = false
            return true
        }
        fun apply() { commit() }
    }

    companion object {
        internal fun encodeSet(values: Set<String>): String {
            val output = ByteArrayOutputStream()
            DataOutputStream(output).use { stream ->
                stream.writeInt(values.size)
                values.forEach {
                    val bytes = it.toByteArray(Charsets.UTF_8)
                    stream.writeInt(bytes.size)
                    stream.write(bytes)
                }
            }
            return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
        private fun decodeSet(value: String): Set<String> =
            DataInputStream(ByteArrayInputStream(Base64.decode(value, Base64.NO_WRAP))).use { stream ->
                val result = linkedSetOf<String>()
                repeat(stream.readInt()) {
                    val bytes = ByteArray(stream.readInt())
                    stream.readFully(bytes)
                    result.add(String(bytes, Charsets.UTF_8))
                }
                result
            }
    }
}
