package com.mizushiki.nicoflick_a

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import kotlinx.android.synthetic.main.activity_result.*

class Activity_Result : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        //val _musicID = intent.getIntExtra("SelectMusicID", -1)
        //val _levelID = intent.getIntExtra("SelectLevelID", -1)
        val selectMusic = GLOBAL.SelectMUSIC!!//MusicDataLists.musics.filter { it.sqlID == _musicID }.first()
        val selectLevel = GLOBAL.SelectLEVEL!!//MusicDataLists.levels[selectMusic.movieURL]!!.filter { it.sqlID == _levelID }.first()
        val noteData = GLOBAL.CurrentNOTES!!
        textView_Title.setText(selectMusic.title)
        textView_Artist.setText(selectMusic.artist)
        textView_Duration.setText(selectMusic.movieLength)
        textView_Star.setText(selectLevel.getLevelAsString())
        textView_tokosya.setText(selectLevel.creator)
        PicassoLoadImage_NicoThumb(imageView_resultThumbnail, selectMusic.thumbnailURL)

        textView_TotalNotes.setText(noteData.getTotalNoteNum().toString())
        textView_Combo.setText(noteData.score.comboMax.toString())
        textView_GreatNum.setText(noteData.getJudgeNum(Note.GREAT).toString())
        textView_GoodNum.setText(noteData.getJudgeNum(Note.GOOD).toString())
        textView_SafeNum.setText(noteData.getJudgeNum(Note.SAFE).toString())
        textView_BadNum.setText(noteData.getJudgeNum(Note.BAD).toString())
        textView_MissNum.setText(noteData.getJudgeNum(Note.MISS).toString())
        if( selectLevel.level > 10 ){ textView_MissNum.setText("0") }

        textView_StageScore.setText(noteData.score.stageScore.toString())
        textView_ComboBonus.setText(noteData.score.comboScore.toString())
        textView_TotalScore.setText(noteData.score.totalScore.toString())

        textView_Rank.setText(noteData.getJudgeRankStr())

        if(selectLevel.isEditing){
            textView_HiScore.setText("編集中データのため保存・送信は行われません。")
            textView_HiScore.isInvisible = false
            return
        }
        if(noteData.score.totalScore <= 0){
            return
        }

        for( note in noteData.notes ){
            println("${note.word}\t${note.time}\t${note.flickedTime}\t${note.judge}")
        }

        //ユーザースコア
        val updatedScore = USERDATA.Score.setScore(selectLevel.sqlID,noteData.score.totalScore,noteData.getJudgeRank())
        if(updatedScore){
            textView_HiScore.isInvisible = false
        }
        //プレイ回数データ
        USERDATA.PlayCount.addPlayCount(selectLevel.sqlID)


        //スコアをデータベースに送信する(送信済みでないもの)
        val scoreset = USERDATA.Score.getSendScoresStr() //送信するデータ
        if( scoreset != "" ){
            //Score 送信
            ServerDataHandler().postScoreData(scoreset= scoreset, userID= USERDATA.UserID) {
                if( it ){
                    //スコアデータを保存する(FLGを降ろして)
                    USERDATA.Score.setSendedFLG()
                }
            }
        }
        //プレイ回数をデータベースに送信する(送信済みでないもの)
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
        println("りざると")
    }

    fun Button_OK(view: View) {
        finish()
        println("finish")
    }

    fun Button_RankingComment(view: View) {
        val intent: Intent = Intent(applicationContext, Activity_RankingComment::class.java)
        startActivity(intent)
    }
}
