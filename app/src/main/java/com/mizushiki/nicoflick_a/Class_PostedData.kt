package com.mizushiki.nicoflick_a

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import java.util.*
import kotlin.collections.ArrayList
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

    //初期データ取得用管理用フラグ
    var usernameJsonNumCount = 0
    var usernameJsonCreateTime = 0

    fun reset(){
        userNames = mutableMapOf()
        usernameJsonNumCount = 0
        usernameJsonCreateTime = 0
    }

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
        val name = userNames[userID]?.name ?: "NO_NAME"
        if( name == "" ){
            return "NO_NAME"
        }
        return name
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
        //println("json output")
        //println(jArrary.toString())
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
    var SqlIDtoIndex:MutableMap<Int,Int> = mutableMapOf() //最初からscores:MutableMap<Int,scoreData>にしとけば良かった

    fun reset(){
        scores = arrayListOf()
        SqlIDtoIndex = mutableMapOf()
    }

    fun setScore(sqlID:Int, levelID:Int, score:Int, userID:String, updateTime:Int) {
        SqlIDtoIndex[sqlID]?.let {
            scores[it].levelID = levelID
            scores[it].score = score
            scores[it].userID = userID
            scores[it].sqlUpdateTime = updateTime
            return
        }
        SqlIDtoIndex[sqlID] = scores.count()
        val scoredata = scoreData()
        scoredata.sqlID = sqlID
        scoredata.levelID = levelID
        scoredata.score = score
        scoredata.userID = userID
        scoredata.sqlUpdateTime = updateTime
        scores.add(scoredata)
    }
    fun getLastUpdateTime(levelID: Int?) : Int {
        var time = 0
        for( scoredata in scores ){
            if(levelID != null){
                if( scoredata.levelID != levelID ){ continue }
            }
            if( time < scoredata.sqlUpdateTime ){
                time = scoredata.sqlUpdateTime
            }
        }
        return time
    }
    fun getSortedScores(levelID: Int) : List<scoreData> {
        return scores.filter { it.levelID == levelID }.sortedByDescending { it.score }
    }

    // score Json化。 USERDATAに保存用
    fun toScoresJsonString() : String{
        val jArrary = Json.array()
        for( scoredata in this.scores ){
            val jObject = Json.`object`()
                .add("id",scoredata.sqlID.toString())
                .add("levelID",scoredata.levelID.toString())
                .add("score",scoredata.score.toString())
                .add("userID",scoredata.userID)
                .add("updateTime",scoredata.sqlUpdateTime.toString())

            jArrary.add(jObject)
        }
        //println("json output")
        //println(jArrary.toString())
        return jArrary.toString()
    }
    // 保存データから読み込み
    fun loadScoresJsonString(jsonStr:String){
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
            this.setScore(
                sqlID = json.get("id").asString().toInt(),
                levelID = json.get("levelID").asString().toInt(),
                score = json.get("score").asString().toInt(),
                userID = json.get("userID").asString(),
                updateTime = json.get("updateTime").asString().toInt()
            )
        }
    }
}
object CommentDataLists {
    var comments:ArrayList<commentData> = arrayListOf()
    var SqlIDtoIndex:MutableMap<Int,Int> = mutableMapOf() //最初からscores:MutableMap<Int,commentData>にしとけば良かった

    fun reset() {
        comments = arrayListOf()
        SqlIDtoIndex = mutableMapOf()
    }

    fun setComment(sqlID:Int, levelID:Int, comment:String, userID:String, updateTime:Int) {
        SqlIDtoIndex[sqlID]?.let {
            comments[it].levelID = levelID
            comments[it].comment = comment
            comments[it].userID = userID
            comments[it].sqlUpdateTime = updateTime
        }
        SqlIDtoIndex[sqlID] = comments.count()
        val commentdata = commentData()
        commentdata.sqlID = sqlID
        commentdata.levelID = levelID
        commentdata.comment = comment
        commentdata.userID = userID
        commentdata.sqlUpdateTime = updateTime
        comments.add(commentdata)
    }
    fun getLastUpdateTime(levelID: Int?) : Int {
        var time = 0
        for( commentdata in comments ){
            if( levelID != null ){
                if( commentdata.levelID != levelID ){ continue }
            }
            if( time < commentdata.sqlUpdateTime ){
                time = commentdata.sqlUpdateTime
            }
        }
        return time
    }
    fun getSortedComments(levelID: Int): List<commentData> {
        return this.comments.filter { it.levelID == levelID }.sortedByDescending { it.sqlID }
    }

    // comment Json化。 USERDATAに保存用
    fun toCommentsJsonString() : String{
        val jArrary = Json.array()
        for( commentdata in this.comments ){
            val jObject = Json.`object`()
                .add("id",commentdata.sqlID.toString())
                .add("levelID",commentdata.levelID.toString())
                .add("comment",commentdata.comment)
                .add("userID",commentdata.userID)
                .add("updateTime",commentdata.sqlUpdateTime.toString())

            jArrary.add(jObject)
        }
        //println("json output")
        //println(jArrary.toString())
        return jArrary.toString()
    }
    // 保存データから読み込み
    fun loadCommentsJsonString(jsonStr:String){
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
            this.setComment(
                sqlID = json.get("id").asString().toInt(),
                levelID = json.get("levelID").asString().toInt(),
                comment = json.get("comment").asString(),
                userID = json.get("userID").asString(),
                updateTime = json.get("updateTime").asString().toInt()
            )
        }
    }
}