package com.mizushiki.nicoflick_a

import android.content.Context
import android.content.SharedPreferences
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


// object Userdata
// class SelectConditions(_tags:String, _sortItem:String)

object USERDATA {

    // 必ず GLOBAL.APPLICATIONCONTEXTが入力されてからシングルトンを作る
    val dataStore: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore", Context.MODE_PRIVATE)

    //MyVersion
    //  バージョンアップ時に必要な処理があれば実行するため
    var MyVersion:Int = 0
        get() = dataStore.getInt("MyVersion",0)
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
    var UserName:String = ""
        get() = dataStore.getString("UserName","")!!
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

    var Score:UserScore
        get() {
            val usString = dataStore.getString("UserScore","")!!
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
            dataStore.edit().putString("UserScore",usString).commit()
        }

    //MyFavorite
    var MyFavorite:MutableSet<Int>
        get() {
            val ids = dataStore.getString("MyFavorite","")!!
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
            dataStore.edit().putString("MyFavorite",s).commit()
        }
    //MyFavorite2
    var MyFavorite2:MutableSet<Int>
        get() {
            val ids = dataStore.getString("MyFavorite2","")!!
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
            dataStore.edit().putString("MyFavorite2",s).commit()
        }
    val MyFavoriteAll:Set<Int>
        get() = MyFavorite.union(MyFavorite2)

    //PlayCounter
    var PlayCount:PlayCounter
        get() {
            val usString = dataStore.getString("PlayCount","")!!
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
            dataStore.edit().putString("PlayCount",usString).commit()
        }
    //FavoriteCounter
    var FavoriteCount:FavoriteCounter
        get() {
            val usString = dataStore.getString("FavoriteCount","")!!
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
            dataStore.edit().putString("FavoriteCount",usString).commit()
        }

    //tagやソートの設定
    var SelectedMusicCondition:SelectConditions = SelectConditions("","")
        get() {
            println("field.sortItem="+field.sortItem)
            if( field.sortItem == "" ){
                val tags = dataStore.getString("SelectConditionsTags","@初期楽曲")!!
                val sortItem = dataStore.getString("SelectConditionsSortItem","曲の投稿が古い順")!!
                field = SelectConditions(tags,sortItem)
                println("SMC field = "+field)
            }
            return field
        }
        set(value) {
            field = value
            val editor = dataStore.edit()
            editor.putString("SelectConditionsTags",value.tags)
            editor.putString("SelectConditionsSortItem",value.sortItem)
            editor.commit()
            println("SMC field保存")
        }
    //
    var LevelSortCondition:Int = 0
        get() = dataStore.getInt("LevelSortCondition",0)
        set(value) {
            dataStore.edit().putInt("LevelSortCondition",value).commit()
            field = value
        }
    var MusicSortCondition:Int = 0
        get() = dataStore.getInt("MusicSortCondition",0)
        set(value) {
            dataStore.edit().putInt("MusicSortCondition",value).commit()
            field = value
        }

    //ジャッジオフセット
    var JudgeOffset:MutableMap<Int,Float>
        get() {
            val usString = dataStore.getString("JudgeOffset","")!!
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
            dataStore.edit().putString("JudgeOffset",usString).commit()
        }
    //cachedMoviesNum
    var cachedMovieNum:Int = 20
        get() = dataStore.getInt("cachedMovieNum",20)
        set(value) {
            dataStore.edit().putInt("cachedMovieNum",value).commit()
            field = value
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

    //
    var PlayerHeightPer:Int = 10000
        get() = dataStore.getInt("PlayerHeightPer",10000)
        set(value) {
            dataStore.edit().putInt("PlayerHeightPer",value).commit()
            field = value
        }


    //
    var MusicsJson:String = ""
        get() = dataStore.getString("MusicsJson","")!!
        set(value) {
            dataStore.edit().putString("MusicsJson",value).commit()
            field = value
        }
    var LevelsJson:String = ""
        get() = dataStore.getString("LevelsJson","")!!
        set(value) {
            dataStore.edit().putString("LevelsJson",value).commit()
            field = value
        }
    var UserNamesJson:String = ""
        get() = dataStore.getString("UserNamesJson","")!!
        set(value) {
            dataStore.edit().putString("UserNamesJson",value).commit()
            field = value
        }
    var UserNamesServerJsonNumCount:Int = 0
        get() = dataStore.getInt("UserNamesServerJsonNumCount",0)
        set(value) {
            dataStore.edit().putInt("UserNamesServerJsonNumCount",value).commit()
            field = value
        }
    var UserNamesServerJsonCreateTime:Int = 0
        get() = dataStore.getInt("UserNamesServerJsonCreateTime",0)
        set(value) {
            dataStore.edit().putInt("UserNamesServerJsonCreateTime",value).commit()
            field = value
        }
    var ScoresJson:String = ""
        get() = dataStore.getString("ScoresJson","")!!
        set(value) {
            dataStore.edit().putString("ScoresJson",value).commit()
            field = value
        }
    var CommentsJson:String = ""
        get() = dataStore.getString("CommentsJson","")!!
        set(value) {
            dataStore.edit().putString("CommentsJson",value).commit()
            field = value
        }

    var RepotedMusicID:MutableList<String> = mutableListOf()
        get() = dataStore.getString("RepotedMusicID","")!!.split(",").toMutableList()
        set(value) {
            println("report set")
            dataStore.edit().putString("RepotedMusicID",value.joinToString(",")).commit()
            field = value
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

class SelectConditions(_tags:String, _sortItem:String) {

    val SortItem = arrayOf(
        "曲の投稿が新しい順",
        "曲の投稿が古い順",
        "ゲームの投稿が新しい曲順",
        "ゲームの投稿が古い曲順",
        "ゲームプレイ回数が多い曲順",
        "ゲームプレイ回数が少ない曲順",
        "お気に入り数が多い曲順",
        "お気に入り数が少ない曲順",
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
            if(!inited){return}
            USERDATA.SelectedMusicCondition = this
        }
    var tag:ArrayList<tagp> = arrayListOf()
    var inited = false
    init {
        tags = _tags
        sortItem = _sortItem
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
}