package com.mizushiki.nicoflick_a

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_ranking_comment.*
import kotlinx.android.synthetic.main.activity_result.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class Activity_Result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val mHandler = Handler()

        //val _musicID = intent.getIntExtra("SelectMusicID", -1)
        //val _levelID = intent.getIntExtra("SelectLevelID", -1)
        val selectMusic = GLOBAL.SelectMUSIC!!//MusicDataLists.musics.filter { it.sqlID == _musicID }.first()
        val selectLevel = GLOBAL.SelectLEVEL!!//MusicDataLists.levels[selectMusic.movieURL]!!.filter { it.sqlID == _levelID }.first()
        val noteData = GLOBAL.CurrentNOTES!!
        var updatedScore = false

        // FrameLayout のインスタンスを取得
        val slashShadeLayout: FrameLayout = findViewById(R.id.ResultView_SlashShade_layout)
        val slashShadeView = SlashShadeView(this,slashColor= Color.argb(255,199,227,255) )
        slashShadeLayout.addView(slashShadeView)
        PicassoLoadImage_NicoThumb(ResultView_imageAlpha, selectMusic.thumbnailURL)

        textView_Title.setText(selectMusic.title)
        textView_Artist.setText(selectMusic.artist)
        textView_Duration.setText(selectMusic.movieLength)
        var starStr = selectLevel.getLevelAsString()
        if( starStr == "FULL" ){
            starStr = "FULL MODE"
        }
        textView_Star.setText(starStr)
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

        val rankStr = noteData.getJudgeRankStr()
        if( rankStr == "PERFECT" ){
            val htmlText = "<font color=\"#FF0000\"><small><small>PERFECT</small></small></font>"
            textView_Rank.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT))
        }else {
            textView_Rank.setText(rankStr)
        }

        //thumbMovieの予約読み込み
        GLOBAL.Selector_instance?.ThumbMoviePlay(Activity_Selector.ForceInResultType.loadOnly)

        textView_TotalNotes.StartNumberRoll(800,ChainTextView.chainOption.callStartFinish){
            if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
            if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
            textView_Combo.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                textView_GreatNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                    if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                    if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                    textView_GoodNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                        if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                        if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                        textView_SafeNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                            if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                            if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                            textView_BadNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                textView_MissNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                    if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                    if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                    textView_StageScore.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                        if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                        if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                        textView_ComboBonus.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                            if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                            if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                            textView_TotalScore.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                                if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                                if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                                //0.8秒遅延させて ランク、ボタン類 を表示
                                                val runb0 = object  : Runnable {
                                                    override fun run() {
                                                        SESystemAudio.rankSePlay()
                                                        if( !selectLevel.isEditing && updatedScore ){
                                                            textView_HiScore.isInvisible = false
                                                        }
                                                        button_ResultOK.isInvisible = false
                                                        button_RankingComment.isInvisible = false
                                                        textView_ResultRankingComment_arrow.isInvisible = false
                                                        textView_Rank.isInvisible = false

                                                        //ThumbMovie再生
                                                        GLOBAL.Selector_instance?.ThumbMoviePlay(Activity_Selector.ForceInResultType.play)
                                                    }
                                                }
                                                mHandler.postDelayed(runb0,800)
                                                //
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        if(selectLevel.isEditing){
            textView_HiScore.setText("編集中データのため保存・送信は行われません。")
            textView_HiScore.isInvisible = false
            return
        }
        if(noteData.score.totalScore <= 0){
            return
        }

        /*
        for( note in noteData.notes ){
            println("${note.word}\t${note.time}\t${note.flickedTime}\t${note.judge}")
        }
         */

        //ユーザースコア
        updatedScore = USERDATA.Score.setScore(selectLevel.sqlID,noteData.score.totalScore,noteData.getJudgeRank())
        USERDATA.JustPlayedScore.setScore(selectLevel.sqlID,noteData.score.totalScore,noteData.getJudgeRank())
        //プレイ回数データ
        USERDATA.PlayCount.addPlayCount(selectLevel.sqlID)


        //スコアをデータベースに送信する(送信済みでないもの)
        var scoreset = USERDATA.Score.getSendScoresStr() //送信するデータ
        scoreset = USERDATA.JustPlayedScore.appendSendScoresStr(scoreset)
        //
        //return
        if( scoreset != "" ){
            //Score 送信
            ServerDataHandler().postScoreData(scoreset= scoreset, userID= USERDATA.UserID) {
                if( it ){
                    //スコアデータを保存する(FLGを降ろして)
                    USERDATA.Score.setSendedFLG()
                    USERDATA.JustPlayedScore.clearSendedData()
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
        SESystemAudio.cansel2SePlay()
        if( !USERDATA.thumbMoviePlay ){
            //戻り直後のThumbMovieちょい見えが気になるので
            GLOBAL.Selector_instance?.PlayerView_Alpha_Huta()
        }
        finish()
        println("finish")
    }

    fun Button_RankingComment(view: View) {
        SESystemAudio.openSePlay()
        val intent: Intent = Intent(applicationContext, Activity_RankingComment::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if( button_ResultOK.isVisible ) {
            if( !USERDATA.thumbMoviePlay ){
                //戻り直後のThumbMovieちょい見えが気になるので
                GLOBAL.Selector_instance?.PlayerView_Alpha_Huta()
            }
            super.onBackPressed()
        }else {
            TextViews_ChainFinish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        println("tap")
        TextViews_ChainFinish()
        return super.onTouchEvent(event)
    }
    fun TextViews_ChainFinish(){
        textView_TotalNotes.chainFinish()
        textView_Combo.chainFinish()
        textView_GreatNum.chainFinish()
        textView_GoodNum.chainFinish()
        textView_SafeNum.chainFinish()
        textView_BadNum.chainFinish()
        textView_MissNum.chainFinish()
        textView_StageScore.chainFinish()
        textView_ComboBonus.chainFinish()
        textView_TotalScore.chainFinish()
    }

    override fun onDestroy() {
        //一応チェインFinishせずに戻ったときの考慮
        textView_TotalNotes.chainBreak()
        textView_Combo.chainBreak()
        textView_GreatNum.chainBreak()
        textView_GoodNum.chainBreak()
        textView_SafeNum.chainBreak()
        textView_BadNum.chainBreak()
        textView_MissNum.chainBreak()
        textView_StageScore.chainBreak()
        textView_ComboBonus.chainBreak()
        textView_TotalScore.chainBreak()
        SESystemAudio.drumRollSeStop()
        super.onDestroy()
    }

}
