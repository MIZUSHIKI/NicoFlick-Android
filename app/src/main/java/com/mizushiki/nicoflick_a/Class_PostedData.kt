package com.mizushiki.nicoflick_a

import kotlin.properties.Delegates

class scoreData {
    var sqlID:Int  by Delegates.notNull()
    var levelID:Int  by Delegates.notNull()
    var score:Int  by Delegates.notNull()
    var userID:String  by Delegates.notNull()
    var sqlUpdateTime:Int  by Delegates.notNull()
}
class commentData {
    var sqlID:Int  by Delegates.notNull()
    var levelID:Int  by Delegates.notNull()
    var comment:String  by Delegates.notNull()
    var userID:String  by Delegates.notNull()
    var sqlUpdateTime:Int  by Delegates.notNull()
}

object UserNameDataLists {
    private val db get() = LocalStorage.database()
    var usernameJsonNumCount = 0
    var usernameJsonCreateTime = 0

    fun reset() {
        db.clear("user_names")
        usernameJsonNumCount = 0
        usernameJsonCreateTime = 0
    }

    fun getUserName(userID: String): String {
        var name = "NO_NAME"
        db.forEachRow("user_names", "userID=?", arrayOf(userID)) { name = it.text("name").ifEmpty { "NO_NAME" } }
        return name
    }

    fun getLastUpdateTime(): Int = db.lastUpdate("user_names")

    fun importResponse(json: String) = db.transaction { db.importJson("user_names", json) }
}

object ScoreDataLists {
    private val db get() = LocalStorage.database()

    fun reset() = db.clear("rankings")

    fun getLastUpdateTime(levelID: Int?): Int = db.lastUpdate("rankings", levelID)

    fun getSortedScores(levelID: Int): List<scoreData> {
        val result = arrayListOf<scoreData>()
        db.forEachRow("rankings", "levelID=?", arrayOf(levelID.toString()), "score DESC, id ASC") { c ->
            result.add(scoreData().apply {
                sqlID = c.number("id")
                this.levelID = c.number("levelID")
                score = c.number("score")
                userID = c.text("userID")
                sqlUpdateTime = c.number("updateTime")
            })
        }
        return result
    }

    fun importResponse(levelID: Int, json: String) =
        db.transaction { db.importJson("rankings", json, levelID) }
}

object CommentDataLists {
    private val db get() = LocalStorage.database()

    fun reset() = db.clear("comments")

    fun getLastUpdateTime(levelID: Int?): Int = db.lastUpdate("comments", levelID)

    fun getSortedComments(levelID: Int): List<commentData> {
        val result = arrayListOf<commentData>()
        db.forEachRow("comments", "levelID=?", arrayOf(levelID.toString()), "id DESC") { c ->
            result.add(commentData().apply {
                sqlID = c.number("id")
                this.levelID = c.number("levelID")
                comment = c.text("comment")
                userID = c.text("userID")
                sqlUpdateTime = c.number("updateTime")
            })
        }
        return result
    }

    fun importResponse(levelID: Int, json: String) =
        db.transaction { db.importJson("comments", json, levelID) }
}
