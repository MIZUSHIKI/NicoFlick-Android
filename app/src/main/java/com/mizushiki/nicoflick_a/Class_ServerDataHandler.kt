package com.mizushiki.nicoflick_a

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Suppress("LABEL_NAME_CLASH", "NAME_SHADOWING")
class ServerDataHandler {

    //各種データ
    var musicDatas:MusicDataLists = MusicDataLists
    var scoreDatas:ScoreDataLists = ScoreDataLists
    var commentDatas:CommentDataLists = CommentDataLists
    var userNameDatas:UserNameDataLists = UserNameDataLists

    // -データベース過負荷対策
    // とりあえず一度ロードしたデータは保存するようにした。（原型はあったが不具合を恐れて使用していなかった）
    // ただ、まだ不十分だと思われるのでデータベース全開放はもう少し待つ見通し。

    //非同期処理でHTTP GETを実行します。
    fun DownloadMusicDataAndUserNameData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //
        //  DB接続 1 : まず musicデータロード
        //
        //の前に保存データから持ってくる
        musicDatas.loadMusicsJsonString(USERDATA.MusicsJson)
        musicDatas.loadLevelsJsonString(USERDATA.LevelsJson)
        //
        val url = GLOBAL.PHP_URL + "?req=music&time="+musicDatas.getLastUpdateTimeMusic()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if(it == null){
                callback("error")
                return@let
            }
            println(it)
            if(it != "latest"){
                val jsonArray: JsonArray
                try {
                    jsonArray = Json.parse(it).asArray()
                }catch (e:Exception){
                    println(e)
                    callback(null)
                    return@let
                }
                // ロードした musicデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    musicDatas.setMusic(
                        sqlID = json.get("id").asString().toInt(),
                        movieURL = json.get("movieURL").asString(),
                        thumbnailURL = json.get("thumbnailURL").asString(),
                        title = json.get("title").asString(),
                        artist = json.get("artist").asString(),
                        movieLength = json.get("movieLength").asString(),
                        tags = json.get("tags").asString(),
                        updateTime = json.get("updateTime").asString().toInt(),
                        createTime = json.get("createTime").asString().toInt()
                    )
                }
                musicDatas.createTaglist()
                //保存データも更新
                USERDATA.MusicsJson = musicDatas.toMusicsJsonString()
            }
            //
            //  DB接続 2 : 次に levelデータロード
            //
            val url = GLOBAL.PHP_URL + "?req=level-noTimetag&time="+musicDatas.getLastUpdateTimeLevel()
            //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
            async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
                if(it == null){
                    callback("error")
                    return@let
                }
                if(it != "latest"){
                    val jsonArray: JsonArray
                    try {
                        jsonArray = Json.parse(it).asArray()
                    }catch (e:Exception){
                        println(e)
                        callback(null)
                        return@let
                    }
                    // ロードした levelデータを処理
                    for (i in 0 until jsonArray.size()) {
                        val json = jsonArray.get(i).asObject()
                        musicDatas.setLevel(
                            sqlID = json.get("id").asString().toInt(),
                            movieURL = json.get("movieURL").asString(),
                            level = json.get("level").asString().toInt(),
                            creator = json.get("creator").asString(),
                            description = json.get("description").asString(),
                            speed = json.get("speed").asString().toInt(),
                            noteData = "",
                            updateTime = json.get("updateTime").asString().toInt(),
                            createTime = json.get("createTime").asString().toInt(),
                            playCount = json.get("playCount").asString().toInt()
                        )
                    }
                    //保存データも更新
                    USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                }
                /* スコアデータもコメントデータもユーザーネームも ここではロードしないことにする。
                //
                //  DB接続 3 : 次に スコアデータロード
                //
                ServerDataHandler().DownloadScoreData {
                    if(it!=null){
                        callback(it)
                        return@DownloadScoreData
                    }
                    //
                    //  DB接続 4 : 次に コメントデータロード
                    //
                    ServerDataHandler().DownloadCommentData {
                        if(it!=null){
                            callback(it)
                            return@DownloadCommentData
                        }
                        //
                        //  DB接続 5 : 最後に UserNameデータロード
                        //
                        ServerDataHandler().DownloadUserName {
                            if(it!=null){
                                callback(it)
                                return@DownloadUserName
                            }
                            */
                            //
                            //  DB接続 6 : おまけで playCountを Post
                            //
                            val playcountset = USERDATA.PlayCount.getSendPlayCountStr() //送信するデータ
                            println("playcountset="+playcountset)
                            if( playcountset != "" ){
                                // プレイ回数 送信
                                ServerDataHandler().postPlayCountData(playcountset= playcountset) {
                                    if( it ){
                                        //プレイ回数データを保存する(初期化データになる)
                                        USERDATA.PlayCount.setSended()
                                    }
                                }
                            }
                            callback(null)
                            /*
                        }//  DB接続 5 : 最後に UserNameデータロード
                    }//  DB接続 4 : 次に コメントデータロード
                }//  DB接続 3 : 次に スコアデータロード
                */
            }//  DB接続 2 : 次に levelデータロード
        }//  DB接続 1 : まず musicデータロード
    }

    fun DownloadTimetag(level:levelData, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=timetag&id="+level.sqlID
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            val jsonObject: JsonObject

            try {
                jsonObject = Json.parse(it).asObject()
            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
            // ロードした timetagデータを処理
            level.noteData = jsonObject.get("notes").asString()

            //保存データも更新
            USERDATA.LevelsJson = musicDatas.toLevelsJsonString()

            callback(null)
        }
    }

    fun DownloadScoreData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=score&levelID=ALL&time="+scoreDatas.getLastUpdateTime()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            if (it == "latest") {
                callback(null)
                return@let
            }

            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse(it).asArray()
            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
            // ロードした musicデータを処理
            for (i in 0 until jsonArray.size()) {
                val json = jsonArray.get(i).asObject()
                scoreDatas.setScore(
                    sqlID = json.get("id").asString().toInt(),
                    levelID = json.get("levelID").asString().toInt(),
                    score = json.get("score").asString().toInt(),
                    userID = json.get("userID").asString(),
                    updateTime = json.get("updateTime").asString().toInt()
                )
            }
            callback(null)
        }
    }

    fun DownloadCommentData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=comment&levelID=ALL&time="+commentDatas.getLastUpdateTime()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            if (it == "latest") {
                callback(null)
                return@let
            }

            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse(it).asArray()
            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
            // ロードした musicデータを処理
            for (i in 0 until jsonArray.size()) {
                val json = jsonArray.get(i).asObject()
                commentDatas.setComment(
                    sqlID = json.get("id").asString().toInt(),
                    levelID = json.get("levelID").asString().toInt(),
                    comment = "",
                    userID = json.get("userID").asString(),
                    updateTime = json.get("updateTime").asString().toInt()
                )
            }
            callback(null)
        }
    }

    fun DownloadUserName(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //の前に保存データから持ってくる
        userNameDatas.loadUserNameJsonString(USERDATA.UserNamesJson)
        //
        val url = GLOBAL.PHP_URL + "?req=username&time="+userNameDatas.getLastUpdateTime()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            if (it == "latest") {
                callback(null)
                return@let
            }

            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse(it).asArray()
            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
            // ロードした musicデータを処理
            for (i in 0 until jsonArray.size()) {
                val json = jsonArray.get(i).asObject()
                userNameDatas.setUserName(
                    sqlID = json.get("id").asString().toInt(),
                    userID = json.get("userID").asString(),
                    userName = json.get("name").asString(),
                    updateTime = json.get("updateTime").asString().toInt()
                )
            }
            //保存データも更新
            USERDATA.UserNamesJson = userNameDatas.toUserNameJsonString()

            callback(null)
        }
    }


    fun GetScoreData(levelID:Int, callback: (JsonArray?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=score&levelID=$levelID"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback(null)
                return@let
            }
            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse(it).asArray()
            } catch (e: Exception) {
                println(e)
                callback(null)
                return@let
            }
            callback(jsonArray)
        }
    }

    fun GetCommentData(levelID:Int, callback: (JsonArray?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=comment&levelID=$levelID"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            println("com="+it)
            if (it == null) {
                callback(null)
                return@let
            }
            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse(it).asArray()
            } catch (e: Exception) {
                println(e)
                callback(null)
                return@let
            }
            callback(jsonArray)
        }
    }

    fun postUserName(name:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=userName-add&id=${userID}&name=${name}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            callback(true)
        }
    }
    fun postScoreData(scoreset:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=score-add&userID=${userID}&scoreset=${scoreset}&pass=${Crypt.encryptx_urlsafe("ニコFlick", userID)}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            if (it == "success score-add") {
                callback(true)
            } else {
                println("スコア送信失敗")
                callback(false)
            }
        }
    }
    fun postPlayCountData(playcountset:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=playcount-add&playcountset=${playcountset}"
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            if (it == "success playcount-add") {
                callback(true)
            } else {
                println("playcount送信失敗")
                callback(false)
            }
        }
    }
    fun postComment(comment:String, levelID: Int, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=comment-add&userID=${userID}&levelID=${levelID}&comment=${URLEncoder.encode(comment, "UTF-8")}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            callback(true)
        }
    }
}