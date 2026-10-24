package com.mizushiki.nicoflick_a

import android.content.ContentValues
import android.database.Cursor

internal fun Cursor.text(name: String): String = getString(getColumnIndexOrThrow(name))
internal fun Cursor.number(name: String): Int = getInt(getColumnIndexOrThrow(name))

/** Only music/level metadata needed for the selector is kept in memory. */
internal object MetadataStorage {
    private val db get() = LocalStorage.database()

    fun load() {
        if (MusicDataLists.musics.isEmpty()) {
            db.forEachRow("musics", order = "rowid") { c ->
                MusicDataLists.setMusic(
                    sqlID = c.number("id"),
                    movieURL = c.text("movieURL"),
                    thumbnailURL = c.text("thumbnailURL"),
                    title = c.text("title"),
                    artist = c.text("artist"),
                    movieLength = c.text("movieLength"),
                    tags = c.text("tags"),
                    updateTime = c.number("updateTime"),
                    createTime = c.number("createTime"),
                    favoriteCount = c.number("favorite"),
                    favoriteCountTime = c.number("favoriteTime")
                )
            }
        }
        if (MusicDataLists.levels.isEmpty()) {
            db.forEachRow("levels", order = "rowid") { c ->
                MusicDataLists.setLevel(
                    sqlID = c.number("id"),
                    movieURL = c.text("movieURL"),
                    level = c.number("level"),
                    creator = c.text("creator"),
                    description = c.text("description"),
                    speed = c.number("speed"),
                    noteData = c.text("notes"),
                    updateTime = c.number("updateTime"),
                    createTime = c.number("createTime"),
                    playCount = c.number("playCount"),
                    playCountTime = c.number("playCountTime"),
                    favoriteCount = c.number("favorite"),
                    favoriteCountTime = c.number("favoriteTime"),
                    commentTime = c.number("commentTime"),
                    scoreTime = c.number("scoreTime"),
                    likeCount = c.number("poslike"),
                    dislikeCount = c.number("dislike"),
                    likeCountTime = c.number("likeTime"),
                    calcLv = c.number("calcLv")
                )
            }
        }
        MusicDataLists.createTaglist()
    }

    fun saveMusics() = db.transaction {
        db.clear("musics")
        MusicDataLists.musics.forEach { music -> saveMusic(music) }
    }

    fun saveLevels() = db.transaction {
        db.clear("levels")
        MusicDataLists.levels.forEach { (url, levels) -> levels.forEach { saveLevel(url, it) } }
    }

    fun saveMusic(music: musicData) {
        db.upsert("musics", ContentValues().apply {
            put("id", music.sqlID)
            put("movieURL", music.movieURL)
            put("thumbnailURL", music.thumbnailURL)
            put("title", music.title)
            put("artist", music.artist)
            put("movieLength", music.movieLength)
            put("tags", music.tags)
            put("updateTime", music.sqlUpdateTime)
            put("createTime", music.sqlCreateTime)
            put("favorite", music.favoriteCount)
            put("favoriteTime", music.favoriteCountTime)
        })
    }

    fun saveLevel(movieURL: String, level: levelData) {
        db.upsert("levels", ContentValues().apply {
            put("id", level.sqlID)
            put("movieURL", movieURL)
            put("level", level.level)
            put("calcLv", level.calcLv)
            put("creator", level.creator)
            put("description", level.description)
            put("speed", level.speed)
            put("notes", level.noteData)
            put("updateTime", level.sqlUpdateTime)
            put("createTime", level.sqlCreateTime)
            put("playCount", level.playCount)
            put("playCountTime", level.playCountTime)
            put("favorite", level.favoriteCount)
            put("favoriteTime", level.favoriteCountTime)
            put("commentTime", level.commentTime)
            put("scoreTime", level.scoreTime)
            put("poslike", level.likeCount)
            put("dislike", level.dislikeCount)
            put("likeTime", level.likeCountTime)
        })
    }

    fun clearMusicsAndLevels() = db.transaction { db.clear("musics"); db.clear("levels") }
}
