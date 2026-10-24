package com.mizushiki.nicoflick_a

import android.widget.TextView
import androidx.core.view.isInvisible

class Notes {

    var gameviewWidth:Double = 0.0
    var flickPointX:Double = 0.0
    var notes:ArrayList<Note> = arrayListOf()
    var score = Score()
    var level = 100
    var flickableNoteNum = 0
    var lastFlickedNum = 0 //最後にフリックした文字より前の文字をフリックされないようにする

    fun noteAnalyze(noteString:String, speed:Int, level_:Int ){
        var noteString = noteString
        level = level_
        if( level == 100 ){
            noteString = Regex("(\\[\\d\\d\\:\\d\\d)\\:(\\d\\d\\])").replace(noteString,"$1.$2")
        }
        //どうなっているかわからないので改行データを統一し、文字列化する。
        noteString = Regex("\n").replace(noteString,"\r")
        noteString = Regex("\r\n").replace(noteString,"\r")
        noteString = Regex("\r").replace(noteString,"改")
        //@タグは全消去(NicoFlick=2(atTagExpMode.PreNicoFlickDB)のとき Offsetだけ反映)
        var offset = 0
        if( noteString.pregMatche(pattern= "@NicoFlick\\s*=\\s*2(改|$)") ){
            val offset_str = noteString.pregMatche_firstString(pattern= "@Offset\\s*=\\s*(.*?)(改|$)")
            if( offset_str != "" ){
                offset = offset_str.toInt()
            }
        }
        noteString = noteString.pregReplace(pattern= "@.*?(改|$)", with= "")
        //先頭にタイムタグが付いてなかったら付いてるところまで削る
        noteString = noteString.pregReplace(pattern= "^.*?(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])", with= "$1")
        //処理簡易化のためにラストにダミータイムタグを置く
        noteString = noteString+"[99:99:99]"
        //簡易化のためタイムタグが付いていない文字にもタイムタグを補完する（変数移し替えしながら）
        var noteString_ = ""
        var ans:ArrayList<String> = arrayListOf()
        while( noteString.pregMatche(pattern= "^(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])(.*?)(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])", matche= ans)){
            //print(String.init(format: "ans[]=%@", ans))
            //取得した分を削除する
            noteString = noteString.pregReplace(pattern= "^(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])(.*?)(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])", with= "$3")
            //もしタグ間で取得した文字が２文字以上なら補完
            if( ans[2].count()>1 ){
                var ans2 = ans[1].pregMatches(pattern= "\\[(\\d\\d)\\:(\\d\\d)[\\:|\\.](\\d\\d)\\]")
                val timeA = Double(ans2[1])*60 + Double(ans2[2]) + Double(ans2[3])/100
                ans2 = ans[3].pregMatches(pattern= "\\[(\\d\\d)\\:(\\d\\d)[\\:|\\.](\\d\\d)\\]")
                val timeB = Double(ans2[1])*60 + Double(ans2[2]) + Double(ans2[3])/100
                var txtAns = ""
                ans[2].forEachIndexed{ index, char ->
                    if( index>0 ){
                        val timeC = timeA+(timeB-timeA)/Double(ans[2].count())*Double(index)
                        txtAns += "[%02d:%02d:%02d]".format( Int(timeC/60), Int(timeC)%60, Int((timeC-Double(Int(timeC)))*100) )
                    }
                    txtAns += String(char)
                }
                //間のタイムタグを補完したテキストで上書き
                ans[2] = txtAns
                //ただし、最後だけは複数文字補完したら困るから１文字だけにして他は消す
                if( ans[3] == "[99:99:99]" ){
                    ans[2] = ans[2].substring(0,1)
                }
            }
            noteString_ += ans[1]+ans[2]
            if( ans[2]=="" ){ noteString_ += " " }
            //pregMatch変数を初期化
            ans = arrayListOf()
        }
        //print("noteString_")
        // println(noteString_)
        //完全に [00:00:00]あ ペアにすることが出来た。
        //次は 一文字ごとの詳細データに展開する
        ans = arrayListOf()
        if( noteString_.pregMatches(pattern= "(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])(.)", matches= ans)){
            //print("ans")
            //print(ans)
            //print(ans.count)
            for( i in 0 until ans.size/3 ){

                val timetag = ans[i*3+1]
                val oneWord = ans[i*3+2]
                //println("timetag="+timetag+", oneWord="+oneWord)
                //println("oneWord.isHiragana="+oneWord.isHiragana+", oneWord.isKatakana="+oneWord.isKatakana)
                //もしoneWordに問題なければ格納していく
                if( oneWord != "改" && (oneWord.isHiragana || oneWord.isKatakana) ){
                    val note = Note(oneWord) //文字
                    //時間
                    var ans2 = timetag.pregMatches("\\[(\\d\\d)\\:(\\d\\d)[\\:|\\.](\\d\\d)\\]")
                    note.time = Double(ans2[1])*60 + Double(ans2[2]) + Double(ans2[3])/100 + Double(offset)/1000
                    //フリックNoteかどうか([00:00.00]かどうか)
                    note.isFlickable = timetag.contains(".")
                    if( oneWord == "ー" ){
                        note.isFlickable = false
                    }
                    //表示位置を計算しておく
                    //300/bpm //見えてからフリックポイントに行くまでの秒数
                    val xps:Double = (gameviewWidth-flickPointX)*Double(speed)/300 //一秒で進む距離
                    note.posX = xps * note.time

                    //ゲーム表示/管理用
                    //note.flickedTime = -1.0

                    //フリックNoteの数
                    if( note.isFlickable ){ flickableNoteNum+=1 }
                    //note保持
                    notes.add(note)
                }
            }
        }
        //print(notes)
        score.flickableNoteNum = flickableNoteNum //スコアにも渡しておく
    }
    fun noteReset(){
        for( note in notes ){
            note.flicked = false
            note.flickedTime = -1.0
            note.judge = Note.NORMAL
            note.comboCount = 0
            if( note.isFlickable ){
                note.setFlickableFont()
            }else {
                note.setUnFlickableFont()
            }
            note.label.x = -200.0f
            note.label.isInvisible = true
        }
        val usedNonStandardConditions = score.usedNonStandardConditions
        score = Score()
        if (usedNonStandardConditions) score.markNonStandardConditions()
        score.flickableNoteNum = flickableNoteNum //スコアにも渡しておく
        lastFlickedNum = 0
    }
    fun getTotalNoteNum() : Int{
        /*
        var count=0
        for note in notes {
            if note.isFlickable {
                count+=1
            }
        }
        // print(String(format: "cnt=%d, flickableNoteNum=%d", count,flickableNoteNum))
         */
        return flickableNoteNum
    }
    fun getJudgeNum(judge:Int) : Int{
        var count=0
        for( note in notes ){
            if( note.judge == judge ){
                count+=1
            }
        }
        return count

    }
    fun getJudgeRank() : Int{
        if( score.stageRank <= Score.RankFalse ){
            //既に計算してあったらそれを返す
            return score.stageRank
        }
        if( level<=10 && score.borderScore <= 0 ){
            score.stageRank = Score.RankFalse
            return score.stageRank
        }
        val maxJudgeNoteNum = Double(flickableNoteNum)//0.0
        var greatJudgeNoteNum = 0.0
        var goodJudgeNoteNum = 0.0
        for( note in notes ){
            if( note.judge == Note.NORMAL ){
                continue
            }
            //maxJudgeNoteNum += 1
            if( note.judge == Note.GREAT ){
                greatJudgeNoteNum += 1
            }else if( note.judge == Note.GOOD ){
                goodJudgeNoteNum += 1
            }
        }
        if( maxJudgeNoteNum == greatJudgeNoteNum ){
            score.stageRank = Score.RankPERFECT
            return score.stageRank
        }
        val rate = (greatJudgeNoteNum+goodJudgeNoteNum)/maxJudgeNoteNum*100
        if( rate>=100 ){
            score.stageRank = Score.RankS
        }else if( rate>95 ){
            score.stageRank = Score.RankA
        }else if( rate>80 ){
            score.stageRank = Score.RankB
        }else if( rate>65 ){
            score.stageRank = Score.RankC
        }else if( rate>50 ){
            score.stageRank = Score.RankD
        }else {
            score.stageRank = Score.RankE
        }
        return score.stageRank

    }

    fun getJudgeRankStr() : String{
        return Score.RankStr[getJudgeRank()]
    }

    //master
    fun masterAllNotFlickable() {
        for( note in notes ){
            note.isFlickable = false
        }
    }
}

class Note(var word:String =""){

    companion object {
        const val NORMAL = 0
        const val GREAT = 1
        const val GOOD = 2
        const val SAFE = 3
        const val BAD = 4
        const val MISS = 5
    }

    var time:Double = 0.0
    var isFlickable = false
    var posX:Double = -100.0

    var flicked = false
    var flickedTime:Double = -1.0
    var judge = Note.NORMAL
    var comboCount = 0

    lateinit var label:TextView
    var flickedLabel:TextView? = null
    var flickableLabel:TextView? = null


    fun setFlickableFont(){
        if( label === flickableLabel ){ return }
        flickableLabel?.let {
            label = it
            label.x = flickedLabel!!.x
            flickedLabel!!.isInvisible = true
            label.isInvisible = false
        }
    }
    fun setUnFlickableFont(){
        if( label === flickedLabel ){ return }
        flickableLabel?.let {
            label = flickedLabel!!
            label.x = flickableLabel!!.x
            flickableLabel!!.isInvisible = true
            label.isInvisible = false
        }
    }

    fun judgeWord(flickWord:String):Int {
        val normalized = judgeWorder(word)
        if (normalized == flickWord) return NORMAL
        if (judgeWorder(normalized, 2) == judgeWorder(flickWord, 2)) return SAFE
        return BAD
    }
    fun judgeWorder(noteWord:String, level:Int = 1):String {
        val normalized = if (level == 1) noteWord.toHiragana() else noteWord
        return if (level == 1) when (normalized) {
            "ぁ" -> "あ"
            "ぃ" -> "い"
            "ぅ" -> "う"
            "ぇ" -> "え"
            "ぉ" -> "お"
            "が" -> "か"
            "ぎ" -> "き"
            "ぐ" -> "く"
            "げ" -> "け"
            "ご" -> "こ"
            "ざ" -> "さ"
            "じ" -> "し"
            "ず" -> "す"
            "ぜ" -> "せ"
            "ぞ" -> "そ"
            "だ" -> "た"
            "ぢ" -> "ち"
            "づ" -> "つ"
            "で" -> "て"
            "ど" -> "と"
            "ば" -> "は"
            "び" -> "ひ"
            "ぶ" -> "ふ"
            "べ" -> "へ"
            "ぼ" -> "ほ"
            "ぱ" -> "は"
            "ぴ" -> "ひ"
            "ぷ" -> "ふ"
            "ぺ" -> "へ"
            "ぽ" -> "ほ"
            "ゔ" -> "う"
            else -> normalized
        } else when (normalized) {
            "い" -> "あ"
            "う" -> "あ"
            "え" -> "あ"
            "お" -> "あ"
            "き" -> "か"
            "く" -> "か"
            "け" -> "か"
            "こ" -> "か"
            "し" -> "さ"
            "す" -> "さ"
            "せ" -> "さ"
            "そ" -> "さ"
            "ち" -> "た"
            "つ" -> "た"
            "て" -> "た"
            "と" -> "た"
            "に" -> "な"
            "ぬ" -> "な"
            "ね" -> "な"
            "の" -> "な"
            "ひ" -> "は"
            "ふ" -> "は"
            "へ" -> "は"
            "ほ" -> "は"
            "み" -> "ま"
            "む" -> "ま"
            "め" -> "ま"
            "も" -> "ま"
            "ゆ" -> "や"
            "よ" -> "や"
            "り" -> "ら"
            "る" -> "ら"
            "れ" -> "ら"
            "ろ" -> "ら"
            "ん" -> "わ"
            else -> normalized
        }
    }

}

class Score {

    companion object {
        const val GREAT = 300
        const val GOOD = 150
        const val SAFE = 50
        const val BAD = 30
        const val MISS = 0

        const val RankPERFECT = 0
        const val RankS = 1
        const val RankA = 2
        const val RankB = 3
        const val RankC = 4
        const val RankD = 5
        const val RankE = 6
        const val RankFalse = 7

        val RankStr = listOf("PERFECT","S","A","B","C","D","E","False")
    }

    // This belongs to the score and can only move from eligible to ineligible.
    var usedNonStandardConditions = false
        private set

    fun markNonStandardConditions() {
        usedNonStandardConditions = true
    }

    fun recordPlayConditions(practice: Boolean, speed: Float, pitch: Float,
                             seeking: Boolean = false, autoPlay: Boolean = false,
                             editing: Boolean = false) {
        if (practice || speed != 1.0f || pitch != 1.0f || seeking || autoPlay || editing) {
            markNonStandardConditions()
        }
    }

    var stageRank = 100

    var stageScore = 0
    var comboScore = 0
    var totalScore = 0

    var comboCounter = 0
    var comboMax = 0

    var flickableNoteNumD = 0.0
    var flickableNoteNum = 0
        set(value) {
            flickableNoteNumD = value.toDouble()
        }
    var borderScore = 50.0

    fun subScore(maeJudge:Int, maeComboCount:Int) {
        stageScore -= when (maeJudge) { Note.GREAT -> GREAT; Note.GOOD -> GOOD; Note.SAFE -> SAFE; Note.BAD -> BAD; else -> 0 }
        comboScore -= when {
            maeComboCount >= 95 -> 500; maeComboCount >= 85 -> 450; maeComboCount >= 75 -> 400
            maeComboCount >= 65 -> 350; maeComboCount >= 55 -> 300; maeComboCount >= 45 -> 250
            maeComboCount >= 35 -> 200; maeComboCount >= 25 -> 150; maeComboCount >= 15 -> 100
            maeComboCount >= 5 -> 50; else -> 0
        }
        if (maeComboCount >= 100) stageScore -= 200
        totalScore = stageScore + comboScore
    }

    fun addScore(judge:Int) {
        when( judge ){
            Note.GREAT -> {
                stageScore += Score.GREAT
                comboCounter += 1
                borderScore += 100.0/(2.0*(flickableNoteNumD))
            }
            Note.GOOD -> {
                stageScore += Score.GOOD
                comboCounter += 1
                borderScore += 100.0/(2.0*(flickableNoteNumD))
            }
            Note.SAFE -> {
                stageScore += Score.SAFE
                comboCounter = 0
            }
            Note.BAD -> {
                stageScore += Score.BAD
                comboCounter = 0
                borderScore -= 100.0/(0.8*(flickableNoteNumD))*2
            }
            Note.MISS -> {
                stageScore += Score.MISS
                comboCounter = 0
                borderScore -= 100.0/(0.4*(flickableNoteNumD))*2
            }
        }
        if( 95 <= comboCounter ){
            comboScore += 500
        }else if( 85 <= comboCounter){
            comboScore += 450
        }else if( 75 <= comboCounter){
            comboScore += 400
        }else if( 65 <= comboCounter){
            comboScore += 350
        }else if( 55 <= comboCounter){
            comboScore += 300
        }else if( 45 <= comboCounter){
            comboScore += 250
        }else if( 35 <= comboCounter){
            comboScore += 200
        }else if( 25 <= comboCounter){
            comboScore += 150
        }else if( 15 <= comboCounter){
            comboScore += 100
        }else if(  5 <= comboCounter){
            comboScore += 50
        }
        if( comboCounter >= 100 ){
            stageScore += 200
        }
        borderScore = borderScore.coerceIn(0.0, 100.0)
        totalScore = stageScore + comboScore
        if( comboMax < comboCounter ){
            comboMax = comboCounter
        }
    }

}