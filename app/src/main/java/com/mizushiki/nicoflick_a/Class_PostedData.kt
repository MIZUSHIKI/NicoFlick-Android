package com.mizushiki.nicoflick_a

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import kotlin.properties.Delegates

class userNameData {
    var sqlID:Int  by Delegates.notNull()
    var name:String  by Delegates.notNull()
    var sqlUpdateTime:Int  by Delegates.notNull()
}
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
    var userNames:MutableMap<String,userNameData> = mutableMapOf()

    fun setUserName(sqlID:Int, userID:String, userName:String, updateTime:Int){
        if (userNames[userID] != null) {
            userNames[userID]?.name = userName
            userNames[userID]?.sqlUpdateTime = updateTime
            return
        }
        val usernamedata = userNameData()
        usernamedata.sqlID = sqlID
        usernamedata.name = userName
        usernamedata.sqlUpdateTime = updateTime

        userNames[userID] = usernamedata
    }

    fun getUserName(userID:String) : String {
        return userNames[userID]?.name ?: "NO_NAME"
    }

    fun getLastUpdateTime() : Int {
        var time = 0
        for( (_,userName) in userNames ){
            if( time < userName.sqlUpdateTime ){
                time = userName.sqlUpdateTime
            }
        }
        return time
    }

    // username Json化。 USERDATAに保存用
    fun toUserNameJsonString() : String{
        //sqlID:Int, userID:String, userName:String, updateTime:Int
        val jArrary = Json.array()
        for( (userID,usernamedata) in this.userNames ){
            val jObject = Json.`object`()
                .add("id",usernamedata.sqlID.toString())
                .add("userID",userID)
                .add("name",usernamedata.name)
                .add("updateTime",usernamedata.sqlUpdateTime.toString())

            jArrary.add(jObject)
        }
        println("json output")
        println(jArrary.toString())

        return jArrary.toString()
    }
    // 保存データから読み込み
    fun loadUserNameJsonString(jsonStr:String){
        if(jsonStr==""){ return }
        val jsonArray: JsonArray
        try {
            jsonArray = Json.parse(jsonStr).asArray()
        } catch (e: Exception) {
            println(e)
            return
        }
        // ロードした musicデータを処理
        for (i in 0 until jsonArray.size()) {
            val json = jsonArray.get(i).asObject()
            this.setUserName(
                sqlID = json.get("id").asString().toInt(),
                userID = json.get("userID").asString(),
                userName = json.get("name").asString(),
                updateTime = json.get("updateTime").asString().toInt()
            )
        }
    }
}

object ScoreDataLists {
    var scores:ArrayList<scoreData> = arrayListOf()

    fun setScore(sqlID:Int, levelID:Int, score:Int, userID:String, updateTime:Int) {
        for( scoredata in scores ){
            if( scoredata.sqlID == sqlID ){
                scoredata.levelID = levelID
                scoredata.score = score
                scoredata.userID = userID
                scoredata.sqlUpdateTime = updateTime
                return
            }
        }
        val scoredata = scoreData()
        scoredata.sqlID = sqlID
        scoredata.levelID = levelID
        scoredata.score = score
        scoredata.userID = userID
        scoredata.sqlUpdateTime = updateTime
        scores.add(scoredata)
    }
    fun getLastUpdateTime() : Int {
        var time = 0
        for( scoredata in scores ){
            if( time < scoredata.sqlUpdateTime ){
                time = scoredata.sqlUpdateTime
            }
        }
        return time
    }
}
object CommentDataLists {
    var comments:ArrayList<commentData> = arrayListOf()

    fun setComment(sqlID:Int, levelID:Int, comment:String, userID:String, updateTime:Int) {
        for( commentdata in comments ){
            if( commentdata.sqlID == sqlID ){
                commentdata.levelID = levelID
                commentdata.comment = comment
                commentdata.userID = userID
                commentdata.sqlUpdateTime = updateTime
                return
            }
        }
        val commentdata = commentData()
        commentdata.sqlID = sqlID
        commentdata.levelID = levelID
        commentdata.comment = comment
        commentdata.userID = userID
        commentdata.sqlUpdateTime = updateTime
        comments.add(commentdata)
    }
    fun getLastUpdateTime() : Int {
        var time = 0
        for( commentdata in comments ){
            if( time < commentdata.sqlUpdateTime ){
                time = commentdata.sqlUpdateTime
            }
        }
        return time
    }
}