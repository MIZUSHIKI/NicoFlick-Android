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
            println("music-load")
            //データベース接続２ : 次にlevelデータロード
            ServerDataHandler().DownloadLevelData {
                if (it != null) {
                    println("level-load error")
                    //callback(it)
                    //return@DownloadMusicData
                }
                println("level-load")
                //データベース接続３ : 次にplayCount,favoriteデータロード
                ServerDataHandler().DownloadPlayFavoriteCountData {
                    if (it != null) {
                        println("PlayFavorite-load error")
                        //callback(it)
                        //return@DownloadMusicData
                    }
                    println("UserNameID-get")
                    //データベース接続4 : もしUserNameを登録していてまだIDを取得していない場合（Ver.1.4未満ケア）
                    ServerDataHandler().GetUserNameSqlID(USERDATA.UserID) {
                        if (!it) {
                            println("UserNameID-get error")
                            //callback(it)
                            //return@DownloadMusicData
                        }


                        //データベース接続 おまけ : プレイ回数をデータベースに送信する（送信済みでないもの）。リザルトまで行かずに溜まったものがあれば出す。
                        // 3.でのロード後に送信してるけど気にしない
                        val pfcountset = PFCounter().getSendPlayFavoriteCountStr()
                        println("pfcountset=${pfcountset}")
                        if( pfcountset != "" ){
                            // データ送信
                            ServerDataHandler().postPlayFavoriteCountData(pfcountset = pfcountset){
                                if( it ){
                                    // データを保存する(初期化データになる)
                                    USERDATA.PlayCount.setSended()
                                    USERDATA.FavoriteCount.setSended()
                                }
                            }
                        }else {
                            val playcountset = USERDATA.PlayCount.getSendPlayCountStr() //送信するデータ
                            if( playcountset != "" ){
                                // プレイ回数 送信
                                ServerDataHandler().postPlayCountData(playcountset= playcountset) {
                                    if( it ){
                                        //プレイ回数データを保存する(初期化データになる)
                                        USERDATA.PlayCount.setSended()
                                    }
                                    println("post-PlayCount")
                                }
                            }
                            val favoritecountset = USERDATA.FavoriteCount.getSendFavoriteCountStr() //送信するデータ
                            if( favoritecountset != "" ){
                                // お気に入り回数 送信
                                ServerDataHandler().postFavoriteCountData(favoritecountset= favoritecountset) {
                                    if( it ){
                                        //プレイ回数データを保存する(初期化データになる)
                                        USERDATA.FavoriteCount.setSended()
                                    }
                                    println("post-FavoriteCount")
                                }
                            }
                        }
                        println("ServerData Download")
                        callback(null)


                    }// 4.UserNameID取得
                }// 3.playCount,favoriteデータロード
            }// 2.levelデータロード
        }// 1.musicデータロード
    }

    fun DownloadMusicData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=musicy&time="+musicDatas.getLastUpdateTimeMusic()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
            if (it == null) {
                println("")
                callback("error")
                GLOBAL.ServerErrorMessage = "予期しないエラーが発生しました"
                return@let
            }
            var htRet = it
            if( htRet.startsWith("<!--NicoFlickMessage=") ){
                GLOBAL.ServerErrorMessage = htRet.pregMatche_firstString("^<!--NicoFlickMessage=(.*?)-->")
                htRet = htRet.pregReplace("^<!--NicoFlickMessage=.*?-->", "" )
                val res = GLOBAL.ServerErrorMessage.pregMatche_strings("\\[([Android,iOS ]+)\\]")
                if (!res.isEmpty() && !res.last().pregMatche("Android")) {
                    GLOBAL.ServerErrorMessage = ""
                }
                print(GLOBAL.ServerErrorMessage)
            }
            if (htRet == "latest") {
                callback(null)
                return@let
            }else if(htRet.startsWith("server-url:")){
                val url = htRet.removePrefix("server-url:")
                DownloadMusicData_FirstData( serverURL = url, callback = callback)
                return@let
            }
            try {
                val jsonArray = Json.parse(htRet).asArray()
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
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }
    fun DownloadMusicData_FirstData(serverURL:String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = serverURL + "musicJson"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                val url = GLOBAL.PHP_URL + "?req=musicm&time="+musicDatas.getLastUpdateTimeMusic()
                //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
                async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
                    if (it == null) {
                        callback("error")
                        return@let
                    }
                    var htRet = it
                    if( htRet.startsWith("<!--NicoFlickMessage=") ){
                        //GLOBAL.ServerErrorMessage = htRet.pregMatche_firstString("^<!--NicoFlickMessage=(.*?)-->")
                        htRet = htRet.pregReplace("^<!--NicoFlickMessage=.*?-->", "" )
                        //print(GLOBAL.ServerErrorMessage)
                    }
                    if (htRet == "latest") {
                        callback(null)
                        return@let
                    }/*else if(htRet.startsWith("server-url:")){
                        val url = htRet.removePrefix("server-url:")
                        DownloadMusicData_FirstData( serverURL = url, callback = callback)
                    }*/
                    try {
                        val jsonArray = Json.parse(htRet).asArray()
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
                        return@let

                    } catch (e: Exception) {
                        println(e)
                        callback(e.message)
                        return@let
                    }
                }
                // ↑

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadLevelData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=levelm-noTimetag&userID="+USERDATA.UserID.take(8)+"&time="+musicDatas.getLastUpdateTimeLevel()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                return@let
            }
            try {
                val jsonArray = Json.parse(it).asArray()
                // ロードした levelデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    //下位互換
                    val playCountTime = json.get("playCountTime")?.asString()?.toInt() ?: 0
                    val favoriteCount =  json.get("favorite")?.asString()?.toInt() ?: 0
                    val favoriteCountTime = json.get("favoriteTime")?.asString()?.toInt() ?: 0
                    val commentTime = json.get("commentTime")?.asString()?.toInt() ?: 0
                    val scoreTime = json.get("scoreTime")?.asString()?.toInt() ?: 0
                    musicDatas.setLevel(
                        sqlID = json.get("id").asString().toInt(),
                        movieURL = json.get("movieURL").asString(),
                        level = json.get("level").asString().toInt(),
                        creator = json.get("creator").asString(),
                        description = json.get("description").asString(),
                        speed = json.get("speed").asString().toInt(),
                        noteData = json.get("bitNote")?.asString() ?: "",
                        updateTime = json.get("updateTime").asString().toInt(),
                        createTime = json.get("createTime").asString().toInt(),
                        playCount = json.get("playCount").asString().toInt(),
                        playCountTime = playCountTime,
                        favoriteCount = favoriteCount,
                        favoriteCountTime = favoriteCountTime,
                        commentTime = commentTime,
                        scoreTime = scoreTime
                    )
                }
                //保存データも更新
                USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                callback(null)
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }
    fun DownloadLevelData_FirstData(serverURL: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = serverURL + "levelJson"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
            if (it == null) {
                callback("error")
                return@let
            }
            try {
                val jsonArray = Json.parse(it).asArray()
                // ロードした levelデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    //下位互換
                    val playCountTime = json.get("playCountTime")?.asString()?.toInt() ?: 0
                    val favoriteCount =  json.get("favorite")?.asString()?.toInt() ?: 0
                    val favoriteCountTime = json.get("favoriteTime")?.asString()?.toInt() ?: 0
                    val commentTime = json.get("commentTime")?.asString()?.toInt() ?: 0
                    val scoreTime = json.get("scoreTime")?.asString()?.toInt() ?: 0
                    musicDatas.setLevel(
                        sqlID = json.get("id").asString().toInt(),
                        movieURL = json.get("movieURL").asString(),
                        level = json.get("level").asString().toInt(),
                        creator = json.get("creator").asString(),
                        description = json.get("description").asString(),
                        speed = json.get("speed").asString().toInt(),
                        noteData = json.get("bitNote")?.asString() ?: "",
                        updateTime = json.get("updateTime").asString().toInt(),
                        createTime = json.get("createTime").asString().toInt(),
                        playCount = json.get("playCount").asString().toInt(),
                        playCountTime = playCountTime,
                        favoriteCount = favoriteCount,
                        favoriteCountTime = favoriteCountTime,
                        commentTime = commentTime,
                        scoreTime = scoreTime
                    )
                }
                //保存データも更新
                USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                //もう一度通常取得を試みる（Swiftみたいに上手くできない？ので そのまま全部コピペ）
                //DownloadLevelData(callback)
                // ↓
                val url = GLOBAL.PHP_URL + "?req=levelm-noTimetag&userID="+USERDATA.UserID.take(8)+"&time="+musicDatas.getLastUpdateTimeLevel()
                //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
                async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                            //下位互換
                            val playCountTime = json.get("playCountTime")?.asString()?.toInt() ?: 0
                            val favoriteCount =  json.get("favorite")?.asString()?.toInt() ?: 0
                            val favoriteCountTime = json.get("favoriteTime")?.asString()?.toInt() ?: 0
                            val commentTime = json.get("commentTime")?.asString()?.toInt() ?: 0
                            val scoreTime = json.get("scoreTime")?.asString()?.toInt() ?: 0
                            musicDatas.setLevel(
                                sqlID = json.get("id").asString().toInt(),
                                movieURL = json.get("movieURL").asString(),
                                level = json.get("level").asString().toInt(),
                                creator = json.get("creator").asString(),
                                description = json.get("description").asString(),
                                speed = json.get("speed").asString().toInt(),
                                noteData = json.get("bitNote")?.asString() ?: "",
                                updateTime = json.get("updateTime").asString().toInt(),
                                createTime = json.get("createTime").asString().toInt(),
                                playCount = json.get("playCount").asString().toInt(),
                                playCountTime = playCountTime,
                                favoriteCount = favoriteCount,
                                favoriteCountTime = favoriteCountTime,
                                commentTime = commentTime,
                                scoreTime = scoreTime
                            )
                        }
                        //保存データも更新
                        USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                        callback(null)
                        return@let

                    } catch (e: Exception) {
                        println(e)
                        callback(e.message)
                        return@let
                    }
                }
                // ↑

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadPlayFavoriteCountData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=PcFcCtSt&playcountTime="+musicDatas.getLastUpdateTimeLevel() + "&playcountTime="+musicDatas.getLastPlayCountTimeLevel() + "&favoriteTime="+musicDatas.getLastFavoriteCountTimeLevel() + "&commentTime="+musicDatas.getLastCommentTimeLevel() + "&scoreTime="+musicDatas.getLastScoreTimeLevel()
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                // ロードした levelデータを処理
                for (i in 0 until jsonArray.size()) {
                    val json = jsonArray.get(i).asObject()
                    //下位互換
                    val playCount = json.get("playCount")?.asString()?.toInt() ?: -1
                    val playCountTime = json.get("playCountTime")?.asString()?.toInt() ?: -1
                    val favoriteCount =  json.get("favorite")?.asString()?.toInt() ?: -1
                    val favoriteCountTime = json.get("favoriteTime")?.asString()?.toInt() ?: -1
                    val commentTime = json.get("commentTime")?.asString()?.toInt() ?: -1
                    val scoreTime = json.get("scoreTime")?.asString()?.toInt() ?: -1
                    musicDatas.setLevel_PlaycountFavorite(
                        sqlID = json.get("id").asString().toInt(),
                        playCount = playCount,
                        playCountTime = playCountTime,
                        favoriteCount = favoriteCount,
                        favoriteCountTime = favoriteCountTime,
                        commentTime = commentTime,
                        scoreTime = scoreTime
                    )
                }
                //保存データも更新
                USERDATA.LevelsJson = musicDatas.toLevelsJsonString()
                callback(null)
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadTimetag(level:levelData, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=timetag&id="+level.sqlID
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadScoreData(levelID: Int, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=scorez&levelID=$levelID&time="+scoreDatas.getLastUpdateTime(levelID)
        println("score url=$url")
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadCommentData(levelID: Int, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=commentz&levelID=$levelID&time="+commentDatas.getLastUpdateTime(levelID)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
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
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                    println("namedata ${serverURL}")
                    println("namedata ${createTime}")
                    if(userNameDatas.usernameJsonCreateTime == 0){
                        userNameDatas.usernameJsonCreateTime = createTime
                    }else if(userNameDatas.usernameJsonCreateTime != createTime){
                        userNameDatas.reset()
                        userNameDatas.usernameJsonCreateTime = createTime
                    }
                    //
                    DownloadUserNameData_FirstData( serverURL = serverURL, callback = callback)
                    return@let

                } catch (e: Exception) {
                    println(e)
                    callback(e.message)
                    return@let
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
                return@let

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadUserNameData_FirstData(serverURL: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = serverURL + "usernameJson${userNameDatas.usernameJsonNumCount}"
        println("url=$url")
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                    return@let
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
                    async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
                            return@let

                        } catch (e: Exception) {
                            println(e)
                            callback(e.message)
                            return@let
                        }
                    }
                    // ↑
                }

            } catch (e: Exception) {
                println(e)
                callback(e.message)
                return@let
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
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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

    fun GetMusicScoreData(musicID:Int, userID:String, callback: (JsonArray?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=mscore&musicID=${musicID}&userID=${userID}"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
            if (it == null) {
                callback(null)
                return@let
            }
            println("json url = ${url}")
            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse("[${it}]").asArray()

                println("json toreta ${it}")
            } catch (e: Exception) {
                println("json sippai ${it}")
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
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
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
        val body = "req=userName-add&id=${userID}&name=${URLEncoder.encode(name,"utf-8")}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            if (it.startsWith("success userName-add UserNameSqlID=")) {
                USERDATA.UserNameID = it.pregMatche_firstString("UserNameSqlID=(\\d+)").toInt()
            }
            callback(true)
        }
    }
    //Ver.1.4未満のケア
    fun GetUserNameSqlID(userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        if( USERDATA.UserName == "" || USERDATA.UserNameID != 0 ){
            println("usernameid=${USERDATA.UserNameID}")
            callback(true)
            return@launch
        }
        val url = GLOBAL.PHP_URL + "?req=userNameID&id=$userID"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            if (it.startsWith("success UserNameSqlID=")) {
                USERDATA.UserNameID = it.pregMatche_firstString("UserNameSqlID=(\\d+)").toInt()
            }
            callback(true)
        }
    }
    fun postScoreData(scoreset:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=scorez-add&userID=${userID}&userNameID=${USERDATA.UserNameID}&scoreset=${scoreset}&pass=${Crypt.encryptx_urlsafe("ニコFlick", userID)}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
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
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
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
    fun postFavoriteCountData(favoritecountset:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=favoritez-add&favoritecountset=${favoritecountset}"
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            if (it == "success favorite-add") {
                callback(true)
            } else {
                println("favorite送信失敗")
                callback(false)
            }
        }
    }
    fun postPlayFavoriteCountData(pfcountset:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=PlaycountFavoritez-add&PFcountset=${pfcountset}"
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            if (it == "success PFcount-add") {
                callback(true)
            } else {
                println("PFcount送信失敗")
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
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            callback(true)
        }
    }
    fun postMusicTagUpdate(id:Int, tags:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=musicTag-update&id=${id}&tags=${URLEncoder.encode(tags,"utf-8")}&userID=${userID}&pass=${Crypt.encryptx_urlsafe("ニコFlick", userID)}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            if (it == "success musictag-update") {
                callback(true)
            } else {
                println("musicタグ送信失敗")
                callback(false)
            }
        }
    }

    fun postReport(musicID:Int, comment:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=report&userID=${userID}&musicID=${musicID}&comment=${URLEncoder.encode(comment, "UTF-8")}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
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
    fun postTagsToMusics(tags:String, musicsStr:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=tagsToMusics&tags=${URLEncoder.encode(tags,"UTF-8")}&musics=${URLEncoder.encode(musicsStr, "UTF-8")}"
        println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            println("postRet="+it)
            callback(true)
        }
    }
}