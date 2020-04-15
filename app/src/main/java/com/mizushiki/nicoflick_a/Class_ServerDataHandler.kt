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
        var zero = System.currentTimeMillis()
        if(musicDatas.musics.count() == 0){
            musicDatas.loadMusicsJsonString(USERDATA.MusicsJson)
        }
        println("music:${System.currentTimeMillis()-zero}")
        zero = System.currentTimeMillis()
        if(musicDatas.levels.count() == 0){
            musicDatas.loadLevelsJsonString(USERDATA.LevelsJson)
        }
        println("level:${System.currentTimeMillis()-zero}")
        zero = System.currentTimeMillis()
        if(scoreDatas.scores.count() == 0){
            scoreDatas.loadScoresJsonString(USERDATA.ScoresJson)
        }
        println("score:${System.currentTimeMillis()-zero}")
        zero = System.currentTimeMillis()
        if(commentDatas.comments.count() == 0){
            commentDatas.loadCommentsJsonString(USERDATA.CommentsJson)
        }
        println("comment:${System.currentTimeMillis()-zero}")
        zero = System.currentTimeMillis()
        if(userNameDatas.userNames.count() == 0){
            userNameDatas.loadUserNameJsonString(USERDATA.UserNamesJson)
            userNameDatas.usernameJsonNumCount = USERDATA.UserNamesServerJsonNumCount
            userNameDatas.usernameJsonCreateTime = USERDATA.UserNamesServerJsonCreateTime
        }
        println("userName:${System.currentTimeMillis()-zero}")
        zero = System.currentTimeMillis()

        //データベース接続１ : まずmusicデータロード
        ServerDataHandler().DownloadMusicData {
            if (it != null) {
                println("music-load error")
                //callback(it)
                //return@DownloadMusicData
            }
            //データベース接続２ : 次にlevelデータロード
            ServerDataHandler().DownloadLevelData {
                if (it != null) {
                    println("level-load error")
                    //callback(it)
                    //return@DownloadMusicData
                }

                //データベース接続 おまけ : プレイ回数をデータベースに送信する（送信済みでないもの）。リザルトまで行かずに溜まったものがあれば出す。
                val playcountset = USERDATA.PlayCount.getSendPlayCountStr() //送信するデータ
                if( playcountset != "" ){
                    // プレイ回数 送信
                    ServerDataHandler().postPlayCountData(playcountset= playcountset) {
                        if( it ){
                            //プレイ回数データを保存する(初期化データになる)
                            USERDATA.PlayCount.setSended()
                        }
                    }
                }
                println("ServerData Download")
                callback(null)
                //
            }// 2.levelデータロード
        }// 1.musicデータロード
    }

    fun DownloadMusicData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=musicz&time="+musicDatas.getLastUpdateTimeMusic()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            if (it == "latest") {
                callback(null)
                return@let
            }else if(it.startsWith("server-url:")){
                val url = it.removePrefix("server-url:")
                DownloadMusicData_FirstData( serverURL = url, callback = callback)
            }
            try {
                val jsonArray = Json.parse(it).asArray()
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
                callback(null)

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }
    fun DownloadMusicData_FirstData(serverURL:String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = serverURL + "musicJson"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            try {
                val jsonArray = Json.parse(it).asArray()
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

                //もう一度通常取得を試みる（Swiftみたいに上手くできない？ので そのまま全部コピペ）
                //DownloadMusicData(callback)
                // ↓
                val url = GLOBAL.PHP_URL + "?req=musicz&time="+musicDatas.getLastUpdateTimeMusic()
                //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
                async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
                    if (it == null) {
                        callback("error")
                        return@let
                    }
                    if (it == "latest") {
                        callback(null)
                        return@let
                    }/*else if(it.startsWith("server-url:")){
                        val url = it.removePrefix("server-url:")
                        DownloadMusicData_FirstData( serverURL = url, callback = callback)
                    }*/
                    try {
                        val jsonArray = Json.parse(it).asArray()
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
                        callback(null)

                    } catch (e: Exception) {
                        println(e)
                        callback(e.message)
                    }
                }
                // ↑

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }

    fun DownloadLevelData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=levelz-noTimetag&time="+musicDatas.getLastUpdateTimeLevel()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            if (it == "latest") {
                callback(null)
                return@let
            }else if(it.startsWith("server-url:")){
                val url = it.removePrefix("server-url:")
                DownloadLevelData_FirstData( serverURL = url, callback = callback)
            }
            try {
                val jsonArray = Json.parse(it).asArray()
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
                callback(null)

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }
    fun DownloadLevelData_FirstData(serverURL: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = serverURL + "levelJson"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            try {
                val jsonArray = Json.parse(it).asArray()
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
                //もう一度通常取得を試みる（Swiftみたいに上手くできない？ので そのまま全部コピペ）
                //DownloadLevelData(callback)
                // ↓
                val url = GLOBAL.PHP_URL + "?req=levelz-noTimetag&time="+musicDatas.getLastUpdateTimeLevel()
                //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
                async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
                    if (it == null) {
                        callback("error")
                        return@let
                    }
                    if (it == "latest") {
                        callback(null)
                        return@let
                    }/*else if(it.startsWith("server-url:")){
                        val url = it.removePrefix("server-url:")
                        DownloadLevelData_FirstData( serverURL = url, callback = callback)
                    }*/
                    try {
                        val jsonArray = Json.parse(it).asArray()
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
                        callback(null)

                    } catch (e: Exception) {
                        println(e)
                        callback(e.message)
                    }
                }
                // ↑

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }

    fun DownloadTimetag(level:levelData, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=timetag&id="+level.sqlID
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            try {
                val jsonObject = Json.parse(it).asObject()
                // ロードした timetagデータを処理
                level.noteData = jsonObject.get("notes").asString()

                //保存データも更新
                USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                callback(null)

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }

    fun DownloadScoreData(levelID: Int, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=scorez&levelID=$levelID&time="+scoreDatas.getLastUpdateTime(levelID)
        println("score url=$url")
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

            try {
                val jsonArray = Json.parse(it).asArray()
                // ロードした scoreデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    scoreDatas.setScore(
                        sqlID = json.get("id").asString().toInt(),
                        levelID = levelID,
                        score = json.get("score").asString().toInt(),
                        userID = json.get("userID").asString(),
                        updateTime = json.get("updateTime").asString().toInt()
                    )
                }
                //保存データも更新
                USERDATA.ScoresJson = scoreDatas.toScoresJsonString()
                callback(null)

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }

    fun DownloadCommentData(levelID: Int, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=commentz&levelID=$levelID&time="+commentDatas.getLastUpdateTime(levelID)
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
            println(it)
            try {
                val jsonArray = Json.parse(it).asArray()
                // ロードした musicデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    commentDatas.setComment(
                        sqlID = json.get("id").asString().toInt(),
                        levelID = levelID,
                        comment = json.get("comment").asString(),
                        userID = json.get("userID").asString(),
                        updateTime = json.get("updateTime").asString().toInt()
                    )
                }
                //保存データも更新
                USERDATA.CommentsJson = commentDatas.toCommentsJsonString()
                callback(null)

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }

    fun DownloadUserNameData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {

        var updateTime = userNameDatas.getLastUpdateTime()
        if( userNameDatas.usernameJsonNumCount >= 0 ){
            updateTime = 0
        }
        //
        val url = GLOBAL.PHP_URL + "?req=usernamez&time=$updateTime"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            if (it == "latest") {
                callback(null)
                return@let
            }else if(it.startsWith("[{\"server-url\":")){
                //（サブ）サーバにファイルを取りに行く
                try {
                    val jsonArray = Json.parse(it).asArray()
                    // ロードしたデータを処理
                    val json = jsonArray.get(0).asObject()
                    val serverURL = json.get("server-url").asString()
                    val createTime = json.get("createTime").asString().toInt()
                    println(serverURL)
                    println(createTime)
                    if(userNameDatas.usernameJsonCreateTime == 0){
                        userNameDatas.usernameJsonCreateTime = createTime
                    }else if(userNameDatas.usernameJsonCreateTime != createTime){
                        userNameDatas.reset()
                        userNameDatas.usernameJsonCreateTime = createTime
                    }
                    //
                    DownloadUserNameData_FirstData( serverURL = serverURL, callback = callback)

                } catch (e: Exception) {
                    println(e)
                    callback(e.message)
                }
                return@let
            }

            try {
                val jsonArray = Json.parse(it).asArray()
                // ロードしたデータを処理
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
                USERDATA.UserNamesServerJsonNumCount = userNameDatas.usernameJsonNumCount
                USERDATA.UserNamesServerJsonCreateTime = userNameDatas.usernameJsonCreateTime
                callback(null)

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }

    fun DownloadUserNameData_FirstData(serverURL: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = serverURL + "usernameJson${userNameDatas.usernameJsonNumCount}"
        println("url=$url")
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            println(it)
            var data = it!!
            var nextFlg = false
            if(data.endsWith("//next")){
                println("//next")
                nextFlg = true
                data = data.removeSuffix("//next")
            }else {
                println("//finish")
            }
            try {
                val jsonArray = Json.parse(data).asArray()
                // ロードした levelデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    userNameDatas.setUserName(
                        sqlID = json.get("id").asString().toInt(),
                        userID = json.get("userID").asString(),
                        userName = json.get("name").asString(),
                        updateTime = json.get("updateTime").asString().toInt()
                    )
                }
                if(nextFlg){
                    userNameDatas.usernameJsonNumCount += 1
                }else {
                    userNameDatas.usernameJsonNumCount = -1
                }
                //保存データも更新
                USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                USERDATA.UserNamesServerJsonNumCount = userNameDatas.usernameJsonNumCount
                USERDATA.UserNamesServerJsonCreateTime = userNameDatas.usernameJsonCreateTime
                if(nextFlg){
                    callback(null)
                }else {
                    //分割Jsonの最後まで来た。もう一度通常取得を試みてFirstデータにない残りを取得。（Swiftみたいに上手くできない？ので そのまま全部コピペ）
                    //DownloadUserNameData(callback)
                    // ↓
                    var updateTime = userNameDatas.getLastUpdateTime()
                    if( userNameDatas.usernameJsonNumCount >= 0 ){
                        updateTime = 0
                    }
                    //
                    val url = GLOBAL.PHP_URL + "?req=usernamez&time=$updateTime"
                    //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
                    async(Dispatchers.Default) { HttpUtil.httpGET(url) }.await().let {
                        if (it == null) {
                            callback("error")
                            return@let
                        }
                        if (it == "latest") {
                            callback(null)
                            return@let
                        }/**/
                        try {
                            val jsonArray = Json.parse(it).asArray()
                            // ロードしたデータを処理
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
                            USERDATA.UserNamesServerJsonNumCount = userNameDatas.usernameJsonNumCount
                            USERDATA.UserNamesServerJsonCreateTime = userNameDatas.usernameJsonCreateTime
                            callback(null)

                        } catch (e: Exception) {
                            println(e)
                            callback(e.message)
                        }
                    }
                    // ↑
                }

            } catch (e: Exception) {
                println(e)
                callback(e.message)
            }
        }
    }
    fun Chance_DownloadUserNameData_FirstData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        if( UserNameDataLists.usernameJsonNumCount == -1 ){
            callback("")
            return@launch
        }
        DownloadUserNameData(callback)
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

    fun postReport(musicID:Int, comment:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=report&userID=${userID}&musicID=${musicID}&comment=${URLEncoder.encode(comment, "UTF-8")}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            if(it == "success report"){
                callback(true)
                return@let
            }
            callback(false)
       }
    }
}