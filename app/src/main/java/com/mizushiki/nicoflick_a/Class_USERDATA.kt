package com.mizushiki.nicoflick_a

import android.content.Context
import android.content.SharedPreferences
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.properties.Delegates


// object Userdata
// class SelectConditions(_tags:String, _sortItem:String)

object USERDATA {

    // 必ず GLOBAL.APPLICATIONCONTEXTが入力されてからシングルトンを作る
    // １つで管理してたらやたら重くなったので分割。本当はでかいのはファイル保存とかが望ましい。
    // nameでファイル単位とかになるのかと思いきや全てを纏めたxmlらしい。(ということはJsonのエンコード・デコードが延々と繰り返されていることに・・・)
    val dataStore: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore", Context.MODE_PRIVATE)
    val dataStore_Util: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_Util", Context.MODE_PRIVATE)
    val dataStore_UserScore: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_UserScore", Context.MODE_PRIVATE)
    val dataStore_MusicsJson: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_MusicsJson", Context.MODE_PRIVATE)
    val dataStore_LevelsJson: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_LevelsJson", Context.MODE_PRIVATE)
    val dataStore_UserNamesJson: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_UserNamesJson", Context.MODE_PRIVATE)
    val dataStore_ScoresJson: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_ScoresJson", Context.MODE_PRIVATE)
    val dataStore_CommentsJson: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore_CommentsJson", Context.MODE_PRIVATE)

//DataStore_Original
    //MyVersion
    //  バージョンアップ時に必要な処理があれば実行するため
    var MyVersion:Int = 0
        get() = dataStore.getInt("MyVersion", GLOBAL.Version)
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
    //cachedMoviesNum
    var cachedMovieNum:Int = 3
        get() = dataStore.getInt("cachedMovieNum",3)
        set(value) {
            dataStore.edit().putInt("cachedMovieNum",value).commit()
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
    var thumbMoviePlay:Boolean = false
        get() = dataStore.getBoolean("thumbMoviePlay",false)
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
    //ヘルプを見たかどうか
    var lookedOtherIme:Boolean = false
        get() = dataStore.getBoolean("LookedOtherIme",false)
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
        get() = dataStore.getBoolean("lookedDesignCoop_v1900",false)
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
            for( data in usString.split("|") ){
                val sp = data.split(",")
                if( sp.size != 4 ){ break }
                userScore.scores[sp[0].toInt()] = arrayListOf(sp[1].toInt(),sp[2].toInt(),sp[3].toInt())
                // userScore.scores[levelID] = arrayListOf(score,rank,flg)
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

//DataStore_Util
    //JustPlayedScore
    var JustPlayedScore:UserJustPlayedScore
        get() {
            val usString = dataStore_Util.getString("JustPlayedScore","")!!
            val userScore = UserJustPlayedScore()
            for( data in usString.split("|") ){
                val sp = data.split(",")
                if( sp.size != 4 ){ break }
                userScore.scores[sp[0].toInt()] = arrayListOf(sp[1].toInt(),sp[2].toInt(),sp[3].toInt())
                // userScore.scores[levelID] = arrayListOf(score,rank,flg)
            }
            return userScore
        }
        set(value) {
            var usString=""
            for( (id,scoreData) in value.scores ){
                usString += "$id,${scoreData[0]},${scoreData[1]},${scoreData[2]}|"
            }
            dataStore_Util.edit().putString("JustPlayedScore",usString).commit()
            println("saved JustPlayUserScore")
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
            println("MyFavo保存 $s")
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
            println("MyFavo2保存 $s")
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
    //tagやソートの設定
    var SelectedMusicCondition:SelectConditions = SelectConditions("","")
        get() {
            println("field.sortItem="+field.sortItem)
            if( field.sortItem == "" ){
                val tags = dataStore_Util.getString("SelectConditionsTags","@初期楽曲")!!
                val sortItem = dataStore_Util.getString("SelectConditionsSortItem","曲の投稿が古い順")!!
                field = SelectConditions(tags,sortItem)
                println("SMC field = "+field)
            }
            return field
        }
        set(value) {
            field = value
            val editor = dataStore_Util.edit()
            editor.putString("SelectConditionsTags",value.tags)
            editor.putString("SelectConditionsSortItem",value.sortItem)
            editor.commit()
            println("SMC field保存")
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
            println("judge set")
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
            println("report set")
            dataStore_Util.edit().putString("RepotedMusicID",value.joinToString(",")).commit()
            field = value
        }


//DataStore_MusicsJson
    var MusicsJson:String = ""
        get() = dataStore_MusicsJson.getString("MusicsJson","")!!
        set(value) {
            dataStore_MusicsJson.edit().putString("MusicsJson",value).commit()
            field = value
        }

//DataStore_LevelsJson
    var LevelsJson:String = ""
        get() = dataStore_LevelsJson.getString("LevelsJson","")!!
        set(value) {
            dataStore_LevelsJson.edit().putString("LevelsJson",value).commit()
            field = value
        }

//DataStore_UserNamesJson
    var UserNamesJson:String = ""
        get() = dataStore_UserNamesJson.getString("UserNamesJson","")!!
        set(value) {
            dataStore_UserNamesJson.edit().putString("UserNamesJson",value).commit()
            field = value
        }

//DataStore_ScoresJson
    var ScoresJson:String = ""
        get() = dataStore_ScoresJson.getString("ScoresJson","")!!
        set(value) {
            dataStore_ScoresJson.edit().putString("ScoresJson",value).commit()
            field = value
        }

//DataStore_CommentsJson
    var CommentsJson:String = ""
        get() = dataStore_CommentsJson.getString("CommentsJson","")!!
        set(value) {
            dataStore_CommentsJson.edit().putString("CommentsJson",value).commit()
            field = value
        }

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
        val sortItem = dataStore.getString("SelectConditionsSortItem","曲の投稿が古い順")!!
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

        //DataStore_MusicsJson
        val musiJson = dataStore.getString("MusicsJson","")!!
        MusicsJson = musiJson

        //DataStore_LevelsJson
        val leveJson = dataStore.getString("LevelsJson","")!!
        LevelsJson = leveJson

        //DataStore_UserNamesJson
        val useNamJson = dataStore.getString("UserNamesJson","")!!
        UserNamesJson = useNamJson

        //DataStore_ScoresJson
        val scoJson = dataStore.getString("ScoresJson","")!!
        ScoresJson = scoJson

        //DataStore_CommentsJson
        val commeJson = dataStore.getString("CommentsJson","")!!
        CommentsJson = commeJson

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

        println("finish! _ Migration__Version1_9_0_0to1_10_0_0")
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

        println("finish! _ SikkariRemove__Version1_9_0_0to1_10_0_0")
    }
}

class UserScore {
    var scores:MutableMap<Int, ArrayList<Int>> = mutableMapOf()
    companion object {
        const val SCORE = 0
        const val RANK = 1
        const val FLG = 2
    }

    fun setScore(levelID:Int, score:Int, rank:Int) : Boolean{
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
        return sup
    }
    //データベースに送るスコアセット文字列
    fun getSendScoresStr() : String {
        var scoreset = ""
        for( (levelID,us) in scores ){
            if( us[Companion.FLG] == 0 ){
                val musicID = MusicDataLists.getLevelIDtoMusicID(levelID)
                scoreset += "<$levelID,$musicID,${us[Companion.SCORE]}>"
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

    fun setScore(levelID:Int, score:Int, rank:Int){
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
        return
    }
    //データベースに送るスコアセット文字列
    fun appendSendScoresStr(scoreset: String) : String {
        var scoreset = scoreset
        var scoresetIDs:ArrayList<Int> = arrayListOf()
        var ans:ArrayList<String> = arrayListOf()
        if( scoreset.pregMatches(pattern= "<(\\d+),\\d+,\\d+>", matches= ans)){
            for( i in 0 until ans.size/2 ) {
                val n = ans[i * 2 + 1].toInt()
                scoresetIDs.add(n)
            }
        }
        for( (levelID,us) in scores ){
            if( us[Companion.FLG] == 0 ){
                if( scoresetIDs.contains(levelID) ){ continue }
                val musicID = MusicDataLists.getLevelIDtoMusicID(levelID)
                scoreset += "<$levelID,$musicID,${us[Companion.SCORE]}>"
            }
        }
        return scoreset
    }
    fun clearSendedData(){
        println("clear sended data")
        //保存
        USERDATA.JustPlayedScore = UserJustPlayedScore()
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
        "曲の投稿が新しい順",
        "曲の投稿が古い順",
        "ゲームの投稿が新しい曲順",
        "ゲームの投稿が古い曲順",
        "ゲームプレイ回数が多い曲順",
        "ゲームプレイ回数が少ない曲順",
        "お気に入り数が多い曲順",
        "お気に入り数が少ない曲順",
        "動画IDが大きい順",
        "動画IDが小さい順",
        "最近ハイスコアが更新された曲順",
        "最近コメントされた曲順")

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
        println("tag.size="+tag.size)
    }

    private fun setTag(){
        tag = arrayListOf()
        println("settag->"+tags)
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
            println("set= "+tag.last().type+", "+tag.last().word)
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