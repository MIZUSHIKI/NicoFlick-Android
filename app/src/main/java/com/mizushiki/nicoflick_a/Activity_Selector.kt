package com.mizushiki.nicoflick_a

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.text.Html
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener
import kotlinx.android.synthetic.main.activity_selector.*
import kotlinx.android.synthetic.main.activity_settings.progress_circular
import java.util.*
import kotlin.concurrent.schedule


class Activity_Selector : AppCompatActivity() {

    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    var scoreDatas: ScoreDataLists = ScoreDataLists
    var commentDatas: CommentDataLists = CommentDataLists

    var userScore = USERDATA.Score

    //
    var currentMusics: ArrayList<musicData> = arrayListOf(musicData())
    var currentLevels: ArrayList<levelData> = arrayListOf()
    var indexCoverFlow = -1
    var indexPicker = 0
    var levelpicker_scrollY = 0
    var levelScroller_index = 0
    var levelScroller_scrollY = 0
    var levelScroller_maeScrollY = -100
    var maeTags = ""
    var maeSort = ""
    var scrollingTimer: Timer? = null

    lateinit var coverflow: FeatureCoverFlow
    var containerFirstPos = 0
    var scrollerOneHeight = 100

    var segueing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector)

        //カバーフロー
        val mHandler = Handler()
        coverflow = findViewById<View>(R.id.coverflow) as FeatureCoverFlow
        val coverFlowAdapter = CoverFlowAdapter(this, currentMusics)
        coverflow.adapter = coverFlowAdapter
        coverflow.setOnScrollPositionListener(object : OnScrollPositionListener {
            override fun onScrolling() {
                //println("scrolling"+coverflow.scrollPosition)
                if (scrollingTimer == null) {
                    val timerCallback1: TimerTask.() -> Unit = {
                        mHandler.post {
                            //println("TODO")
                            if ( coverflow.isVisible && indexCoverFlow != coverflow.scrollPosition) {
                                indexCoverFlow = coverflow.scrollPosition
                                println("index=" + coverflow.scrollPosition)
                                setCurrentLevels(indexCoverFlow)
                            }
                        }
                    }
                    scrollingTimer = Timer()
                    scrollingTimer!!.schedule(0, 100, timerCallback1)
                }
            }

            override fun onScrolledToPosition(position: Int) {
                if( coverflow.isVisible ){
                    //println("pos=$position")
                    indexCoverFlow = position
                    scrollingTimer?.cancel()
                    scrollingTimer = null
                }
            }

        })
        //レベルピッカー(スクロールビューVer)
        scrollerOneHeight = levelScrollerView.layoutParams.height / 2

        scrollview_levelScroller.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            //println("scID = ${ (scrollY.toFloat() / scrollerOneHeight + 0.5).toInt()}, scrollY = $scrollY")
            levelScroller_scrollY = scrollY
            // IndexPicker
            if( indexPicker != (scrollY.toFloat() / scrollerOneHeight + 0.5).toInt() ){
                indexPicker = (scrollY.toFloat() / scrollerOneHeight + 0.5).toInt()
                if(indexPicker > currentLevels.count()-1){
                    indexPicker = currentLevels.count()-1
                }else {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val vibrationEffect = VibrationEffect.createOneShot(30, DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)
                    } else {
                        vibrator.vibrate(30)
                    }
                }
            }
            // 表示用のindex
            if( levelScroller_index != (scrollY / scrollerOneHeight) ){
                levelScroller_index = scrollY / scrollerOneHeight
                if(levelScroller_index > currentLevels.count()-1){
                    levelScroller_index = currentLevels.count()-1
                }
                //LevelPickerコンテナの中身を更新
                LevelPickerContainerRedraw()
            }
            LevelPickerContainerSetScrollY(scrollY % scrollerOneHeight)
        }
        scrollview_levelScroller.setOnTouchListener { v, event ->
            if(event.action == ACTION_UP){
                //指を離したとき据わりの良い位置にスクロールする。ための確認ループ
                val timerRun = object : Runnable {
                    override fun run() {
                        if(levelScroller_maeScrollY == -100){
                            //確認ループ最初はmae数値の保存だけする
                            levelScroller_maeScrollY = levelScroller_scrollY
                            //繰り返し
                            mHandler.postDelayed(this, 20)
                            return
                        }
                        if(levelScroller_maeScrollY != levelScroller_scrollY){
                            levelScroller_maeScrollY = levelScroller_scrollY
                            //前回とスクロール値が異なるならまだスクロール中と判断して繰り返し
                            mHandler.postDelayed(this, 20)
                            return
                        }else {
                            //指離した後、スクロールも止まった。
                            levelScroller_maeScrollY = -100
                            scrollview_levelScroller.smoothScrollTo(0,indexPicker * scrollerOneHeight)
                       }
                    }
                }
                if(levelScroller_maeScrollY == -100){
                    //指離し後、まだスクロール中か確認するループ起動
                    mHandler.post(timerRun)
                }
            }
            return@setOnTouchListener false
        }

        SetMusicToCoverFlow()

        //オブジェクトが生成されてから実行（値を取得する）
        Handler().postDelayed(Runnable {
            //コンテナの初期値を保存
            containerFirstPos = levelpickerContainer.y.toInt()
        }, 100)

        text_Tags.setOnClickListener {
            if( currentMusics.size > 0 ){
                if(segueing){ return@setOnClickListener }
                segueing = true
                maeTags = USERDATA.SelectedMusicCondition.tags
                maeSort = USERDATA.SelectedMusicCondition.sortItem
                GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
                val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTableForTag::class.java)
                intent.putExtra("fromSelector", true)
                startActivityForResult(intent, 1002)
            }
        }
    }

    fun SetMusicToCoverFlow() {
        progress_circular.isVisible = true

        musicDatas.getSelectMusics {
            //for(m in it){
            //    println(m.title)
            //}
            currentMusics = it
            //println("tags="+USERDATA.SelectedMusicCondition.tags)
            //println("currentMusics.size="+currentMusics.size)

            coverflow = findViewById<View>(R.id.coverflow) as FeatureCoverFlow
            if(indexCoverFlow == -1){
                if( currentMusics.size > 0 ){
                    coverflow.clearCache()
                    val coverFlowAdapter = CoverFlowAdapter(this, it)
                    coverflow.adapter = coverFlowAdapter
                    coverflow.setShouldRepeat(false)
                    coverflow.scrollToPosition(0)
                    indexCoverFlow = 0
                }else{
                    coverflow.isVisible = false
                }
            }
            progress_circular.isVisible = false

            setCurrentLevels(indexCoverFlow)
        }

    }

    fun setCurrentLevels(index: Int) {
        if (index == -1) {
            //曲が一つもないとき
            text_Title_c.setText("")
            text_Artist.setText("")
            text_Length.setText("")
            text_Tags.setText("")
            text_Speed.setText("")
            text_Num.setText("")
            currentLevels = arrayListOf()
            indexPicker = -1
            LevelPickerContainerRedraw()
            return
        }
        text_Title_c.setText(currentMusics[index].title)
        text_Artist.setText(currentMusics[index].artist)
        text_Length.setText(currentMusics[index].movieLength)
        text_Tags.setText(currentMusics[index].tags)
        var htmlText = currentMusics[index].tags
        for( tagp in USERDATA.SelectedMusicCondition.tag ){
            val tag = tagp.word
            //println(tag)
            htmlText = htmlText.pregReplace("(^|\\s)($tag)(\\s|$)","$1<font color='#000000'>$2</font>$3")

        }
        text_Tags.setText( Html.fromHtml( htmlText, Html.FROM_HTML_MODE_COMPACT ) )

        text_Num.setText("${index + 1} / ${currentMusics.size}")


        if(musicDatas.levels[currentMusics[index].movieURL]==null){
            currentLevels = arrayListOf()
        }else {
            currentLevels = musicDatas.getSelectMusicLevels(currentMusics[index].movieURL)
        }

        if(indexPicker >= currentLevels.size - 1){
            indexPicker = currentLevels.size - 1
        }
        // LinearLayout(levelScrollerContainer)内の Viewを新しく作り直してAddする（height変更がうまくできなかった）
        levelScrollerContainer.removeAllViews()
        levelScrollerContainer.addView(View(applicationContext), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,scrollerOneHeight * (currentLevels.size + 1 )))

        LevelPickerContainerRedraw()
    }

    fun ShowHowToExtendView():Boolean{

        if(USERDATA.lookedExtend == false){
            var musicIDSet:MutableSet<Int> = mutableSetOf()
            for( (sKey,_) in USERDATA.Score.scores){
                for( ms in musicDatas.musics ){
                    if( ms.levelIDs.contains(sKey) ){
                        musicIDSet.add(ms.sqlID)
                    }
                }
            }
            println("$musicIDSet ; ${musicIDSet.size}")
            if( musicIDSet.size >= 5 ){
                USERDATA.lookedExtend = true
                segueing = true
                Handler().postDelayed(Runnable {
                    segueing = false
                    val intent: Intent = Intent(applicationContext, Activity_HowToExtend::class.java)
                    startActivity(intent)
                }, 750)
                return true
            }
            if( USERDATA.lookedOtherIme ){ return false }
            USERDATA.lookedOtherIme = true
            segueing = true
            Handler().postDelayed(Runnable {
                segueing = false
                val intent: Intent = Intent(applicationContext, Activity_HowToGame2::class.java)
                startActivity(intent)
            }, 750)
            return true
        }
        return false
    }

    fun Button_Go(view: View) {
        if( currentMusics.size < 1 || currentLevels.size < 1 ){ return }

        if(segueing) {
            return
        }
        segueing = true

        //すでにゲームデータを持っている場合
        if(currentLevels[indexPicker].noteData != ""){
            val intent: Intent = Intent(applicationContext, Activity_GameView::class.java)
            GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
            GLOBAL.SelectLEVEL = currentLevels[indexPicker]
            startActivityForResult(intent, 1001)
            segueing = false
            return
        }
        //まだゲームデータがない場合ダウンロードしてから遷移する
        progress_circular.isVisible = true
        ServerDataHandler().DownloadTimetag(currentLevels[indexPicker]) {
            //if(it!=null) {
            //return@DownloadMusicDataAndUserNameData
            //}
            progress_circular.isVisible = false

            val intent: Intent = Intent(applicationContext, Activity_GameView::class.java)
            GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
            GLOBAL.SelectLEVEL = currentLevels[indexPicker]
            startActivityForResult(intent, 1001)
            segueing = false
        }
    }
    fun Button_Menu(view: View) {
        if(segueing) {
            return
        }
        segueing = true
        val intent: Intent = Intent(applicationContext, Activity_SelectorMenu::class.java)
        if( currentMusics.size > 0 ) {
            GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
        }else {
            GLOBAL.SelectMUSIC = null
        }

        if( currentLevels.size > 0 ){
            GLOBAL.SelectLEVEL = currentLevels[indexPicker]
        }else {
            GLOBAL.SelectLEVEL = null
        }
        maeTags = USERDATA.SelectedMusicCondition.tags
        maeSort = USERDATA.SelectedMusicCondition.sortItem
        startActivityForResult(intent, 1002)
    }
    fun Button_Back(view: View) {
        if(segueing) {
            return
        }
        finish()
    }
    fun Button_RankingComment(view: View) {
        if(indexPicker==-1){ return }
        if(segueing) {
            return
        }
        segueing = true
        val intent: Intent = Intent(applicationContext, Activity_RankingComment::class.java)
        GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
        println("selector title ="+ GLOBAL.SelectMUSIC!!.title)
        GLOBAL.SelectLEVEL = currentLevels[indexPicker]
        startActivityForResult(intent, 1003)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        segueing = false
        // startActivityForResult()の際に指定した識別コードとの比較
        when( requestCode ){
            1001 -> {
                userScore = USERDATA.Score
                setCurrentLevels(indexCoverFlow)
                if(!ShowHowToExtendView()){
                    //ユーザーネームデータをロードするため遷移させないようにする
                    segueing = true
                    progress_circular.isVisible = true
                    ServerDataHandler().Chance_DownloadUserNameData_FirstData {
                        segueing = false
                        progress_circular.isVisible = false
                    }
                }
            }
            1002 -> {
                if( maeTags != USERDATA.SelectedMusicCondition.tags || maeSort != USERDATA.SelectedMusicCondition.sortItem){
                    indexCoverFlow = -1
                    SetMusicToCoverFlow()
                }
            }
            1003 -> {
                //ユーザーネームデータをロードするため遷移させないようにする
                segueing = true
                progress_circular.isVisible = true
                ServerDataHandler().Chance_DownloadUserNameData_FirstData {
                    segueing = false
                    progress_circular.isVisible = false
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LevelPicker処理 //
    fun LevelPickerContainerRedraw() {
        if( (levelScroller_index-1) >= 0 &&  (levelScroller_index-1) < currentLevels.count()) {
            setLevelPickerContainer( currentLevels[levelScroller_index-1], star_m1, creator_m1, score_m1, rank_m1, date_m1 )
        }else {
            setLevelPickerContainer( null, star_m1, creator_m1, score_m1, rank_m1, date_m1 )
        }
        if( levelScroller_index != -1 &&  (levelScroller_index) < currentLevels.count() ){
            setLevelPickerContainer( currentLevels[levelScroller_index], star, creator, score, rank, date )
            text_Speed.setText( "speed: "+currentLevels[levelScroller_index].speed )
        }else {
            setLevelPickerContainer( null, star, creator, score, rank, date )
            text_Speed.setText("")
        }
        if( levelScroller_index+1 <= currentLevels.size-1  &&  (levelScroller_index+1) < currentLevels.count()){
            setLevelPickerContainer( currentLevels[levelScroller_index+1], star_p1, creator_p1, score_p1, rank_p1, date_p1 )
        }else {
            setLevelPickerContainer( null, star_p1, creator_p1, score_p1, rank_p1, date_p1 )
        }
    }
    fun setLevelPickerContainer( leveldata:levelData?, _star:TextView, _creator:TextView, _score:TextView, _rank:TextView, _date:TextView ){
        if( leveldata == null ){
            _star.setText( "" )
            _creator.setText( "" )
            _score.setText( "" )
            _rank.setText( "" )
            _date.setText( "" )
            return
        }
        _star.setText( leveldata.getLevelAsString() )
        _creator.setText( leveldata.creator )
        if( !leveldata.isEditing ){
            if( userScore.scores[leveldata.sqlID] != null ){
                val scoredata = userScore.scores[leveldata.sqlID]!!
                println(scoredata)
                _score.setText( "HighScore: "+ scoredata[UserScore.SCORE] )
                var rankStr = Score.RankStr[scoredata[UserScore.RANK]]
                if( rankStr == "False" ){ rankStr = "" }
                _rank.setText( rankStr )
            }else {
                _score.setText("")
                _rank.setText("")
            }
        }else {
            _score.setText( "only you can see." )
            _rank.setText( "編集中" )
        }
        _date.setText( "" )

    }
    fun LevelPickerContainerSetScrollY(scrollY:Int) {
        val realScrollY = levelpickerContainer.height * (scrollY) / (scrollerOneHeight) / 3
        levelpickerContainer.layout(0, containerFirstPos-realScrollY, levelpickerContainer.width, containerFirstPos-realScrollY + levelpickerContainer.height)
    }


}