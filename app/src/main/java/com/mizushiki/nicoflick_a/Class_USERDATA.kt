package com.mizushiki.nicoflick_a

import android.content.Context
import android.content.SharedPreferences
import java.util.*
import kotlin.collections.ArrayList


// object Userdata
// class SelectConditions(_tags:String, _sortItem:String)

object USERDATA {

    // 必ず GLOBAL.APPLICATIONCONTEXTが入力されてからシングルトンを作る
    val dataStore: SharedPreferences = GLOBAL.APPLICATIONCONTEXT.getSharedPreferences("DataStore", Context.MODE_PRIVATE)

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
                scoreset += "<$levelID,${us[Companion.SCORE]}>"
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

class SelectConditions(_tags:String, _sortItem:String) {

    val SortItem = arrayOf(
        "曲の投稿が新しい順",
        "曲の投稿が古い順",
        "ゲームの投稿が新しい曲順",
        "ゲームの投稿が古い曲順",
        "ゲームプレイ回数が多い曲順",
        "ゲームプレイ回数が少ない曲順",
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
        for( value in array ){
            var v = value
            when(value) {
                "and" -> type = "and"
                "or" -> type = "of"
                "" -> {}
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

    var sortItemNum: Int
        get() = SortItem.indexOf(sortItem)
        set(value) {
            sortItem = SortItem[value]
            USERDATA.SelectedMusicCondition = this
        }
}