package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivityResultBinding

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

import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class Activity_Result : SafeAreaActivity() {

    private lateinit var binding: ActivityResultBinding
    private val mHandler = Handler()
    private var resultRecorded = false
    private var updatedScore = false
    private var segueing = false
    private val rankingRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectMusic = GLOBAL.SelectMUSIC ?: return returnToStartAfterStateLoss()
        val selectLevel = GLOBAL.SelectLEVEL ?: return returnToStartAfterStateLoss()
        val noteData = GLOBAL.CurrentNOTES ?: return returnToStartAfterStateLoss()
        resultRecorded = savedInstanceState?.getBoolean("resultRecorded") ?: false
        updatedScore = savedInstanceState?.getBoolean("updatedScore") ?: false

        // FrameLayout のインスタンスを取得
        val slashShadeLayout: FrameLayout = findViewById(R.id.ResultView_SlashShade_layout)
        val slashShadeView = SlashShadeView(this,slashColor= Color.argb(255,199,227,255) )
        slashShadeLayout.addView(slashShadeView)
        PicassoLoadImage_NicoThumb(binding.ResultViewImageAlpha, selectMusic.thumbnailURL)

        binding.textViewTitle.setText(selectMusic.title)
        binding.textViewArtist.setText(selectMusic.artist)
        binding.textViewDuration.setText(selectMusic.movieLength)
        var starStr = selectLevel.getLevelAsString()
        if( starStr == "FULL" ){
            starStr = "FULL MODE"
        }
        binding.textViewStar.setText(starStr)
        binding.textViewTokosya.setText(selectLevel.creator)
        PicassoLoadImage_NicoThumb(binding.imageViewResultThumbnail, selectMusic.thumbnailURL)

        binding.textViewTotalNotes.setText(noteData.getTotalNoteNum().toString())
        binding.textViewCombo.setText(noteData.score.comboMax.toString())
        binding.textViewGreatNum.setText(noteData.getJudgeNum(Note.GREAT).toString())
        binding.textViewGoodNum.setText(noteData.getJudgeNum(Note.GOOD).toString())
        binding.textViewSafeNum.setText(noteData.getJudgeNum(Note.SAFE).toString())
        binding.textViewBadNum.setText(noteData.getJudgeNum(Note.BAD).toString())
        binding.textViewMissNum.setText(noteData.getJudgeNum(Note.MISS).toString())
        if( selectLevel.level > 10 ){ binding.textViewMissNum.setText("0") }

        binding.textViewStageScore.setText(noteData.score.stageScore.toString())
        binding.textViewComboBonus.setText(noteData.score.comboScore.toString())
        binding.textViewTotalScore.setText(noteData.score.totalScore.toString())

        val rankStr = noteData.getJudgeRankStr()
        if( rankStr == "PERFECT" ){
            val htmlText = "<font color=\"#FF0000\"><small><small>PERFECT</small></small></font>"
            binding.textViewRank.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT))
        }else {
            binding.textViewRank.setText(rankStr)
        }

        //thumbMovieの予約読み込み
        GLOBAL.Selector_instance?.ThumbMoviePlay(Activity_Selector.ForceInResultType.loadOnly)

        binding.textViewTotalNotes.StartNumberRoll(800,ChainTextView.chainOption.callStartFinish){
            if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
            if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
            binding.textViewCombo.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                binding.textViewGreatNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                    if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                    if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                    binding.textViewGoodNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                        if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                        if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                        binding.textViewSafeNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                            if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                            if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                            binding.textViewBadNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                binding.textViewMissNum.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                    if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                    if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                    binding.textViewStageScore.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                        if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                        if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                        binding.textViewComboBonus.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                            if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                            if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                            binding.textViewTotalScore.StartNumberRoll(400,ChainTextView.chainOption.callStartFinish){
                                                if( it == ChainTextView.chainState.start ){ SESystemAudio.drumRollSeLoop(); return@StartNumberRoll }
                                                if( it == ChainTextView.chainState.finish ){ SESystemAudio.drumRollSeStop() }
                                                //0.8秒遅延させて ランク、ボタン類 を表示
                                                val runb0 = object  : Runnable {
                                                    override fun run() {
                                                        if (isFinishing || isDestroyed) return
                                                        SESystemAudio.rankSePlay()
                                                        if( !selectLevel.isEditing && updatedScore ){
                                                            binding.textViewHiScore.isInvisible = false
                                                        }
                                                        binding.buttonResultOK.isInvisible = false
                                                        binding.buttonRankingComment.isInvisible = false
                                                        binding.textViewResultRankingCommentArrow.isInvisible = false
                                                        binding.textViewRank.isInvisible = false

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


        // 同じ結果画面の再生成では記録・送信を繰り返さない。
        val shouldRecord = !resultRecorded
        resultRecorded = true

        if(selectLevel.isEditing){
            binding.textViewHiScore.setText("編集中データのため保存・送信は行われません。")
            binding.textViewHiScore.isInvisible = false
            return
        }

        if (shouldRecord) {
            SelectorDiagnostics.measure("result.playCount") { USERDATA.PlayCount.addPlayCount(selectLevel.sqlID) }
            if (intent.getBooleanExtra("hasPassedFirstNote", false)) {
                SelectorDiagnostics.measure("result.playHistory") { USERDATA.recordLevelPlay(selectLevel.sqlID) }
            }
        }
        if (GLOBAL.ResultPractice || noteData.score.usedNonStandardConditions) {
            binding.textViewHiScore.text = if (GLOBAL.ResultPractice)
                "練習モードのためスコアの保存・送信は行われません。"
            else "通常と異なるプレイ条件のためスコアの保存・送信は行われません。"
            binding.textViewHiScore.isInvisible = false
            if (shouldRecord) ServerDataHandler().postUserCountData()
            return
        }
        if(noteData.score.totalScore <= 0){
            return
        }

        /*
        for( note in noteData.notes ){
            // println("${note.word}\t${note.time}\t${note.flickedTime}\t${note.judge}")
        }
         */

        if (!shouldRecord) return

        // 今回の保存前のスコア件数で対象を限定する。途中終了・練習・特殊条件は調べない。
        if (intent.getBooleanExtra("completedNormally", false) && noteData.score.borderScore > 0) {
            FlickOnlyReminder.inspectCompletedResult(noteData.notes, USERDATA.Score.scores.size)
        }

        //ユーザースコア
        updatedScore = SelectorDiagnostics.measure("result.saveBestScore") { USERDATA.Score.setScore(selectLevel.sqlID,noteData.score.totalScore,noteData.getJudgeRank(),selectLevel.level) }
        SelectorDiagnostics.measure("result.saveRecentScore") { USERDATA.JustPlayedScore.setScore(selectLevel.sqlID,noteData.score.totalScore,noteData.getJudgeRank(),selectLevel.level) }



        //スコアをデータベースに送信する(送信済みでないもの)
        var scoreset = SelectorDiagnostics.measure("result.prepareBestScores") { USERDATA.Score.getSendScoresStr() }
        scoreset = SelectorDiagnostics.measure("result.prepareRecentScores") { USERDATA.JustPlayedScore.appendSendScoresStr(scoreset) }
        //
        //return
        SelectorDiagnostics.event { "SCORE_REQUEST chars=${scoreset.length} records=${scoreset.count { it == '<' }}" }
        val diagnosticRequestStart = if (SelectorDiagnostics.ENABLED) android.os.SystemClock.elapsedRealtime() else 0L
        if( scoreset != "" ){
            //Score 送信
            ServerDataHandler().postScoreData(scoreset= scoreset, userID= USERDATA.UserID) {
                SelectorDiagnostics.event { "SCORE_RESPONSE success=$it elapsedMs=${android.os.SystemClock.elapsedRealtime() - diagnosticRequestStart}" }
                if( it ){
                    //スコアデータを保存する(FLGを降ろして)
                    SelectorDiagnostics.measure("result.acknowledgeBestScores") { USERDATA.Score.setSendedFLG() }
                    SelectorDiagnostics.measure("result.clearRecentScores") { USERDATA.JustPlayedScore.clearSendedData() }
                }
            }
        }
        ServerDataHandler().postUserCountData()
        // println("りざると")
    }

    fun Button_OK(view: View) {
        if (segueing || isFinishing || isDestroyed) return
        segueing = true
        SESystemAudio.cansel2SePlay()
        if( !USERDATA.thumbMoviePlay ){
            //戻り直後のThumbMovieちょい見えが気になるので
            GLOBAL.Selector_instance?.PlayerView_Alpha_Huta()
        }
        GLOBAL.ResultFirst = false
        finish()
        // println("finish")
    }

    fun Button_RankingComment(view: View) {
        if (segueing || isFinishing || isDestroyed) return
        segueing = true
        SESystemAudio.openSePlay()
        val intent: Intent = Intent(applicationContext, Activity_RankingComment::class.java)
        startActivityForResult(intent, rankingRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rankingRequestCode && !isFinishing && !isDestroyed) {
            segueing = false
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (segueing || isFinishing || isDestroyed) return true
        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        if (segueing || isFinishing || isDestroyed) return
        if( binding.buttonResultOK.isVisible ) {
            Button_OK(binding.buttonResultOK)
        }else {
            TextViews_ChainFinish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // println("tap")
        TextViews_ChainFinish()
        return super.onTouchEvent(event)
    }
    fun TextViews_ChainFinish(){
        binding.textViewTotalNotes.chainFinish()
        binding.textViewCombo.chainFinish()
        binding.textViewGreatNum.chainFinish()
        binding.textViewGoodNum.chainFinish()
        binding.textViewSafeNum.chainFinish()
        binding.textViewBadNum.chainFinish()
        binding.textViewMissNum.chainFinish()
        binding.textViewStageScore.chainFinish()
        binding.textViewComboBonus.chainFinish()
        binding.textViewTotalScore.chainFinish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("resultRecorded", resultRecorded)
        outState.putBoolean("updatedScore", updatedScore)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        //一応チェインFinishせずに戻ったときの考慮
        binding.textViewTotalNotes.chainBreak()
        binding.textViewCombo.chainBreak()
        binding.textViewGreatNum.chainBreak()
        binding.textViewGoodNum.chainBreak()
        binding.textViewSafeNum.chainBreak()
        binding.textViewBadNum.chainBreak()
        binding.textViewMissNum.chainBreak()
        binding.textViewStageScore.chainBreak()
        binding.textViewComboBonus.chainBreak()
        binding.textViewTotalScore.chainBreak()
        SESystemAudio.drumRollSeStop()
        super.onDestroy()
    }

}
