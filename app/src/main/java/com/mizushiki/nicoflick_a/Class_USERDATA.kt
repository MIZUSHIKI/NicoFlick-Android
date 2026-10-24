package com.mizushiki.nicoflick_a

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.properties.Delegates


// object Userdata
// class SelectConditions(_tags:String, _sortItem:String)

object USERDATA {

    // 旧SharedPreferencesはLocalStorageの初回接続時にSQLiteへ移行する。
    val dataStore = SQLitePreferences(LocalStorage.database(), "DataStore")
    val dataStore_Util = SQLitePreferences(LocalStorage.database(), "DataStore_Util")
    val dataStore_UserScore = SQLitePreferences(LocalStorage.database(), "DataStore_UserScore")

//DataStore_Original
    //MyVersion
    //  バージョンアップ時に必要な処理があれば実行するため
    var MyVersion:Int = 0
        get() = dataStore.getInt("MyVersion", 0)
        set(value) {
            dataStore.edit().putInt("MyVersion",value).commit()
            field = value
        }
    //ユニークユーザーID
    val UserID: String
        get() {
            var uuid:String = dataStore.getString("UniqueUserID","")!!
            if(uuid == "") {
                uuid = UUID.randomUUID().toString()
                dataStore.edit().putString("UniqueUserID",uuid).commit()
            }
            return uuid
        }
    //ユニークユーザーIDxxx
    val UserIDxxx:String
        get() {
            val uuid:String = dataStore.getString("UniqueUserID","")!!
            if(uuid != "") {
                return uuid.substring(0..23) + "************"
            }
            return ""
        }
    //ユーザーネーム
    var UserName:String = "NO_NAME"
        get() = dataStore.getString("UserName","NO_NAME")!!
        set(value) {
            dataStore.edit().putString("UserName",value).commit()
            field = value
        }
    var UserNameID:Int = 0
        get() = dataStore.getInt("UserNameID",0)
        set(value) {
            dataStore.edit().putInt("UserNameID",value).commit()
            field = value
        }
    //ニコニコ動画： mail
    var NicoMail:String = ""
        get() = dataStore.getString("NicoMail","")!!
        set(value) {
            dataStore.edit().putString("NicoMail",value).commit()
            field = value
        }
    //ニコニコ動画： pass
    var NicoPass:String = ""
        get() = dataStore.getString("NicoPass","")!!
        set(value) {
            dataStore.edit().putString("NicoPass",value).commit()
            field = value
        }
    //ゲーム画面での前回のキーボード高さ
    var maeKeyboardTop:Int = 0
        get() = dataStore.getInt("maeKeyboardTop",0)
        set(value) {
            dataStore.edit().putInt("maeKeyboardTop",value).commit()
            field = value
        }
    //volume movie
    var SoundVolumeMovie:Float = 1.0f
        get() = dataStore.getFloat("SoundVolumeMovie",1.0f)
        set(value) {
            dataStore.edit().putFloat("SoundVolumeMovie",value).commit()
            field = value
        }
    //volume GameSE
    var SoundVolumeGameSE:Float = 1.0f
        get() = dataStore.getFloat("SoundVolumeGameSE",1.0f)
        set(value) {
            dataStore.edit().putFloat("SoundVolumeGameSE",value).commit()
            field = value
        }
    //volume SystemSE
    var SoundVolumeSystemSE:Float = 1.0f
        get() = dataStore.getFloat("SoundVolumeSystemSE",1.0f)
        set(value) {
            dataStore.edit().putFloat("SoundVolumeSystemSE",value).commit()
            field = value
        }
    //曲セレクト画面で30秒プレビューを見るかどうか
    var thumbMoviePlay:Boolean = true
        get() = dataStore.getBoolean("thumbMoviePlay",true)
        set(value) {
            dataStore.edit().putBoolean("thumbMoviePlay",value).commit()
            field = value
        }
    //time3sec
    var Time3secExitZureTime:Float = 0.0f
        get() = dataStore.getFloat("Time3secExitZureTime",0.0f)
        set(value) {
            if( abs(value) < 0.2 ) {
                dataStore.edit().putFloat("Time3secExitZureTime", value).commit()
                field = value
            }else {
                dataStore.edit().putFloat("Time3secExitZureTime", 0.0f).commit()
                field = value
            }
        }
    //プレイ方法、本体キーボード設定のヘルプを見たかどうか
    var lookedHelp:Boolean = false
        get() = dataStore.getBoolean("LookedHelp",false)
        set(value) {
            dataStore.edit().putBoolean("LookedHelp",value).commit()
            field = value
        }
    //プレイ方法、本体キーボード設定のヘルプを見たかどうか
    var lookedExtend:Boolean = false
        get() = dataStore.getBoolean("LookedExtend",false)
        set(value) {
            dataStore.edit().putBoolean("LookedExtend",value).commit()
            field = value
        }
    //ヘルプを見たかどうか ()
    var lookedOtherIme:Boolean = true
        get() = dataStore.getBoolean("LookedOtherIme",true)
        set(value) {
            dataStore.edit().putBoolean("LookedOtherIme",value).commit()
            field = value
        }
    //ver1.5でのお気に入り仕様変更について見たかどうか
    var lookedChangeFavoSpec_v1500:Boolean = false
        get() = dataStore.getBoolean("lookedChangeFavoSpec_v1500",true)
        set(value) {
            dataStore.edit().putBoolean("lookedChangeFavoSpec_v1500",value).commit()
            field = value
        }
    //デザイン協力募集中
    var lookedDesignCoop_v1900:Boolean = false
        get() = dataStore.getBoolean("lookedDesignCoop_v1900",true)
        set(value) {
            dataStore.edit().putBoolean("lookedDesignCoop_v1900",value).commit()
            field = value
        }
    //
    var PlayerHeightPer:Int = 10000
        get() = dataStore.getInt("PlayerHeightPer",10000)
        set(value) {
            dataStore.edit().putInt("PlayerHeightPer",value).commit()
            field = value
        }
    //
    var UseNicoFlickKeyboard:Boolean = false
        get() = dataStore.getBoolean("UseNicoFlickKeyboard", false)
        set(value) {
            dataStore.edit().putBoolean("UseNicoFlickKeyboard",value).commit()
            field = value
        }
    //
    var NicoFlickKeyboardHeightPer:Int = 5000
        get() = dataStore.getInt("NicoFlickKeyboardHeightPer", 5000)
        set(value) {
            dataStore.edit().putInt("NicoFlickKeyboardHeightPer",value).commit()
            field = value
        }
    var NicoFlickKeyboardSense:Int = 50
        get() = dataStore.getInt("NicoFlickKeyboardSense", 50)
        set(value) {
            dataStore.edit().putInt("NicoFlickKeyboardSense",value).commit()
            field = value
        }
    var BorderPosUe:Boolean = false
        get() = dataStore.getBoolean("BorderPosUe",false)
        set(Value) {
            dataStore.edit().putBoolean("BorderPosUe",Value).commit()
            field = Value
        }

//DataStore_UserScore
    //UserScore
    var Score:UserScore
        get() {
            val usString = dataStore_UserScore.getString("UserScore","")!!
            val userScore = UserScore()
            userScore.scores = readSavedScoreRecords(usString) {
                if (!dataStore_UserScore.contains("UserScoreRecoveryBackup")) {
                    dataStore_UserScore.edit().putString("UserScoreRecoveryBackup", usString).commit()
                }
            }
            return userScore
        }
        set(value) {
            var usString=""
            for( (id,scoreData) in value.scores ){
                usString += "$id,${scoreData[0]},${scoreData[1]},${scoreData[2]}|"
            }
            dataStore_UserScore.edit().putString("UserScore",usString).commit()
        }

    // Key: levelID、Value: 保存スコアを記録したときの難易度。
    // getterはコピーを返すため、変更後はsetterへ代入する。
    var ScoreLevel:MutableMap<Int,Int>
        get() = readCountMap("ScoreLevel")
        set(value) = writeCountMap("ScoreLevel", value)
    var JustPlayedScoreLevel:MutableMap<Int,Int>
        get() = readCountMap("JustPlayedScoreLevel")
        set(value) = writeCountMap("JustPlayedScoreLevel", value)

    // 新規ユーザーは移行不要。旧バージョンからの更新時にMainでfalseにする。
    var doneScoreLevelUpdate_v2000:Boolean
        get() = dataStore_Util.getBoolean("doneScoreLevelUpdate_v2000", true)
        set(value) { dataStore_Util.edit().putBoolean("doneScoreLevelUpdate_v2000", value).commit() }

    // selectorで譜面データ読み込み後に一度だけ移行する。
    fun migrateScoreLevelsV2000() {
        if (doneScoreLevelUpdate_v2000 || MusicDataLists.levels.isEmpty()) return
        val currentLevels = MusicDataLists.levels.values.flatten().associate { it.sqlID to it.level }
        val scoreLevels = migratedScoreLevels(Score.scores.keys, ScoreLevel, currentLevels)
        val justPlayedLevels = migratedScoreLevels(JustPlayedScore.scores.keys, JustPlayedScoreLevel, currentLevels)
        // 両方の難易度と完了フラグを同時に保存する。
        dataStore_Util.edit()
            .putString("ScoreLevel", scoreLevels.entries.joinToString("|") { "${it.key},${it.value}" })
            .putString("JustPlayedScoreLevel", justPlayedLevels.entries.joinToString("|") { "${it.key},${it.value}" })
            .putBoolean("doneScoreLevelUpdate_v2000", true)
            .commit()
    }

//DataStore_Util
    //JustPlayedScore
    var JustPlayedScore:UserJustPlayedScore
        get() {
            val usString = dataStore_Util.getString("JustPlayedScore","")!!
            val userScore = UserJustPlayedScore()
            userScore.scores = readSavedScoreRecords(usString) {
                if (!dataStore_Util.contains("JustPlayedScoreRecoveryBackup")) {
                    dataStore_Util.edit().putString("JustPlayedScoreRecoveryBackup", usString).commit()
                }
            }
            return userScore
        }
        set(value) {
            var usString=""
            for( (id,scoreData) in value.scores ){
                usString += "$id,${scoreData[0]},${scoreData[1]},${scoreData[2]}|"
            }
            dataStore_Util.edit().putString("JustPlayedScore",usString).commit()
            // println("saved JustPlayUserScore")
        }
    //MyFavorite
    var MyFavorite:MutableSet<Int>
        get() {
            val ids = dataStore_Util.getString("MyFavorite","")!!
            if( ids == "" ){
                return mutableSetOf()
            }
            val s = mutableSetOf<Int>()
            s.addAll( ids.split(",").map{ Int(it) } )
            return s
        }
        set(value) {
            val s = value.map { String(it) }.joinToString(separator = ",")
            // println("MyFavo保存 $s")
            dataStore_Util.edit().putString("MyFavorite",s).commit()
        }
    //MyFavorite2
    var MyFavorite2:MutableSet<Int>
        get() {
            val ids = dataStore_Util.getString("MyFavorite2","")!!
            if( ids == "" ){
                return mutableSetOf()
            }
            val s = mutableSetOf<Int>()
            s.addAll( ids.split(",").map{ Int(it) } )
            return s
        }
        set(value) {
            val s = value.map { String(it) }.joinToString(separator = ",")
            // println("MyFavo2保存 $s")
            dataStore_Util.edit().putString("MyFavorite2",s).commit()
        }
    val MyFavoriteAll:Set<Int>
        get() = MyFavorite.union(MyFavorite2)

    //PlayCounter
    var PlayCount:PlayCounter
        get() {
            val usString = dataStore_Util.getString("PlayCount","")!!
            val playCounter = PlayCounter()
            for( data in usString.split("|") ){
                val sp = data.split(",")
                if( sp.size != 2 ){ break }
                playCounter.counter[sp[0].toInt()] = sp[1].toInt()
                // playCounter.counter[levelID] = playCount
            }
            return playCounter
        }
        set(value) {
            var usString=""
            for( (id,playCount) in value.counter ){
                usString += "$id,$playCount|"
            }
            dataStore_Util.edit().putString("PlayCount",usString).commit()
        }
    //FavoriteCounter
    var FavoriteCount:FavoriteCounter
        get() {
            val usString = dataStore_Util.getString("FavoriteCount","")!!
            val favoriteCounter = FavoriteCounter()
            for( data in usString.split("|") ){
                val sp = data.split(",")
                if( sp.size != 2 ){ break }
                favoriteCounter.counter[sp[0].toInt()] = sp[1].toInt()
                // playCounter.counter[levelID] = playCount
            }
            return favoriteCounter
        }
        set(value) {
            var usString=""
            for( (id,favoriteCount) in value.counter ){
                usString += "$id,$favoriteCount|"
            }
            dataStore_Util.edit().putString("FavoriteCount",usString).commit()
        }
    // v2: music favorites, level ratings, and play history use independent keys.
    private fun readIDs(key:String):MutableSet<Int> =
        dataStore_Util.getString(key, "")!!.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
    private fun writeIDs(key:String, value:Set<Int>) {
        dataStore_Util.edit().putString(key, value.sorted().joinToString(",")).commit()
    }
    var MyFavorite3:MutableSet<Int>
        get() = readIDs("MyFavorite3")
        set(value) = writeIDs("MyFavorite3", value)
    // Key: levelID。日時はUnix epochからのミリ秒で保存する。
    // getterはコピーを返すため、変更したMapはsetterへ代入して保存する。
    var levelPlayHistories:MutableMap<Int,LevelPlayHistory>
        get() {
            val histories = mutableMapOf<Int,LevelPlayHistory>()
            dataStore_Util.getString("levelPlayHistories", "")!!.split("|").forEach {
                val parts = it.split(",")
                if (parts.size == 3) {
                    val levelID = parts[0].toIntOrNull()
                    val playCount = parts[1].toIntOrNull()
                    val lastPlayedAt = parts[2].toLongOrNull()
                    if (levelID != null && playCount != null && playCount >= 0 && lastPlayedAt != null) {
                        histories[levelID] = LevelPlayHistory(playCount, Date(lastPlayedAt))
                    }
                }
            }
            return histories
        }
        set(value) {
            val saved = value.entries.joinToString("|") {
                "${it.key},${it.value.playCount},${it.value.lastPlayedAt.time}"
            }
            dataStore_Util.edit().putString("levelPlayHistories", saved).commit()
        }

    // 1プレイにつき1回呼び出す。初回は1、以降は回数を加算し最終日時を更新する。
    @Synchronized
    fun recordLevelPlay(levelID:Int, playedAt:Date = Date()) {
        val histories = levelPlayHistories
        val playCount = (histories[levelID]?.playCount ?: 0) + 1
        histories[levelID] = LevelPlayHistory(playCount, Date(playedAt.time))
        levelPlayHistories = histories
    }

    fun readCountMap(key:String):MutableMap<Int,Int> {
        val result = mutableMapOf<Int,Int>()
        dataStore_Util.getString(key, "")!!.split("|").forEach {
            val parts = it.split(",")
            if (parts.size == 2) {
                val id = parts[0].toIntOrNull(); val value = parts[1].toIntOrNull()
                if (id != null && value != null) result[id] = value
            }
        }
        return result
    }
    fun writeCountMap(key:String, values:Map<Int,Int>) {
        dataStore_Util.edit().putString(key, values.entries.joinToString("|") { "${it.key},${it.value}" }).commit()
    }
    var MyLike:MutableMap<Int,Int>
        get() = readCountMap("MyLike")
        set(value) = writeCountMap("MyLike", value)
    val MusicFavoriteCount get() = PendingCounter("MusicFavoriteCount")
    val LikeCount get() = PendingCounter("LikeCount")
    val DislikeCount get() = PendingCounter("DislikeCount")
    var doneFavoriteUpdate_v2000:Boolean
        get() = dataStore_Util.getBoolean("doneFavoriteUpdate_v2000", false)
        set(value) { dataStore_Util.edit().putBoolean("doneFavoriteUpdate_v2000", value).commit() }
    var lookedNameRegistrationIfNoName_v2000:Boolean
        get() = dataStore_Util.getBoolean("lookedNameRegistrationIfNoName_v2000", false)
        set(value) { dataStore_Util.edit().putBoolean("lookedNameRegistrationIfNoName_v2000", value).commit() }
    var fitsMovieToKeyboardHeight:Boolean
        get() = dataStore_Util.getBoolean("fitsMovieToKeyboardHeight", true)
        set(value) { dataStore_Util.edit().putBoolean("fitsMovieToKeyboardHeight", value).commit() }
    var showOffsetLabel:Boolean
        get() = dataStore_Util.getBoolean("showOffsetLabel", false)
        set(value) { dataStore_Util.edit().putBoolean("showOffsetLabel", value).commit() }
    var UseNewLevelCalculationKey:Boolean
        get() = dataStore_Util.getBoolean("UseNewLevelCalculationKey", false)
        set(value) { dataStore_Util.edit().putBoolean("UseNewLevelCalculationKey", value).commit() }

    var ShowGameSettings:Boolean
        get() = dataStore_Util.getBoolean("ShowGameSettings", false)
        set(value) { dataStore_Util.edit().putBoolean("ShowGameSettings", value).commit() }

    // Run after music/level metadata has loaded; never send migration as new votes.
    fun migrateFavoritesV2000() {
        if (doneFavoriteUpdate_v2000 || MusicDataLists.levels.isEmpty()) return
        val favorites = MyFavorite3; val likes = MyLike
        var unresolved = false
        for (levelID in MyFavoriteAll) {
            val musicID = MusicDataLists.getLevelIDtoMusicID(levelID)
            if (musicID == 0) { unresolved = true; continue }
            favorites.add(musicID)
            if (!likes.containsKey(levelID)) likes[levelID] = -2
        }
        MyFavorite3 = favorites; MyLike = likes;
        if (!unresolved) doneFavoriteUpdate_v2000 = true
    }

    //tagやソートの設定
    var SelectedMusicCondition:SelectConditions = SelectConditions("","")
        get() {
            // println("field.sortItem="+field.sortItem)
            if( field.sortItem == "" ){
                val tags = dataStore_Util.getString("SelectConditionsTags","@初期楽曲")!!
                val sortItem = dataStore_Util.getString("SelectConditionsSortItem","曲の投稿日時 昇順")!!
                field = SelectConditions(tags,sortItem)
                // println("SMC field = "+field)
            }
            return field
        }
        set(value) {
            field = value
            val editor = dataStore_Util.edit()
            editor.putString("SelectConditionsTags",value.tags)
            editor.putString("SelectConditionsSortItem",value.sortItem)
            editor.commit()
            // println("SMC field保存")
        }
    //
    var LevelSortCondition:Int = 0
        get() = dataStore_Util.getInt("LevelSortCondition",0)
        set(value) {
            dataStore_Util.edit().putInt("LevelSortCondition",value).commit()
            field = value
        }
    var MusicSortCondition:Int = 0
        get() = dataStore_Util.getInt("MusicSortCondition",0)
        set(value) {
            dataStore_Util.edit().putInt("MusicSortCondition",value).commit()
            field = value
        }
    //ジャッジオフセット
    var JudgeOffset:MutableMap<Int,Float>
        get() {
            val usString = dataStore_Util.getString("JudgeOffset","")!!
            val judgeMap: MutableMap<Int,Float> = mutableMapOf()
            for( data in usString.split("|") ){
                val sp = data.split(",")
                if( sp.size != 2 ){ break }
                judgeMap[sp[0].toInt()] = sp[1].toFloat()
                // userScore.scores[levelID] = arrayListOf(score,rank,flg)
            }
            return judgeMap
        }
        set(value) {
            // println("judge set")
            var usString=""
            for( (id,offset) in value ){
                usString += "$id,$offset|"
            }
            dataStore_Util.edit().putString("JudgeOffset",usString).commit()
        }
    //
    var UserNamesServerJsonNumCount:Int = 0
        get() = dataStore_Util.getInt("UserNamesServerJsonNumCount",0)
        set(value) {
            dataStore_Util.edit().putInt("UserNamesServerJsonNumCount",value).commit()
            field = value
        }
    //
    var UserNamesServerJsonCreateTime:Int = 0
        get() = dataStore_Util.getInt("UserNamesServerJsonCreateTime",0)
        set(value) {
            dataStore_Util.edit().putInt("UserNamesServerJsonCreateTime",value).commit()
            field = value
        }
    //
    var RepotedMusicID:MutableList<String> = mutableListOf()
        get() = dataStore_Util.getString("RepotedMusicID","")!!.split(",").toMutableList()
        set(value) {
            // println("report set")
            dataStore_Util.edit().putString("RepotedMusicID",value.joinToString(",")).commit()
            field = value
        }

    // 大量のサーバーデータはMetadataStorage / 各DataListsのSQLiteテーブルで管理する。


//
// DataStore整理
//
    fun Migration__Version1_9_0_0to1_10_0_0(){

        //UserScore
        val usString = dataStore.getString("UserScore","")!!
        val userScore = UserScore()
        for( data in usString.split("|") ){
            val sp = data.split(",")
            if( sp.size != 4 ){ break }
            userScore.scores[sp[0].toInt()] = arrayListOf(sp[1].toInt(),sp[2].toInt(),sp[3].toInt())
        }
        Score = userScore

        //JustPlayedUserScore
        val usString_1 = dataStore.getString("JustPlayedScore","")!!
        val userScore_1 = UserJustPlayedScore()
        for( data in usString_1.split("|") ){
            val sp = data.split(",")
            if( sp.size != 4 ){ break }
            userScore_1.scores[sp[0].toInt()] = arrayListOf(sp[1].toInt(),sp[2].toInt(),sp[3].toInt())
        }
        JustPlayedScore = userScore_1

        //MyFavorite
        val ids = dataStore.getString("MyFavorite","")!!
        if( ids != "" ){
            val s = mutableSetOf<Int>()
            s.addAll( ids.split(",").map{ Int(it) } )
            MyFavorite = s
        }

        //MyFavorite2
        val ids_1 = dataStore.getString("MyFavorite2","")!!
        if( ids_1 != "" ){
            val s = mutableSetOf<Int>()
            s.addAll( ids_1.split(",").map{ Int(it) } )
            MyFavorite2 = s
        }

        //PlayCounter
        val usString_2 = dataStore.getString("PlayCount","")!!
        val playCounter = PlayCounter()
        for( data in usString_2.split("|") ){
            val sp = data.split(",")
            if( sp.size != 2 ){ break }
            playCounter.counter[sp[0].toInt()] = sp[1].toInt()
        }
        PlayCount = playCounter

        //FavoriteCounter
        val usString_3 = dataStore.getString("FavoriteCount","")!!
        val favoriteCounter = FavoriteCounter()
        for( data in usString_3.split("|") ){
            val sp = data.split(",")
            if( sp.size != 2 ){ break }
            favoriteCounter.counter[sp[0].toInt()] = sp[1].toInt()
        }
        FavoriteCount = favoriteCounter

        //SelectedMusicCondition
        val tags = dataStore.getString("SelectConditionsTags","@初期楽曲")!!
        val sortItem = dataStore.getString("SelectConditionsSortItem","曲の投稿日時 昇順")!!
        val selecon = SelectConditions(tags,sortItem)
        SelectedMusicCondition = selecon

        //LevelSortCondition
        val levsocon = dataStore.getInt("LevelSortCondition",0)
        LevelSortCondition = levsocon

        //MusicSortCondition
        val musisocon = dataStore.getInt("MusicSortCondition",0)
        MusicSortCondition = musisocon

        //JudgeOffset
        val usString_4 = dataStore.getString("JudgeOffset","")!!
        val judgeMap: MutableMap<Int,Float> = mutableMapOf()
        for( data in usString_4.split("|") ){
            val sp = data.split(",")
            if( sp.size != 2 ){ break }
            judgeMap[sp[0].toInt()] = sp[1].toFloat()
        }
        JudgeOffset = judgeMap

        //UserNamesServerJsonNumCount
        val usernamesjsoncount = dataStore.getInt("UserNamesServerJsonNumCount",0)
        UserNamesServerJsonNumCount = usernamesjsoncount

        //UserNamesServerJsonCreateTime
        val usernamesjsontime = dataStore.getInt("UserNamesServerJsonCreateTime",0)
        UserNamesServerJsonCreateTime = usernamesjsontime

        //RepotedMusicID
        val repoMuIDs = dataStore.getString("RepotedMusicID","")!!.split(",").toMutableList()
        RepotedMusicID = repoMuIDs

        // 旧ルート内のJSONもSQLite初回移行時に専用テーブルへ取り込み済み。

        //DataStore_Original DATA_REMOVE
        dataStore.edit().remove("UserScore").commit()
        dataStore.edit().remove("JustPlayedScore").commit()
        dataStore.edit().remove("MyFavorite").commit()
        dataStore.edit().remove("MyFavorite2").commit()
        dataStore.edit().remove("PlayCount").commit()
        dataStore.edit().remove("FavoriteCount").commit()
        dataStore.edit().remove("SelectConditionsTags").commit()
        dataStore.edit().remove("LevelSortCondition").commit()
        dataStore.edit().remove("MusicSortCondition").commit()
        dataStore.edit().remove("JudgeOffset").commit()
        dataStore.edit().remove("UserNamesServerJsonNumCount").commit()
        dataStore.edit().remove("UserNamesServerJsonCreateTime").commit()
        dataStore.edit().remove("RepotedMusicID").commit()
        dataStore.edit().remove("MusicsJson").commit()
        dataStore.edit().remove("LevelsJson").commit()
        dataStore.edit().remove("UserNamesJson").commit()
        dataStore.edit().remove("ScoresJson").commit()
        dataStore.edit().remove("CommentsJson").commit()

        // println("finish! _ Migration__Version1_9_0_0to1_10_0_0")
    }
    fun SikkariRemove__Version1_9_0_0to1_10_0_0(){
        //DataStore_Original DATA_REMOVE
        dataStore.edit().remove("UserScore").commit()
        dataStore.edit().remove("JustPlayedScore").commit()
        dataStore.edit().remove("MyFavorite").commit()
        dataStore.edit().remove("MyFavorite2").commit()
        dataStore.edit().remove("PlayCount").commit()
        dataStore.edit().remove("FavoriteCount").commit()
        dataStore.edit().remove("SelectConditionsTags").commit()
        dataStore.edit().remove("LevelSortCondition").commit()
        dataStore.edit().remove("MusicSortCondition").commit()
        dataStore.edit().remove("JudgeOffset").commit()
        dataStore.edit().remove("UserNamesServerJsonNumCount").commit()
        dataStore.edit().remove("UserNamesServerJsonCreateTime").commit()
        dataStore.edit().remove("RepotedMusicID").commit()
        dataStore.edit().remove("MusicsJson").commit()
        dataStore.edit().remove("LevelsJson").commit()
        dataStore.edit().remove("UserNamesJson").commit()
        dataStore.edit().remove("ScoresJson").commit()
        dataStore.edit().remove("CommentsJson").commit()

        // println("finish! _ SikkariRemove__Version1_9_0_0to1_10_0_0")
    }
}

class UserScore {
    var scores:MutableMap<Int, ArrayList<Int>> = mutableMapOf()
    companion object {
        const val SCORE = 0
        const val RANK = 1
        const val FLG = 2
    }

    fun setScore(levelID:Int, score:Int, rank:Int, level:Int) : Boolean{
        var sup = false
        if( scores[levelID] != null ){
            //あった
            val us = scores[levelID]!!
            if( us[Companion.SCORE] < score ){
                us[Companion.SCORE] = score
                //FalseはHighScoreを保存するけどデータベースには送信しない
                if( rank < Score.RankFalse ){
                    us[Companion.FLG] = 0 //スコア投稿済みかのフラグ
                }else {
                    us[Companion.FLG] = 1 //スコア投稿済みかのフラグ
                }
                sup = true
            }
            if( us[Companion.RANK] > rank ){
                us[Companion.RANK] = rank
            }
        }else {
            //なかった
            //FalseはHighScoreを保存するけどデータベースには送信しない
            if( rank < Score.RankFalse ){
                scores[levelID] = arrayListOf(score, rank, 0)
            }else {
                scores[levelID] = arrayListOf(score, rank, 1)
            }
            sup = true
        }
        //保存
        USERDATA.Score = this
        if (sup) {
            val levels = USERDATA.ScoreLevel
            levels[levelID] = level
            USERDATA.ScoreLevel = levels
        }
        return sup
    }
    private fun bestScoresByMusic():Map<Int,Int> {
        val result = mutableMapOf<Int,Int>()
        for ((levelID, score) in scores) {
            val musicID = MusicDataLists.getLevelIDtoMusicID(levelID)
            if (musicID != 0) result[musicID] = maxOf(result[musicID] ?: 0, score[SCORE])
        }
        return result
    }
    fun getPlayedAllMusicScore():Int = bestScoresByMusic().values.sum()
    fun getPlayedAverageMusicScore():Int {
        val values = bestScoresByMusic().values
        return values.sum() / maxOf(values.size, 1)
    }

    //データベースに送るスコアセット文字列
    fun getSendScoresStr() : String {
        val scoreLevels = USERDATA.ScoreLevel
        var scoreset = ""
        for( (levelID,us) in scores ){
            if( us[Companion.FLG] == 0 ){
                val musicID = MusicDataLists.getLevelIDtoMusicID(levelID)
                val level = scoreLevels[levelID] ?: 0
                scoreset += "<$levelID,$musicID,${us[Companion.SCORE]},$level,${us[Companion.RANK]}>"
            }
        }
        return scoreset
    }
    //スコア送信後、FLGを送信済みにする
    fun setSendedFLG() {
        for( (levelID,us) in scores ){
            if( us[Companion.FLG]==0 ){
                us[Companion.FLG]=1
            }
        }
        //保存
        USERDATA.Score = this
    }
    fun getScore(levelID:Int) : Int {
        return (scores[levelID]!![Companion.SCORE])
    }
    fun getRank(levelID:Int) : Int {
        return (scores[levelID]!![Companion.RANK])
    }

}

//ちょっと酷いが仕方ない
class UserJustPlayedScore {
    var scores:MutableMap<Int, ArrayList<Int>> = mutableMapOf()
    companion object {
        const val SCORE = 0
        const val RANK = 1
        const val FLG = 2
    }

    fun setScore(levelID:Int, score:Int, rank:Int, level:Int){
        val scoreUpdated = scores[levelID]?.get(SCORE)?.let { it < score } ?: true
        if( scores[levelID] != null ){
            //あった
            val us = scores[levelID]!!
            if( us[Companion.SCORE] < score ){
                us[Companion.SCORE] = score
                //FalseはHighScoreを保存するけどデータベースには送信しない
                if( rank < Score.RankFalse ){
                    us[Companion.FLG] = 0 //スコア投稿済みかのフラグ
                }else {
                    us[Companion.FLG] = 1 //スコア投稿済みかのフラグ
                }
            }
            if( us[Companion.RANK] > rank ){
                us[Companion.RANK] = rank
            }
        }else {
            //なかった
            //FalseはHighScoreを保存するけどデータベースには送信しない
            if( rank < Score.RankFalse ){
                scores[levelID] = arrayListOf(score, rank, 0)
            }else {
                scores[levelID] = arrayListOf(score, rank, 1)
            }
        }
        //保存
        USERDATA.JustPlayedScore = this
        if (scoreUpdated) {
            val levels = USERDATA.JustPlayedScoreLevel
            levels[levelID] = level
            USERDATA.JustPlayedScoreLevel = levels
        }
        return
    }
    //データベースに送るスコアセット文字列
    fun appendSendScoresStr(scoreset: String) : String {
        val scoreLevels = USERDATA.JustPlayedScoreLevel
        var scoreset = scoreset
        var scoresetIDs:ArrayList<Int> = arrayListOf()
        var ans:ArrayList<String> = arrayListOf()
        if( scoreset.pregMatches(pattern= "<(\\d+),\\d+,\\d+,-?\\d+,-?\\d+>", matches= ans)){
            for( i in 0 until ans.size/2 ) {
                val n = ans[i * 2 + 1].toInt()
                scoresetIDs.add(n)
            }
        }
        for( (levelID,us) in scores ){
            if( us[Companion.FLG] == 0 ){
                if( scoresetIDs.contains(levelID) ){ continue }
                val musicID = MusicDataLists.getLevelIDtoMusicID(levelID)
                val level = scoreLevels[levelID] ?: 0
                scoreset += "<$levelID,$musicID,${us[Companion.SCORE]},$level,${us[Companion.RANK]}>"
            }
        }
        return scoreset
    }
    fun clearSendedData(){
        // println("clear sended data")
        //保存
        USERDATA.JustPlayedScore = UserJustPlayedScore()
        USERDATA.JustPlayedScoreLevel = mutableMapOf()
    }
    //スコア送信後、FLGを送信済みにする
    fun setSendedFLG() {
        for( (levelID,us) in scores ){
            if( us[Companion.FLG]==0 ){
                us[Companion.FLG]=1
            }
        }
        //保存
        USERDATA.JustPlayedScore = this
    }
    fun getScore(levelID:Int) : Int {
        return (scores[levelID]!![Companion.SCORE])
    }
    fun getRank(levelID:Int) : Int {
        return (scores[levelID]!![Companion.RANK])
    }

}

class PlayCounter {
    var counter:MutableMap<Int,Int> = mutableMapOf()

    fun addPlayCount(levelID:Int){
        //  既にそのlevelのプレイ回数データが有れば、カウント、無ければ作る
        if( counter[levelID] != null ){
            //あった
            counter[levelID] = counter[levelID]!! + 1
        }else {
            //なかった
            counter[levelID] = 1
        }
        //保存
        USERDATA.PlayCount = this
    }
    //データベースに送るプレイ回数セット文字列
    fun getSendPlayCountStr() : String {
        var playcountset = ""
        for( (levelID,pc) in counter ){
            playcountset += "<$levelID,$pc>"
        }
        return playcountset
    }
    //スコア送信後、FLGを送信済みにする
    fun setSended() {
        counter = mutableMapOf() //初期化
        //保存
        USERDATA.PlayCount = this
    }
}

class FavoriteCounter {
    var counter:MutableMap<Int,Int> = mutableMapOf()

    fun addFavoriteCount(levelID:Int){
        if( counter[levelID] != null ){
            //あった
            if( counter[levelID] == -1 ){
                //けど引こうとしていた
                counter.remove(levelID)
            }else {
                return
            }
        }else {
            //なかった
            counter[levelID] = 1
        }
        //保存
        USERDATA.FavoriteCount = this
    }
    fun subFavoriteCount(levelID:Int){
        if( counter[levelID] != null ){
            //あった
            if( counter[levelID] == 1 ){
                //足そうとしていた
                counter.remove(levelID)
            }else {
                return
            }
        }else {
            //なかった
            counter[levelID] = -1
        }
        //保存
        USERDATA.FavoriteCount = this
    }
    //データベースに送るプレイ回数セット文字列
    fun getSendFavoriteCountStr() : String {
        var favoritecountset = ""
        for( (levelID,pc) in counter ){
            favoritecountset += "<$levelID,$pc>"
        }
        return favoritecountset
    }
    //スコア送信後、FLGを送信済みにする
    fun setSended() {
        counter = mutableMapOf() //初期化
        //保存
        USERDATA.FavoriteCount = this
    }
}
class PFCounter {
    var playCounter:MutableMap<Int,Int>  by Delegates.notNull()
    var favoriteCounter:MutableMap<Int,Int>  by Delegates.notNull()
    init {
        playCounter = USERDATA.PlayCount.counter
        favoriteCounter = USERDATA.FavoriteCount.counter
    }
    fun getSendPlayFavoriteCountStr() : String {
        val sets =  playCounter.keys.toMutableSet()
        sets.addAll(favoriteCounter.keys)
        if( sets.size == (playCounter.size + favoriteCounter.size)){
            return ""
        }
        var pfcountset = ""
        for( levelID in sets ){
            val pc = playCounter[levelID] ?: 0
            val fc = favoriteCounter[levelID] ?: 0
            pfcountset += "<${levelID},${pc},${fc}>"
        }
        return pfcountset
    }
}

class SelectConditions(_tags:String="", _sortItem:String="") {

    val SortItem = arrayOf(
        "曲の投稿日時 昇順",
        "曲の投稿日時 降順",
        "ゲームの投稿日時 昇順",
        "ゲームの投稿日時 降順",
        "みんなのプレイ回数が多い曲順",
        "みんなのプレイ回数が少ない曲順",
        "お気に入り数が多い曲順",
        "お気に入り数が少ない曲順",
        "動画ID 昇順",
        "動画ID 降順",
        "最近ハイスコアが更新された曲順",
        "最近コメントされた曲順",
        "最近プレイした曲順",
        "あなたのプレイ回数が多い曲順")

    var tags:String =""
        set(value) {
            field = value
            setTag()
            if(!inited){return}
            USERDATA.SelectedMusicCondition = this
        }
    var sortItem:String = ""
        set(value) {
            field = value
            sortStars = getSortStars(value)
            if(!inited){return}
            USERDATA.SelectedMusicCondition = this
        }
    var tag:ArrayList<tagp> = arrayListOf()
    var sortStars: MutableList<Boolean> = mutableListOf(true,true,true,true,true,true,true,true,true,true,true)
    var inited = false
    init {
        tags = _tags
        sortItem = _sortItem
        sortStars = getSortStars(sortItem)
        inited = true //どうすれば．．．
        //setTag()
        // println("tag.size="+tag.size)
    }

    private fun setTag(){
        tag = arrayListOf()
        // println("settag->"+tags)
        if(tags==""){
            return
        }
        val array = tags.trim().split(" ")
        var type = "or" //1つ目のデフォルトは orで以降のデフォルトは and
        loop@ for( value in array ){
            var v = value
            when(value) {
                "and" -> {
                    type = "and"
                    continue@loop
                }
                "or" -> {
                    type = "or"
                    continue@loop
                }
                "" -> { continue@loop }
                else -> {
                    if(value.substring(0,1)=="-"){
                        v = ""
                    }
                }
            }
            tag.add(tagp(v,type))
            // println("set= "+tag.last().type+", "+tag.last().word)
            type = "and"
        }
    }
    data class tagp(val word:String, val type:String)
    /*
    var sortItemNum: Int
        get() = SortItem.indexOf(sortItem)
        set(value) {
            sortItem = SortItem[value]
            USERDATA.SelectedMusicCondition = this
        }
     */
    fun getSortStarsString(containingStars:String? = null) : String {
        containingStars?.let {
            return it.pregMatche_firstString("( [★☆]{10}[■□])$")
        }
        return sortItem.pregMatche_firstString("( [★☆]{10}[■□])$")
    }
    fun getSortStars(containingStars: String? = null) : MutableList<Boolean> {
        var containingStars = containingStars
        if( containingStars == null ){
            containingStars = sortItem
        }
        val hosi = getSortStarsString(containingStars)
        if( hosi == "" ){
            return mutableListOf(true,true,true,true,true,true,true,true,true,true,true)
        }
        sortStars[0] = hosi.pregMatche("★[★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆][■□]$")
        sortStars[1] = hosi.pregMatche("[★☆]★[★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆][■□]$")
        sortStars[2] = hosi.pregMatche("[★☆][★☆]★[★☆][★☆][★☆][★☆][★☆][★☆][★☆][■□]$")
        sortStars[3] = hosi.pregMatche("[★☆][★☆][★☆]★[★☆][★☆][★☆][★☆][★☆][★☆][■□]$")
        sortStars[4] = hosi.pregMatche("[★☆][★☆][★☆][★☆]★[★☆][★☆][★☆][★☆][★☆][■□]$")
        sortStars[5] = hosi.pregMatche("[★☆][★☆][★☆][★☆][★☆]★[★☆][★☆][★☆][★☆][■□]$")
        sortStars[6] = hosi.pregMatche("[★☆][★☆][★☆][★☆][★☆][★☆]★[★☆][★☆][★☆][■□]$")
        sortStars[7] = hosi.pregMatche("[★☆][★☆][★☆][★☆][★☆][★☆][★☆]★[★☆][★☆][■□]$")
        sortStars[8] = hosi.pregMatche("[★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆]★[★☆][■□]$")
        sortStars[9] = hosi.pregMatche("[★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆]★[■□]$")
        sortStars[10] = hosi.pregMatche("[★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆][★☆]■$")
        return sortStars
    }
}

data class LevelPlayHistory(
    var playCount: Int,
    var lastPlayedAt: Date
)