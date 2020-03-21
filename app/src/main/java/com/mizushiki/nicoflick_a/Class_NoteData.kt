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
        println(noteString_)
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
            if( note.isFlickable ){
                note.setFlickableFont()
            }else {
                note.setUnFlickableFont()
            }
            note.label.x = -200.0f
            note.label.isInvisible = true
        }
        score = Score()
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
        print(String(format: "cnt=%d, flickableNoteNum=%d", count,flickableNoteNum))
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

    fun judgeWord(flickWord:String) : Int {
        var flickWord = flickWord
        var noteWord = word
        noteWord = noteWord.toHiragana()
        //1段回目
        when( noteWord ){
            "ぁ" -> noteWord = "あ"
            "ぃ" -> noteWord = "い"
            "ぅ" -> noteWord = "う"
            "ぇ" -> noteWord = "え"
            "ぉ" -> noteWord = "お"
            "が" -> noteWord = "か"
            "ぎ" -> noteWord = "き"
            "ぐ" -> noteWord = "く"
            "げ" -> noteWord = "け"
            "ご" -> noteWord = "こ"
            "ざ" -> noteWord = "さ"
            "じ" -> noteWord = "し"
            "ず" -> noteWord = "す"
            "ぜ" -> noteWord = "せ"
            "ぞ" -> noteWord = "そ"
            "だ" -> noteWord = "た"
            "ぢ" -> noteWord = "ち"
            "づ" -> noteWord = "つ"
            "で" -> noteWord = "て"
            "ど" -> noteWord = "と"
            "ば" -> noteWord = "は"
            "び" -> noteWord = "ひ"
            "ぶ" -> noteWord = "ふ"
            "べ" -> noteWord = "へ"
            "ぼ" -> noteWord = "ほ"
            "ぱ" -> noteWord = "は"
            "ぴ" -> noteWord = "ひ"
            "ぷ" -> noteWord = "ふ"
            "ぺ" -> noteWord = "へ"
            "ぽ" -> noteWord = "ほ"
            "ゔ" -> noteWord = "う"
        }
        if( noteWord == flickWord ){
            return Note.NORMAL
        }

        //2段回目
        when( noteWord ){
            "い" -> noteWord = "あ"
            "う" -> noteWord = "あ"
            "え" -> noteWord = "あ"
            "お" -> noteWord = "あ"
            "き" -> noteWord = "か"
            "く" -> noteWord = "か"
            "け" -> noteWord = "か"
            "こ" -> noteWord = "か"
            "し" -> noteWord = "さ"
            "す" -> noteWord = "さ"
            "せ" -> noteWord = "さ"
            "そ" -> noteWord = "さ"
            "ち" -> noteWord = "た"
            "つ" -> noteWord = "た"
            "て" -> noteWord = "た"
            "と" -> noteWord = "た"
            "に" -> noteWord = "な"
            "ぬ" -> noteWord = "な"
            "ね" -> noteWord = "な"
            "の" -> noteWord = "な"
            "ひ" -> noteWord = "は"
            "ふ" -> noteWord = "は"
            "へ" -> noteWord = "は"
            "ほ" -> noteWord = "は"
            "み" -> noteWord = "ま"
            "む" -> noteWord = "ま"
            "め" -> noteWord = "ま"
            "も" -> noteWord = "ま"
            "ゆ" -> noteWord = "や"
            "よ" -> noteWord = "や"
            "り" -> noteWord = "ら"
            "る" -> noteWord = "ら"
            "れ" -> noteWord = "ら"
            "ろ" -> noteWord = "ら"
            "ん" -> noteWord = "わ"
        }
        when( flickWord ){
            "い" -> flickWord = "あ"
            "う" -> flickWord = "あ"
            "え" -> flickWord = "あ"
            "お" -> flickWord = "あ"
            "き" -> flickWord = "か"
            "く" -> flickWord = "か"
            "け" -> flickWord = "か"
            "こ" -> flickWord = "か"
            "し" -> flickWord = "さ"
            "す" -> flickWord = "さ"
            "せ" -> flickWord = "さ"
            "そ" -> flickWord = "さ"
            "ち" -> flickWord = "た"
            "つ" -> flickWord = "た"
            "て" -> flickWord = "た"
            "と" -> flickWord = "た"
            "に" -> flickWord = "な"
            "ぬ" -> flickWord = "な"
            "ね" -> flickWord = "な"
            "の" -> flickWord = "な"
            "ひ" -> flickWord = "は"
            "ふ" -> flickWord = "は"
            "へ" -> flickWord = "は"
            "ほ" -> flickWord = "は"
            "み" -> flickWord = "ま"
            "む" -> flickWord = "ま"
            "め" -> flickWord = "ま"
            "も" -> flickWord = "ま"
            "ゆ" -> flickWord = "や"
            "よ" -> flickWord = "や"
            "り" -> flickWord = "ら"
            "る" -> flickWord = "ら"
            "れ" -> flickWord = "ら"
            "ろ" -> flickWord = "ら"
            "ん" -> flickWord = "わ"
        }
        if( noteWord == flickWord ){
            return Note.SAFE
        }

        return Note.BAD
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

        val RankStr = listOf("Perfect","S","A","B","C","D","E","False")
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
        totalScore = stageScore + comboScore
        if( comboMax < comboCounter ){
            comboMax = comboCounter
        }
    }

}