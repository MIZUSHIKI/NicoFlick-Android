package com.mizushiki.nicoflick_a

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        // 大量のランキング・コメント・ユーザー名はDBから必要時に読む。
        withContext(Dispatchers.IO) { MetadataStorage.load() }
        userNameDatas.usernameJsonNumCount = USERDATA.UserNamesServerJsonNumCount
        userNameDatas.usernameJsonCreateTime = USERDATA.UserNamesServerJsonCreateTime

        //データベース接続１ : まずmusicデータロード
        ServerDataHandler().DownloadMusicData {
            if (it != null) {
                // println("music-load error")
                //callback(it)
                //return@DownloadMusicData
            }
            // println("music-load")
            //データベース接続２ : 次にlevelデータロード
            ServerDataHandler().DownloadLevelData {
                if (it != null) {
                    // println("level-load error")
                    //callback(it)
                    //return@DownloadMusicData
                }
                // println("level-load")
                //データベース接続３ : 次にplayCount,likeCountデータロード
                ServerDataHandler().Download_level_PlayLikeCountData {
                    if (it != null) {
                        // println("PlayFavorite-load error")
                        //callback(it)
                        //return@DownloadMusicData
                    }
                    //データベース接続4 : 次にfavoriteCountデータロード
                    ServerDataHandler().Download_music_FavoriteCountData {
                        // println("UserNameID-get")
                        //データベース接続5 : もしUserNameを登録していてまだIDを取得していない場合（Ver.1.4未満ケア）
                        ServerDataHandler().GetUserNameSqlID(USERDATA.UserID) {
                            if (!it) {
                                // println("UserNameID-get error")
                                //callback(it)
                                //return@DownloadMusicData
                            }

                            //データベース接続 おまけ : プレイ回数をデータベースに送信する（送信済みでないもの）。リザルトまで行かずに溜まったものがあれば出す。
                            // 3.でのロード後に送信してるけど気にしない
                            postUserCountData()
                            // println("ServerData Download")
                            callback(null)

                        }// 5.UserNameIDデータ取得
                    } // 4.music favoriteCountデータロード
                }// 3.level playCount,likeCountデータロード
            }// 2.levelデータロード
        }// 1.musicデータロード
    }

    fun DownloadMusicData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        callback(downloadMusicData(afterFirstData = false))
    }

    fun DownloadMusicData_FirstData(serverURL:String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        callback(downloadMusicFirstData(serverURL))
    }

    private suspend fun downloadMusicData(afterFirstData: Boolean): String? {
        if (!afterFirstData) {
            GLOBAL.ServerErrorMessage = ""
            GLOBAL.ServerErrorTitle = "サーバエラー"
        }
        val url = GLOBAL.PHP_URL + "?req=musics&time=" + musicDatas.getLastUpdateTimeMusic()
        val response = withContext(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }
        if (response == null) {
            if (!afterFirstData) {
                GLOBAL.ServerErrorMessage = "予期しないエラーが発生しました"
            }
            return "error"
        }

        var body = response
        if (body.startsWith("<!--NicoFlickMessage=")) {
            // 初期取得後の差分取得では、表示用メッセージを更新しない。
            if (!afterFirstData) {
                // SubMessageはニコRe仮のものなので強制削除（役割を終えた）。
                val rawMessage = body.pregMatche_firstString("^<!--NicoFlickMessage=(.*?)-->")
                    .pregReplace("&NicoFlickSubMessage=.*?$", "")
                    .pregReplace("\\[\\[Silent:.*?\\]\\]", "")
                val notice = ServerErrorNotice.parse(rawMessage)
                GLOBAL.ServerErrorMessage = notice.message
                GLOBAL.ServerErrorTitle = notice.title
            }
            body = body.pregReplace("^<!--NicoFlickMessage=.*?-->", "")
        }
        if (body == "latest") return null

        // 初期取得後の再要求は、繰り返し取得を防ぐため明示的なエラーを返す。
        if (body.startsWith("server-url:")) {
            if (afterFirstData) return "Unexpected server-url after initial music data download"
            return downloadMusicFirstData(body.removePrefix("server-url:"))
        }
        return try {
            applyMusicJson(body)
            null
        } catch (e: Exception) {
            // println(e)
            e.message
        }
    }

    private suspend fun downloadMusicFirstData(serverURL: String): String? {
        val response = withContext(Dispatchers.Default) {
            HttpUtil.httpGET(serverURL + "musicJson", noCache = true)
        } ?: return "error"

        try {
            applyMusicJson(response)
        } catch (e: Exception) {
            // println(e)
            return e.message
        }
        // 初期データを反映した後の更新時刻で差分を取得する。
        return downloadMusicData(afterFirstData = true)
    }

    private suspend fun applyMusicJson(jsonText: String) {
        val jsonArray = Json.parse(jsonText).asArray()
        for (i in 0 until jsonArray.size()) {
            val json = jsonArray.get(i).asObject()
            // 仕様変更
            // - ver.2.0～: favorite, favoriteTime
            musicDatas.setMusic(
                sqlID = json.get("id").asString().toInt(),
                movieURL = json.get("movieURL").asString(),
                thumbnailURL = json.get("thumbnailURL").asString(),
                title = json.get("title").asString(),
                artist = json.get("artist").asString(),
                movieLength = json.get("movieLength").asString(),
                tags = json.get("tags").asString(),
                updateTime = json.get("updateTime").asString().toInt(),
                createTime = json.get("createTime").asString().toInt(),
                favoriteCount = json.get("favorite")?.asString()?.toIntOrNull() ?: 0,
                favoriteCountTime = json.get("favoriteTime")?.asString()?.toIntOrNull() ?: 0
            )
        }
        musicDatas.createTaglist()
        withContext(Dispatchers.IO) { MetadataStorage.saveMusics() }
    }

    fun DownloadLevelData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        callback(downloadLevelData(afterFirstData = false))
    }

    fun DownloadLevelData_FirstData(serverURL: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        callback(downloadLevelFirstData(serverURL))
    }

    private suspend fun downloadLevelData(afterFirstData: Boolean): String? {
        val url = GLOBAL.PHP_URL + "?req=levely-noTimetag&userID=" + USERDATA.UserID.take(8) +
            "&time=" + musicDatas.getLastUpdateTimeLevel()
        val response = withContext(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }
            ?: return "error"
        if (response == "latest") return null

        // 初期取得後の再要求は、繰り返し取得を防ぐため明示的なエラーを返す。
        if (response.startsWith("server-url:")) {
            if (afterFirstData) return "Unexpected server-url after initial level data download"
            return downloadLevelFirstData(response.removePrefix("server-url:"))
        }
        return try {
            applyLevelJson(response)
            null
        } catch (e: Exception) {
            // println(e)
            e.message
        }
    }

    private suspend fun downloadLevelFirstData(serverURL: String): String? {
        val response = withContext(Dispatchers.Default) {
            HttpUtil.httpGET(serverURL + "levelJson", noCache = true)
        } ?: return "error"

        try {
            applyLevelJson(response)
        } catch (e: Exception) {
            // println(e)
            return e.message
        }
        // 初期データを反映した後の更新時刻で差分を取得する。
        return downloadLevelData(afterFirstData = true)
    }

    private suspend fun applyLevelJson(jsonText: String) {
        val jsonArray = Json.parse(jsonText).asArray()
        for (i in 0 until jsonArray.size()) {
            val json = jsonArray.get(i).asObject()
            // 仕様変更
            // - ver.1.*～: playCountTime, commentTime, scoreTime
            // - ver.2.0～: poslike, dislike, LikeTime - DEL: favorite, favoriteTime
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
                playCountTime = json.get("playCountTime")?.asString()?.toInt() ?: 0,
                favoriteCount = json.get("favorite")?.asString()?.toInt() ?: 0,
                favoriteCountTime = json.get("favoriteTime")?.asString()?.toInt() ?: 0,
                commentTime = json.get("commentTime")?.asString()?.toInt() ?: 0,
                scoreTime = json.get("scoreTime")?.asString()?.toInt() ?: 0,
                likeCount = json.get("poslike")?.asString()?.toIntOrNull() ?: 0,
                dislikeCount = json.get("dislike")?.asString()?.toIntOrNull() ?: 0,
                likeCountTime = json.get("likeTime")?.asString()?.toIntOrNull() ?: 0,
                calcLv = json.get("calcLv")?.asString()?.toIntOrNull() ?: 0
            )
        }
        musicDatas.createTaglist()
        withContext(Dispatchers.IO) { MetadataStorage.saveLevels() }
    }

    fun Download_level_PlayLikeCountData(callback:(String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=level-PcLcCtSt&playcountTime=" + musicDatas.getLastPlayCountTimeLevel() +
            "&likeTime=" + musicDatas.getLastLikeCountTimeLevel() + "&commentTime=" + musicDatas.getLastCommentTimeLevel() +
            "&scoreTime=" + musicDatas.getLastScoreTimeLevel()
        val response = async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await()
        if (response == null) { callback("error"); return@launch }
        if (response.trim() == "latest") { callback(null); return@launch }
        try {
            for (item in Json.parse(response).asArray()) {
                val json = item.asObject()
                musicDatas.setLevel_PlaycountLike(
                    sqlID = json.get("id").asString().toInt(),
                    playCount = json.get("playCount")?.asString()?.toIntOrNull() ?: -1,
                    playCountTime = json.get("playCountTime")?.asString()?.toIntOrNull() ?: -1,
                    commentTime = json.get("commentTime")?.asString()?.toIntOrNull() ?: -1,
                    scoreTime = json.get("scoreTime")?.asString()?.toIntOrNull() ?: -1,
                    likeCount = json.get("poslike")?.asString()?.toIntOrNull() ?: -1,
                    dislikeCount = json.get("dislike")?.asString()?.toIntOrNull() ?: -1,
                    likeCountTime = json.get("likeTime")?.asString()?.toIntOrNull() ?: -1)
            }
                withContext(Dispatchers.IO) { MetadataStorage.saveLevels() }
            callback(null)
        } catch (e:Exception) { callback(e.message ?: "Invalid level counts") }
    }

    fun Download_music_FavoriteCountData(callback:(String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=music-Fc&musicFavoriteTime=" + musicDatas.getLastFavoriteCountTimeMusic()
        val response = async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await()
        if (response == null) { callback("error"); return@launch }
        if (response.trim() == "latest") { callback(null); return@launch }
        try {
            for (item in Json.parse(response).asArray()) {
                val json = item.asObject()
                musicDatas.setMusic_Favoritecount(json.get("id").asString().toInt(),
                    json.get("favorite")?.asString()?.toIntOrNull() ?: -1,
                    json.get("favoriteTime")?.asString()?.toIntOrNull() ?: -1)
            }
            withContext(Dispatchers.IO) { MetadataStorage.saveMusics() }
            callback(null)
        } catch (e:Exception) { callback(e.message ?: "Invalid music counts") }
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
                withContext(Dispatchers.IO) { MetadataStorage.saveLevels() }
                callback(null)
                return@let

            } catch (e: Exception) {
                // println(e)
                callback(e.message)
                return@let
            }
        }
    }

    fun DownloadScoreData(levelID: Int, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val error = try {
            withContext(Dispatchers.IO) {
                val url = GLOBAL.PHP_URL + "?req=scorez&levelID=$levelID&time=" + scoreDatas.getLastUpdateTime(levelID)
                val response = HttpUtil.httpGET(url, noCache = true) ?: return@withContext "error"
                if (response.trim() != "latest") scoreDatas.importResponse(levelID, response)
                null
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            e.message ?: "Unable to save ranking"
        }
        callback(error)
    }

    fun DownloadCommentData(levelID: Int, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val error = try {
            withContext(Dispatchers.IO) {
                val url = GLOBAL.PHP_URL + "?req=commentz&levelID=$levelID&time=" + commentDatas.getLastUpdateTime(levelID)
                val response = HttpUtil.httpGET(url, noCache = true) ?: return@withContext "error"
                if (response.trim() != "latest") commentDatas.importResponse(levelID, response)
                null
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            e.message ?: "Unable to save comments"
        }
        callback(error)
    }

    fun DownloadUserNameData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        callback(downloadUserNameData(afterFirstData = false))
    }

    fun DownloadUserNameData_FirstData(serverURL: String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        callback(downloadUserNameFirstData(serverURL))
    }

    private suspend fun downloadUserNameData(afterFirstData: Boolean): String? {
        val updateTime = if (userNameDatas.usernameJsonNumCount >= 0) 0 else userNameDatas.getLastUpdateTime()
        val url = GLOBAL.PHP_URL + "?req=usernamez&time=$updateTime"
        val response = withContext(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }
            ?: return "error"
        if (response == "latest") return null

        // 初期取得後の再要求は、繰り返し取得を防ぐため明示的なエラーを返す。
        if (response.startsWith("[{\"server-url\":")) {
            if (afterFirstData) return "Unexpected server-url after initial user name data download"
            return try {
                val json = Json.parse(response).asArray().get(0).asObject()
                val serverURL = json.get("server-url").asString()
                val createTime = json.get("createTime").asString().toInt()
                if (userNameDatas.usernameJsonCreateTime == 0) {
                    userNameDatas.usernameJsonCreateTime = createTime
                } else if (userNameDatas.usernameJsonCreateTime != createTime) {
                    userNameDatas.reset()
                    userNameDatas.usernameJsonCreateTime = createTime
                }
                downloadUserNameFirstData(serverURL)
            } catch (e: Exception) {
                // println(e)
                e.message
            }
        }
        return try {
            applyUserNameJson(response)
            saveUserNameData()
            null
        } catch (e: Exception) {
            // println(e)
            e.message
        }
    }

    private suspend fun downloadUserNameFirstData(serverURL: String): String? {
        val url = serverURL + "usernameJson${userNameDatas.usernameJsonNumCount}"
        val response = withContext(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }
            ?: return "error"
        val hasNext = response.endsWith("//next")

        try {
            applyUserNameJson(response.removeSuffix("//next"))
            userNameDatas.usernameJsonNumCount = if (hasNext) userNameDatas.usernameJsonNumCount + 1 else -1
            saveUserNameData()
        } catch (e: Exception) {
            // println(e)
            return e.message
        }
        // 分割データは1回につき1件取得し、最後の分割を反映した後だけ差分を取得する。
        return if (hasNext) null else downloadUserNameData(afterFirstData = true)
    }

    private suspend fun applyUserNameJson(jsonText: String) {
        withContext(Dispatchers.IO) { userNameDatas.importResponse(jsonText) }
    }

    private fun saveUserNameData() {
        USERDATA.UserNamesServerJsonNumCount = userNameDatas.usernameJsonNumCount
        USERDATA.UserNamesServerJsonCreateTime = userNameDatas.usernameJsonCreateTime
    }
    fun Chance_DownloadUserNameData_FirstData(callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        if( UserNameDataLists.usernameJsonNumCount == -1 ){
            callback("")
            return@launch
        }
        DownloadUserNameData(callback)
    }

    fun GetMusicScoreData(musicID:Int, userID:String, callback: (JsonArray?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val url = GLOBAL.PHP_URL + "?req=mscore&musicID=${musicID}&userID=${userID}"
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(url, noCache = true) }.await().let {
            if (it == null) {
                callback(null)
                return@let
            }
            // println("json url = ${url}")
            val jsonArray: JsonArray
            try {
                jsonArray = Json.parse("[${it}]").asArray()

                // println("json toreta ${it}")
            } catch (e: Exception) {
                // println("json sippai ${it}")
                // println(e)
                callback(null)
                return@let
            }
            callback(jsonArray)
        }
    }

    fun postUserName(name:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        val success = try {
            val url = GLOBAL.PHP_URL
            val body = "req=userName-add&id=${userID}&name=${URLEncoder.encode(name,"utf-8")}"
            val response = withContext(Dispatchers.IO) {
                HttpUtil.httpPOST(url, body, noCache = true)
            }
            if (response?.startsWith("success userName-add UserNameSqlID=") == true) {
                USERDATA.UserNameID = response.pregMatche_firstString("UserNameSqlID=(\\d+)").toInt()
            }
            response?.startsWith("success userName-add") == true
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        callback(success)
    }
    //Ver.1.4未満のケア
    fun GetUserNameSqlID(userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        if( USERDATA.UserName == "" || USERDATA.UserNameID != 0 ){
            // println("usernameid=${USERDATA.UserNameID}")
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
            // println("postRet="+it)
            if (it.startsWith("success UserNameSqlID=")) {
                USERDATA.UserNameID = it.pregMatche_firstString("UserNameSqlID=(\\d+)").toInt()
            }
            callback(true)
        }
    }
    fun postScoreData(scoreset:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=scorem-add&userID=${userID}&userNameID=${USERDATA.UserNameID}&scoreset=${scoreset}&pass=${Crypt.encryptx_urlsafe("ニコFlick", userID)}"
        // println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            // println("postRet="+it)
            if (it == "success score-add") {
                callback(true)
            } else {
                // println("スコア送信失敗")
                callback(false)
            }
        }
    }
    fun postUserCountData() {
        // One request per counter; retain new changes made while a request is in flight.
        if (postingCounts) return
        postingCounts = true
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val requests = listOf(
                    Triple("playcount-add", "playcountset", "PlayCount"),
                    Triple("likecount-add", "likecountset", "LikeCount"),
                    Triple("dislikecount-add", "dislikecountset", "DislikeCount"),
                    Triple("musicfavorite-add", "musicfavoritecountset", "MusicFavoriteCount"))
                for ((req, key, storage) in requests) {
                    val counter = PendingCounter(storage)
                    val snapshot = counter.counter.toMap()
                    if (snapshot.isEmpty()) continue
                    val body = "req=$req&$key=" + URLEncoder.encode(PendingCounter.encode(snapshot), "UTF-8")
                    val result = async(Dispatchers.Default) { HttpUtil.httpPOST(GLOBAL.PHP_URL, body, noCache = true) }.await()
                    if (result?.trim() == "success $req") counter.acknowledge(snapshot)
                }
            } finally { postingCounts = false }
        }
    }
    companion object {
        private var postingCounts = false
    }

    fun postComment(comment:String, levelID: Int, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=comment-add&userID=${userID}&levelID=${levelID}&comment=${URLEncoder.encode(comment, "UTF-8")}"
        // println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            // println("postRet="+it)
            if (it == "success comment-add") {
                callback(true)
            } else {
                // println("コメント送信失敗")
                callback(false)
            }
        }
    }
    fun postMusicTagUpdate(id:Int, tags:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=musicTag-update&id=${id}&tags=${URLEncoder.encode(tags,"utf-8")}&userID=${userID}&pass=${Crypt.encryptx_urlsafe("ニコFlick", userID)}"
        // println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            // println("postRet="+it)
            if (it == "success musictag-update") {
                callback(true)
            } else {
                // println("musicタグ送信失敗")
                callback(false)
            }
        }
    }

    fun postReport(musicID:Int, comment:String, userID:String, callback: (Boolean) -> Unit) = GlobalScope.launch(Dispatchers.Main) {
        //  登録
        val url = GLOBAL.PHP_URL
        val body = "req=report&userID=${userID}&musicID=${musicID}&comment=${URLEncoder.encode(comment, "UTF-8")}"
        // println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            // println("postRet="+it)
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
        // println("postbody="+body)
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, noCache = true) }.await().let {
            if (it == null) {
                callback(false)
                return@let
            }
            // println("postRet="+it)
            callback(true)
        }
    }
}