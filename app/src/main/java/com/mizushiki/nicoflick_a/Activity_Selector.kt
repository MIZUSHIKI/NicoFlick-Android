package com.mizushiki.nicoflick_a

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.text.Html
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener
import kotlinx.android.synthetic.main.activity_selector.*
import kotlinx.android.synthetic.main.activity_settings.progress_circular
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.text.SimpleDateFormat
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
    var maeMusicTags = ""
    var scrollingTimer: Timer? = null
    var maeMusic:musicData? = null
    var numberRoll_index = -1

    lateinit var coverflow: FeatureCoverFlow
    var containerFirstPos = 0
    var scrollerOneHeight = 100

    private var cacheExoPlayer: SimpleExoPlayer? = null
    private lateinit var playerView: PlayerView

    var segueing = false
    var leaved = false
    var flg_notHome = false
    var flg_thumbMovieStopping = false //曲選択後遅延して再生しているためGame遷移後に再生始まったりするのを防ぐフラグ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GLOBAL.Selector_instance = this
        setContentView(R.layout.activity_selector)

        //お気に入りソート関係
        button_levelSort.alpha = if (USERDATA.LevelSortCondition != 0) 0.6F else 0.2F
        button_musicSort.alpha = if (USERDATA.MusicSortCondition != 0) 0.6F else 0.2F
        //カバーフロー
        val mHandler = Handler()
        coverflow = findViewById<View>(R.id.coverflow) as FeatureCoverFlow
        val coverFlowAdapter = CoverFlowAdapter(this, currentMusics)
        coverflow.adapter = coverFlowAdapter
        coverflow.setReflectionOpacity(0xFF * 40/100)
        coverflow.setOnScrollPositionListener(object : OnScrollPositionListener {
            override fun onScrolling() {
                //println("scrolling"+coverflow.scrollPosition)
                if (scrollingTimer == null) {
                    val timerCallback1: TimerTask.() -> Unit = {
                        mHandler.post {
                            //println("TODO")
                            if (coverflow.isVisible && indexCoverFlow != coverflow.scrollPosition) {
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
                if (coverflow.isVisible) {
                    //println("pos=$position")
                    indexCoverFlow = position
                    scrollingTimer?.cancel()
                    scrollingTimer = null

                    if (currentMusics.indices.contains(indexCoverFlow)) {
                        maeMusic = currentMusics[indexCoverFlow]
                        println("maeMusic=$maeMusic")
                    }
                    ThumbMoviePlay()
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
                            scrollview_levelScroller.smoothScrollTo(0, indexPicker * scrollerOneHeight)
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
            RedrawLevelpickerSelection()
        }, 100)

        text_Tags.setOnClickListener {
            if( currentMusics.size > 0 ){
                if(segueing){ return@setOnClickListener }
                segueing = true
                flg_notHome = true
                maeTags = USERDATA.SelectedMusicCondition.tags
                maeSort = USERDATA.SelectedMusicCondition.sortItem
                maeMusicTags = currentMusics[indexCoverFlow].tags
                GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
                val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTableForTag::class.java)
                intent.putExtra("fromSelector", true)
                startActivityForResult(intent, 1002)
            }
        }
        text_Title_c.setOnClickListener {
           gotoSelectorMenuTableForSort()
        }
        text_Artist.setOnClickListener {
            gotoSelectorMenuTableForSort()
        }
        text_Length.setOnClickListener {
            gotoSelectorMenuTableForSort()
        }
        text_Speed.setOnClickListener {
            gotoSelectorMenuTableForSort()
        }
        text_Num.setOnClickListener {

            val a = currentMusics.indices.map { (it + 1).toString() }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("曲の選択")
                .setItems(a){ dialog, which ->
                    numberRoll_index = which
                    indexCoverFlow = -1 //CoverFlowリロード
                    SetMusicToCoverFlow()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        //
        scrollview_levelScroller.isHorizontalFadingEdgeEnabled = true
        //showChangeFavoSpecView() //もういいか
        playerView = findViewById(R.id.playerView)
        // FrameLayout のインスタンスを取得
        val slashShadeLayout: FrameLayout = findViewById(R.id.playerView_SlashShade_layout)
        val slashShadeView = SlashShadeView(this)
        slashShadeLayout.addView(slashShadeView)
        if( USERDATA.thumbMoviePlay ){
            button_thumbMoviePlay.alpha = 1.0F
            button_thumbMoviePlay.background.colorFilter = PorterDuffColorFilter(
                0xFFFF2D71.toInt(),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
    fun gotoSelectorMenuTableForSort() {
        if( currentMusics.size <= 0 ){
            return
        }
        if(segueing){ return }
        segueing = true
        flg_notHome = true
        maeTags = USERDATA.SelectedMusicCondition.tags
        maeSort = USERDATA.SelectedMusicCondition.sortItem
        maeMusicTags = currentMusics[indexCoverFlow].tags
        GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
        val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTableForSort::class.java)
        intent.putExtra("fromSelector", true)
        startActivityForResult(intent, 1004)
    }

    fun SetMusicToCoverFlow() {
        progress_circular.isVisible = true

        musicDatas.getSelectMusics {

            currentMusics = it
            //println("tags="+USERDATA.SelectedMusicCondition.tags)
            //println("currentMusics.size="+currentMusics.size)

            coverflow = findViewById<View>(R.id.coverflow) as FeatureCoverFlow
            if(indexCoverFlow == -1){
                var scrollPos = 0
                if( currentMusics.size > 0 ){
                    coverflow.clearCache()
                    val coverFlowAdapter = CoverFlowAdapter(this, it)
                    coverflow.adapter = coverFlowAdapter
                    coverflow.setShouldRepeat(false)
                    //前回選択していた曲に飛べれば飛ぶ
                    if( numberRoll_index == -1 ){
                        maeMusic?.let {
                            val selectMusic = it
                            val firstIndex = currentMusics.indexOfFirst { it.movieURL == selectMusic.movieURL }
                            if( firstIndex>=0 ){
                                scrollPos = firstIndex
                            }
                        }
                    }else {
                        scrollPos = numberRoll_index
                        numberRoll_index = -1
                    }
                    println("scrollPos=${scrollPos}")
                    indexCoverFlow = scrollPos
                }else{
                    coverflow.isVisible = false
                }
                Handler().postDelayed(Runnable {
                    //coverflowセット後、遅延させて選択
                    coverflow.scrollToPosition(scrollPos)
                }, 200)
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
            htmlText = htmlText.pregReplace(
                "(^|\\s)(${Regex.escape(tag)})(\\s|$)",
                "$1<font color='#000000'>$2</font>$3"
            )

        }
        text_Tags.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT))

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
        levelScrollerContainer.addView(
            View(applicationContext), LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                scrollerOneHeight * (currentLevels.size + 1)
            )
        )

        LevelPickerContainerRedraw()
    }

    fun ShowHowToExtendView():Boolean{

        if(USERDATA.lookedExtend == false){
            var musicIDSet:MutableSet<Int> = mutableSetOf()
            for((sKey, _) in USERDATA.Score.scores){
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
    fun showChangeFavoSpecView() {
        if(USERDATA.lookedChangeFavoSpec_v1500){
            return
        }
        USERDATA.lookedChangeFavoSpec_v1500 = true

        Handler().postDelayed(Runnable {
            segueing = false
            val intent: Intent = Intent(applicationContext, Activity_AboutVerUp1500::class.java)
            startActivity(intent)
        }, 750)
    }

    fun Button_Go(view: View) {
        if( currentMusics.size < 1 || currentLevels.size < 1 ){ return }

        if(segueing) {
            return
        }
        segueing = true
        flg_notHome = true
        println(currentLevels[indexPicker].noteData)
        println("c == ${currentLevels[indexPicker].noteData}")
        ThumbMovieStop()
        CachedThumbMovies.allRelease()
        //すでにゲームデータを持っている場合
        if(currentLevels[indexPicker].noteData.count() >= 20){
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
        flg_notHome = true
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
        ThumbMovieStop()
        finish()
    }
    override fun onBackPressed() {
        if(segueing) {
            return
        }
        ThumbMovieStop()
        finish()
    }
    fun Button_RankingComment(view: View) {
        if(indexPicker==-1){ return }
        if(segueing) {
            return
        }
        segueing = true
        flg_notHome = true
        val intent: Intent = Intent(applicationContext, Activity_RankingComment::class.java)
        GLOBAL.SelectMUSIC = currentMusics[indexCoverFlow]
        println("selector title =" + GLOBAL.SelectMUSIC!!.title)
        GLOBAL.SelectLEVEL = currentLevels[indexPicker]
        startActivityForResult(intent, 1003)
    }
    fun Button_StartPage(view: View) {
        println("gotoStartPage")
        ThumbMovieStop()
        val intent: Intent = Intent(applicationContext, Activity_WikiPageWeb::class.java)
        startActivityForResult(intent, 1005)
    }
    fun Button_favorite(view: View) {
        println("favorite")
        if( currentMusics.indices.contains(indexCoverFlow) == false ){
            return
        }
        val selectMovieURL:String = currentMusics[indexCoverFlow].movieURL
        if( musicDatas.levels.size > 0 && musicDatas.levels[selectMovieURL] == null ){
            println()
            return
        }
        val currentLevel = currentLevels[indexPicker]
        if( USERDATA.MyFavorite2.contains(currentLevel.sqlID) ) {
            val myFavorite = USERDATA.MyFavorite2
            myFavorite.remove(currentLevel.sqlID)
            USERDATA.MyFavorite2 = myFavorite //ここでDataStoreに保存される
            USERDATA.FavoriteCount.subFavoriteCount(levelID = currentLevel.sqlID)
        }else {
            if( USERDATA.MyFavorite.contains(currentLevel.sqlID) ){
                //Ver.1.5未満の互換
                val myFavorite = USERDATA.MyFavorite
                myFavorite.remove(currentLevel.sqlID)
                USERDATA.MyFavorite = myFavorite //ここでDataStoreに保存される
                //プレイしたことのあるレベルだけお気に入りできるようにする
                if( USERDATA.Score.scores.keys.contains(currentLevel.sqlID) ){
                    val myFavo = USERDATA.MyFavorite2
                    myFavo.add(currentLevel.sqlID)
                    USERDATA.MyFavorite2 = myFavo //ここでDataStoreに保存される
                    USERDATA.FavoriteCount.addFavoriteCount(levelID = currentLevel.sqlID)
                }
            }else {
                //プレイしたことのあるレベルだけお気に入りできるようにする
                if( USERDATA.Score.scores.keys.contains(currentLevel.sqlID) ){
                    val myFavo = USERDATA.MyFavorite2
                    myFavo.add(currentLevel.sqlID)
                    USERDATA.MyFavorite2 = myFavo //ここでDataStoreに保存される
                    USERDATA.FavoriteCount.addFavoriteCount(levelID = currentLevel.sqlID)
                }else {

                    val strList = arrayOf("譜面が良かったらお気に入りしよう！")
                    AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                        .setTitle("ゲームをプレイしたらお気に入りできるようになります。")
                        .setItems(strList, null)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
        star_black.isVisible = USERDATA.MyFavorite.contains(currentLevel.sqlID)
        star_color.isVisible = USERDATA.MyFavorite2.contains(currentLevel.sqlID)

    }
    fun Button_LevelSort(view: View) {
        println("levelSort")

        val strList = arrayOf("なし", "お気に入りを上に集める")
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("お気に入りソート")
            .setItems(strList, { dialog, which ->
                when (which) {
                    0 -> {
                        USERDATA.LevelSortCondition = 0
                        SetMusicToCoverFlow()
                        button_levelSort.alpha = if (USERDATA.LevelSortCondition != 0) 0.6F else 0.2F
                    }
                    1 -> {
                        USERDATA.LevelSortCondition = 1
                        SetMusicToCoverFlow()
                        button_levelSort.alpha = if (USERDATA.LevelSortCondition != 0) 0.6F else 0.2F
                    }
                }
            })
            .setPositiveButton("キャンセル", null)
            .show()
    }
    fun Button_MusicSort(view: View) {

        val strList = arrayOf("なし", "お気に入りを含む曲を先頭に集める")
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("お気に入りソート")
            .setItems(strList, { dialog, which ->
                when (which) {
                    0 -> {
                        USERDATA.MusicSortCondition = 0
                        indexCoverFlow = -1 //CoverFlowリロード
                        SetMusicToCoverFlow()
                        button_musicSort.alpha = if (USERDATA.MusicSortCondition != 0) 0.6F else 0.2F
                    }
                    1 -> {
                        USERDATA.MusicSortCondition = 1
                        indexCoverFlow = -1 //CoverFlowリロード
                        SetMusicToCoverFlow()
                        button_musicSort.alpha = if (USERDATA.MusicSortCondition != 0) 0.6F else 0.2F
                    }
                }
            })
            .setPositiveButton("キャンセル", null)
            .show()
    }

    fun Button_thumbMoviePlay(view: View) {
        USERDATA.thumbMoviePlay = !USERDATA.thumbMoviePlay
        println(USERDATA.thumbMoviePlay)
        if( USERDATA.thumbMoviePlay ){
            ThumbMovieStart(indexCoverFlow)
            button_thumbMoviePlay.alpha = 1.0F
            button_thumbMoviePlay.background.colorFilter = PorterDuffColorFilter(
                0xFFFF2D71.toInt(),
                PorterDuff.Mode.SRC_IN
            )
        }else {
            button_thumbMoviePlay.alpha = 0.2F
            button_thumbMoviePlay.background.colorFilter = null
            ThumbMovieStop()
        }
    }
    fun ThumbMoviePlay(){
        if( !USERDATA.thumbMoviePlay ){
            return
        }
        //ThumbMovie呼び込み
        // 一度すべて止める
        ThumbMovieStop()
        // 裏側で再生
        ThumbMovieStart(indexCoverFlow)
    }
    fun ThumbMovieStop(){
        flg_thumbMovieStopping = true
        //一度すべて止める
        for( avPlayerVC in CachedThumbMovies.cachedMovies ){
            avPlayerVC.simpleExoPlayer.playWhenReady = false
            playerView_Alpha.alpha = 1.0f
        }
    }
    fun ThumbMovieStart(index: Int, loadOnly: Boolean = false){
        flg_thumbMovieStopping = false
        if( currentMusics.indices.contains(index) == false ){
            return
        }
        val smNum = currentMusics[index].movieURL.pregMatche_firstString("watch/(.+)$")
        if( smNum == "" ){
            return
        }
        //val index = indexCoverFlow
        Handler().postDelayed(Runnable { //0.3秒後
            if (!USERDATA.thumbMoviePlay) {
                return@Runnable
            }
            if (index == indexCoverFlow || loadOnly == true) {
                //Movie呼び込み
                var nicodougaURL = ""
                println("thumbMovie ${smNum}")
                MovieAccess(ecoThumb = true).StreamingUrlNicoAccess(smNum) {
                    // 動画
                    nicodougaURL = it!!
                    println("smNum=$smNum, loadOnly=$loadOnly, nicodougaURL=$nicodougaURL")
                    if (flg_thumbMovieStopping == true) {
                        println("thumbMovieStopping")
                        return@StreamingUrlNicoAccess
                    }
                    if (loadOnly == true && nicodougaURL == "cached") {
                        return@StreamingUrlNicoAccess
                    }
                    if (index != indexCoverFlow && loadOnly == false) {
                        return@StreamingUrlNicoAccess
                    }
                    //println(applicationContext.packageName)
                    cacheExoPlayer = CachedThumbMovies.access(nicodougaURL, smNum)
                    if (loadOnly == false) {
                        playerView_Alpha.alpha = 1.0f
                        playerView.player = cacheExoPlayer
                    }
                    cacheExoPlayer!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    var t30 = musicDatas.getNotesFirstTime(currentMusics[index].movieURL)
                    if (t30 < 0.0) {
                        t30 = 30.0
                    } else {
                        t30 -= 3.0
                        if (t30 < 0.0) {
                            t30 = 0.0
                        }
                    }
                    println("t30 = ${t30}")
                    if (loadOnly == false) {
                        cacheExoPlayer!!.addListener(object : Player.EventListener {
                            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                                println("playbackState=" + playbackState)
                                when (playbackState) {
                                    ExoPlayer.STATE_ENDED -> {
                                        println("終了でループ")
                                        GlobalScope.launch(Dispatchers.Main) {
                                            playerView.player?.seekTo((t30 * 1000).toLong())
                                        }
                                    }
                                }
                            }
                        })
                        var firstRun = true
                        for (i in 1..10) {
                            cacheExoPlayer!!
                                .createMessage({ messageType, payload ->
                                    println("フェードイン$i")
                                    GlobalScope.launch(Dispatchers.Main) {
                                        val player = playerView.player as SimpleExoPlayer
                                        player.volume = 0.15f * i / 10
                                    }
                                    playerView_Alpha.alpha = 1.0f - 0.25f * i / 10
                                    if (i == 1 && firstRun == true) {
                                        firstRun = false
                                        //前後の曲を読み込んでおく
                                        val a = if (index + 1 >= currentMusics.size) 0 else index + 1
                                        val b = if (index - 1 < 0) currentMusics.size - 1 else index - 1
                                        println("inab = $index, $a, $b")
                                        ThumbMovieStart(a, true)
                                        ThumbMovieStart(b, true)
                                    }
                                })
                                .setPosition(((t30 + 0.05 * i) * 1000).toLong())
                                .setDeleteAfterDelivery(false)
                                .send()
                            cacheExoPlayer!!
                                .createMessage({ messageType, payload ->
                                    println("フェードアウト$i")
                                    GlobalScope.launch(Dispatchers.Main) {
                                        val player = playerView.player as SimpleExoPlayer
                                        player.volume = 0.15f * (10 - i) / 10
                                    }
                                    playerView_Alpha.alpha = 1.0f - 0.25f * (10 - i) / 10
                                })
                                .setPosition(((t30 + 29.0 + 0.1 * i) * 1000).toLong())
                                .setDeleteAfterDelivery(false)
                                .send()
                        }
                        cacheExoPlayer!!
                            .createMessage({ messageType, payload ->
                                println("30秒でループ")
                                GlobalScope.launch(Dispatchers.Main) {
                                    playerView.player?.seekTo((t30 * 1000).toLong())
                                }
                            })
                            .setPosition(((t30 + 30.0 + 0.1) * 1000).toLong())
                            .setDeleteAfterDelivery(false)
                            .send()

                        var nonSeek = false
                        CachedThumbMovies.cachedMovies.firstOrNull { it.smNum == smNum }?.let {
                            println("smNum=$smNum|it=${it.smNum} cacheing just=${it.just}")
                            if (it.just) {
                                nonSeek = true
                            }
                            it.just = false
                        }
                        println("nonSeek=$nonSeek")
                        GlobalScope.launch(Dispatchers.Main) {
                            if (!nonSeek) {
                                cacheExoPlayer!!.seekTo((t30 * 1000).toLong())
                            }
                            cacheExoPlayer!!.volume = 0.001f
                            cacheExoPlayer!!.playWhenReady = true
                        }

                    } else {
                        //読み込みのみ
                        cacheExoPlayer!!
                            .createMessage({ messageType, payload ->
                                if (currentMusics[indexCoverFlow].movieURL.pregMatche_firstString("watch/(.+)$") != smNum) {
                                    println("再生された形跡見つけたらムービーを止める")
                                    CachedThumbMovies.cachedMovies.firstOrNull { it.smNum == smNum }?.let {
                                        println("smNum=$smNum|it=${it.smNum} cacheing just=${it.just}")
                                        it.just = true
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        cacheExoPlayer!!.playWhenReady = false
                                        //cacheExoPlayer!!.seekTo( (t30*1000).toLong() )
                                    }
                                }
                            })
                            .setPosition(((t30) * 1000).toLong())
                            .setDeleteAfterDelivery(true)
                            .send()

                        val t30_ = if (t30 - 0.5 >= 0) (t30 - 0.5) else 0.0
                        GlobalScope.launch(Dispatchers.Main) {
                            cacheExoPlayer!!.seekTo((t30_ * 1000).toLong())
                            cacheExoPlayer!!.volume = 0.001f
                            cacheExoPlayer!!.playWhenReady = true
                        }
                    }

                }
            }
        }, 300)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        segueing = false
        // startActivityForResult()の際に指定した識別コードとの比較
        when( requestCode ){
            1001 -> {
                userScore = USERDATA.Score
                setCurrentLevels(indexCoverFlow)
                if (!ShowHowToExtendView()) {
                    //ユーザーネームデータをロードするため遷移させないようにする
                    segueing = true
                    progress_circular.isVisible = true
                    ServerDataHandler().Chance_DownloadUserNameData_FirstData {
                        segueing = false
                        progress_circular.isVisible = false
                    }
                }
                ThumbMoviePlay()
            }
            1002 -> {
                if (maeTags != USERDATA.SelectedMusicCondition.tags || maeMusicTags != currentMusics[indexCoverFlow].tags || maeSort != USERDATA.SelectedMusicCondition.sortItem) {
                    indexCoverFlow = -1 //CoverFlowリロード
                    SetMusicToCoverFlow()
                }
            }
            1004 -> {
                if (maeSort != USERDATA.SelectedMusicCondition.sortItem) {
                    indexCoverFlow = -1 //CoverFlowリロード
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
            1005 -> {
                val modoriStr = data?.getStringExtra("modori") ?: ""
                println("url = $modoriStr")
                if (modoriStr != "") {
                    val retStr = URLDecoder.decode(modoriStr, "UTF-8")
                    println(retStr)
                    var retTag = retStr.pregMatche_firstString("tag=(.*?)(&sort=|$)")
                    val retSort = retStr.pregMatche_firstString("sort=(.*?)(&tag=|$)")
                    retTag = retTag.trim()
                    retTag = retTag.pregReplace("\\s*/(and|AND)/\\s", "/and/")
                    retTag = retTag.pregReplace("\\s+", " or ")
                    retTag = retTag.pregReplace("/and/", " ")

                    USERDATA.SelectedMusicCondition.tags = retTag
                    if (retSort != "") {
                        USERDATA.SelectedMusicCondition.sortItem = retSort
                    }

                    indexCoverFlow = -1 //CoverFlowリロード
                    maeMusic = null
                    SetMusicToCoverFlow()
                }
                ThumbMoviePlay()
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LevelPicker処理 //
    fun LevelPickerContainerRedraw() {
        if( (levelScroller_index-1) >= 0 &&  (levelScroller_index-1) < currentLevels.count()) {
            setLevelPickerContainer(
                currentLevels[levelScroller_index - 1],
                star_m1,
                creator_m1,
                score_m1,
                rank_m1,
                date_m1
            )
        }else {
            setLevelPickerContainer(null, star_m1, creator_m1, score_m1, rank_m1, date_m1)
        }
        if( levelScroller_index != -1 &&  (levelScroller_index) < currentLevels.count() ){
            setLevelPickerContainer(currentLevels[levelScroller_index], star, creator, score, rank, date)
            UpdateObject_Level(currentLevels[levelScroller_index])
        }else {
            setLevelPickerContainer(null, star, creator, score, rank, date)
            UpdateObject_Level(null)
        }
        if( levelScroller_index+1 <= currentLevels.size-1  &&  (levelScroller_index+1) < currentLevels.count()){
            setLevelPickerContainer(
                currentLevels[levelScroller_index + 1],
                star_p1,
                creator_p1,
                score_p1,
                rank_p1,
                date_p1
            )
        }else {
            setLevelPickerContainer(null, star_p1, creator_p1, score_p1, rank_p1, date_p1)
        }
        RedrawLevelpickerSelection()
    }
    fun UpdateObject_Level(currentLevel: levelData?){
        if( currentLevel == null ){
            text_Speed.setText("")
            star_color.isVisible = false
            return
        }
        currentLevel?.let {
            val currentLevel = it
            //スピード反映
            text_Speed.setText("speed: " + currentLevel.speed)
            //favorite反映
            star_color.isVisible = USERDATA.MyFavorite2.contains(currentLevel.sqlID)
            star_black.isVisible = USERDATA.MyFavorite.contains(currentLevel.sqlID)
            println(USERDATA.MyFavorite)
            println(USERDATA.MyFavorite.contains(currentLevel.sqlID))
            val fc = Int(currentLevel.favoriteCount / 5) * 5
            text_FavoriteNum.text = String(fc)
            text_FavoriteNum2.text = String(fc)
            text_FavoriteNum3.text = String(fc)
            text_FavoriteNum.isVisible = ( fc > 0)
            text_FavoriteNum2.isVisible = ( fc > 0)
            text_FavoriteNum3.isVisible = ( fc > 0)
            val nowTime = (Date().time / 1000).toInt()
            val sDateFormat = SimpleDateFormat("yyy.MM.dd")
            if( (USERDATA.SelectedMusicCondition.sortItem == "最近ハイスコアが更新された曲順" || (nowTime - currentLevel.scoreTime)<2592000 ) && currentLevel.scoreTime > 0 ){
                text_RankingTime.text = sDateFormat.format(Date(currentLevel.scoreTime * 1000L))
                text_RankingTime.isVisible = true
            }else{
                text_RankingTime.isVisible = false
            }
            if( (USERDATA.SelectedMusicCondition.sortItem == "最近コメントされた曲順" || (nowTime - currentLevel.commentTime)<2592000 ) && currentLevel.commentTime > 0 ){
                text_CommentTime.text = sDateFormat.format(Date(currentLevel.commentTime * 1000L))
                text_CommentTime.isVisible = true
            }else{
                text_CommentTime.isVisible = false
            }

        }
    }
    fun setLevelPickerContainer(
        leveldata: levelData?,
        _star: TextView,
        _creator: TextView,
        _score: TextView,
        _rank: TextView,
        _date: TextView
    ){
        if( leveldata == null ){
            _star.setText("")
            _creator.setText("")
            _score.setText("")
            _rank.setText("")
            _date.setText("")
            return
        }
        _star.setText(leveldata.getLevelAsString())
        _creator.setText(leveldata.creator)
        if( !leveldata.isEditing ){
            if( userScore.scores[leveldata.sqlID] != null ){
                val scoredata = userScore.scores[leveldata.sqlID]!!
                println(scoredata)
                _score.setText("HighScore: " + scoredata[UserScore.SCORE])
                var rankStr = Score.RankStr[scoredata[UserScore.RANK]]
                if( rankStr == "False" ){ rankStr = "" }
                _rank.setText(rankStr)
            }else {
                _score.setText("")
                _rank.setText("")
            }
        }else {
            _score.setText("only you can see.")
            _rank.setText("編集中")
        }
        _date.setText("")

    }
    fun LevelPickerContainerSetScrollY(scrollY: Int) {
        val realScrollY = levelpickerContainer.height * (scrollY) / (scrollerOneHeight) / 3
        levelpickerContainer.layout(
            0,
            containerFirstPos - realScrollY,
            levelpickerContainer.width,
            containerFirstPos - realScrollY + levelpickerContainer.height
        )
        RedrawLevelpickerSelection()
    }
    fun RedrawLevelpickerSelection(){
        val view = levelpickerContainer
        if( view.width <=0 || view.height <= 0 ){
            return
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate(0F, levelpickerContainer.y)
        view.draw(canvas)
        // ? アルファ値グラデーションのフィルタが作れるかと思ったら無理(?)だった。
        //val paint = Paint()
        //val shader = LinearGradient(0F,0F,0F,50F,0xff000000,0x00000000,Shader.TileMode.CLAMP)
        //paint.shader = shader
        //paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        //canvas.drawRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), paint)
        val redrawBitmap = Bitmap.createBitmap(bitmap,0,outputImageView_levelScroller.height * 3/4,outputImageView_levelScroller.width,outputImageView_levelScroller.height)
        outputImageView_levelScroller.setImageBitmap(redrawBitmap)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(flg_notHome == false) {
            println("Leave!")
            leaved = true
            //Thumb止め
            ThumbMovieStop()
        }
    }
    override fun onResume() {
        super.onResume()
        println("Resume!")
        if( leaved && USERDATA.thumbMoviePlay ){
            //Thumb開始
            ThumbMoviePlay()
        }
        leaved = false
        flg_notHome = false
    }
}