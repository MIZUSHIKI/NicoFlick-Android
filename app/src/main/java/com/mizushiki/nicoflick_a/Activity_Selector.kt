package com.mizushiki.nicoflick_a

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.PlayerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.text.Html
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.doOnNextLayout
import com.mizushiki.nicoflick_a.databinding.ActivitySelectorBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import it.moondroid.coverflow.components.ui.containers.StableFeatureCoverFlow
import it.moondroid.coverflow.components.ui.containers.StableFeatureCoverFlow.OnScrollPositionListener

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule


class Activity_Selector : AppCompatActivity() {

    private lateinit var binding: ActivitySelectorBinding

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
    private var coverFlowScrolling = false
    var maeMusic:musicData? = null
    var numberRoll_index = -1

    val coverflow: StableFeatureCoverFlow get() = binding.coverflow
    var scrollerOneHeight = 100
    private var levelPickerBitmap: Bitmap? = null
    private var levelPickerBitmapDirty = true

    private var cacheExoPlayer: SimpleExoPlayer? = null
    private val selectorHandler = Handler(Looper.getMainLooper())
    private val selectorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var thumbGeneration = 0L
    private val thumbListeners = mutableMapOf<SimpleExoPlayer, Player.EventListener>()
    private val thumbMessages = mutableListOf<Pair<SimpleExoPlayer, PlayerMessage>>()
    private val thumbRequests = mutableSetOf<kotlinx.coroutines.Job>()
    private fun clearThumbCallbacks() {
        thumbRequests.toList().forEach { it.cancel() }
        thumbRequests.clear()
        thumbMessages.forEach { it.second.cancel() }
        thumbMessages.clear()
        thumbListeners.forEach { (player, listener) -> player.removeListener(listener) }
        thumbListeners.clear()
    }

    private fun ownsThumbRequest(generation: Long): Boolean =
        generation == thumbGeneration && !isFinishing && !isDestroyed

    private val playerView: PlayerView get() = binding.playerView

    private val navigation = DeferredNavigation()
    var segueing = false
    var leaved = false
    var flg_notHome = false
    var flg_thumbMovieStopping = false //曲選択後遅延して再生しているためGame遷移後に再生始まったりするのを防ぐフラグ
    var flg_thumbMovieTopMost = false
    var volumeForceInResult : Float = 1.0f
    val ReducedVolumeForceInResult : Float = 0.5f

    enum class ForceInResultType {
        none, play, loadOnly
    }

    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        if (segueing || isFinishing || isDestroyed) return true
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GLOBAL.Selector_instance = this
        binding = ActivitySelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        USERDATA.migrateFavoritesV2000()
        USERDATA.migrateScoreLevelsV2000()

        //カバーフロー
        val mHandler = selectorHandler

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // CoverFlow 内の横スワイプを、画面端の「戻る」ジェスチャーより優先する。
            // 初回レイアウトとサイズ変更時に、View 内の座標で除外領域を更新する。
            coverflow.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
                // 通常の高さ制限は200dp。従来の下端基準の範囲を50px上へ移動する。
                val exclusionHeightPx = (200 * view.resources.displayMetrics.density).toInt()
                    .coerceAtMost(view.height)
                val offsetUpPx = 100 // 大きくすると、除外領域がさらに上へ移動する。
                val bottom = (view.height - offsetUpPx).coerceIn(exclusionHeightPx, view.height)
                view.systemGestureExclusionRects = listOf(
                    Rect(0, bottom - exclusionHeightPx, view.width, bottom)
                )
            }
        }
        coverflow.setShouldRepeat(currentMusics.size > 1)
        val coverFlowAdapter = CoverFlowAdapter(this, currentMusics)
        coverflow.adapter = coverFlowAdapter
        coverflow.setReflectionOpacity(0xFF * 40/100)
        // false: 高速移動中も反射を更新。true: 従来どおり停止後に更新。
        coverflow.setDeferReflectionUpdatesWhileScrolling(false)
        coverflow.setOnScrollPositionListener(object : OnScrollPositionListener {
            override fun onScrolling() {
                coverFlowScrolling = true
                //println("scrolling"+coverflow.scrollPosition)
                if (scrollingTimer == null) {
                    val timerCallback1: TimerTask.() -> Unit = {
                        mHandler.post {
                            if (isFinishing || isDestroyed) return@post
                            //println("TODO")
                            val coverflow_scrollPosition = if (currentMusics.size == 1) {
                                0
                            } else {
                                coverflow.scrollPosition
                            }
                            if (coverflow.isVisible && indexCoverFlow != coverflow_scrollPosition) {
                                SESystemAudio.shuffleSePlay()
                                indexCoverFlow = coverflow_scrollPosition
                                //println("index=" + coverflow_scrollPosition)
                                setCurrentLevels(indexCoverFlow)
                            }
                        }
                    }
                    scrollingTimer = Timer()
                    scrollingTimer!!.schedule(0, 100, timerCallback1)
                }
                if (flg_thumbMovieTopMost) {
                    flg_thumbMovieTopMost = false
                    //thumbMovieを最前面表示していたら奥に戻す
                    setThumbMovieMaeUsiro(usiro = true)
                }
            }

            override fun onScrolledToPosition(position: Int) {
                coverFlowScrolling = false
                if (coverflow.isVisible) {
                    //println("pos=$position")
                    indexCoverFlow = position
                    scrollingTimer?.cancel()
                    scrollingTimer = null
                    if( currentMusics.size == 1 ){
                        indexCoverFlow = 0
                    }

                    if (currentMusics.indices.contains(indexCoverFlow)) {
                        // Programmatic jumps do not run the scrolling timer. Always commit
                        // the centered music and its levels together before playing video.
                        setCurrentLevels(indexCoverFlow)
                        if( currentMusics.size == 1 && maeMusic == currentMusics[indexCoverFlow] ){
                            return
                        }
                        maeMusic = currentMusics[indexCoverFlow]
                        // println("maeMusic=$maeMusic")
                    }
                    ThumbMoviePlay()
                }
            }

        })
        coverflow.setOnItemClickListener{ parent: AdapterView<*>?, view: View?, position: Int, id: Long -> Unit
            // println("CoverFlow Tap ${position} ${id}")

            if( !USERDATA.thumbMoviePlay ){ return@setOnItemClickListener }
            if( flg_thumbMovieStopping ){ return@setOnItemClickListener }
            if( flg_thumbMovieTopMost ){ return@setOnItemClickListener }
            flg_thumbMovieTopMost = true //thumbMovieを最前面再生しているかどうか
            volumeForceInResult = 1.0f

            setThumbMovieMaeUsiro(usiro = false)

        }
        //レベルピッカー(スクロールビューVer)
        scrollerOneHeight = binding.levelScrollerView.layoutParams.height / 2

        binding.scrollviewLevelScroller.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
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
                LevelPickerContainerRedraw(redraw = false)
            }
            LevelPickerContainerSetScrollY(scrollY % scrollerOneHeight)
        }
        binding.scrollviewLevelScroller.setOnTouchListener { v, event ->
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
                            binding.scrollviewLevelScroller.smoothScrollTo(0, indexPicker * scrollerOneHeight)
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
        selectorHandler.postDelayed(Runnable {
            RedrawLevelpickerSelection()
        }, 100)

        binding.textTags.setOnClickListener {
            val selectedMusic = currentMusics.getOrNull(indexCoverFlow) ?: return@setOnClickListener
            if( currentMusics.size > 0 ){
                if (segueing || coverFlowScrolling) return@setOnClickListener
                SESystemAudio.openSePlay()
                segueing = true
                flg_notHome = true
                maeTags = USERDATA.SelectedMusicCondition.tags
                maeSort = USERDATA.SelectedMusicCondition.sortItem
                maeMusicTags = selectedMusic.tags
                GLOBAL.SelectMUSIC = selectedMusic
                val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTableForTag::class.java)
                intent.putExtra("fromSelector", true)
                startActivityForResult(intent, 1002)
            }
        }
        binding.buttonSortMenu.setOnClickListener { gotoSelectorMenuTableForSort() }
        binding.textNum.setOnClickListener {
            if (segueing || coverFlowScrolling || isFinishing || isDestroyed) return@setOnClickListener
            SESystemAudio.openSePlay()
            val a = currentMusics.indices.map { (it + 1).toString() }.toTypedArray()
            val listView = ListView(this).apply {
                adapter = ArrayAdapter(this@Activity_Selector, android.R.layout.simple_list_item_1, a)
                // 初期レイアウトで行の高さを取得し、選択中の曲を中央に配置する。
                if (indexCoverFlow in a.indices) {
                    val selectedPosition = indexCoverFlow
                    setSelection(selectedPosition)
                    doOnNextLayout {
                        val rowHeight = getChildAt(0)?.height ?: return@doOnNextLayout
                        val contentHeight = height - paddingTop - paddingBottom
                        setSelectionFromTop(selectedPosition, (contentHeight - rowHeight) / 2)
                    }
                }
            }
            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            fun addScrollButton(label: String, description: String, position: Int) {
                val row = LinearLayout(this).apply {
                    gravity = Gravity.END
                }
                row.addView(Button(this).apply {
                    text = label
                    contentDescription = description
                    isEnabled = a.isNotEmpty()
                    setOnClickListener {
                        val currentPosition = listView.firstVisiblePosition
                        val threshold = 10
                        if (kotlin.math.abs(position - currentPosition) > threshold) {
                            val nearbyPosition = if (position > currentPosition) {
                                position - threshold*2
                            } else {
                                position + threshold
                            }
                            // ジャンプ後の行配置が確定してから短いアニメーションを開始する。
                            listView.doOnNextLayout {
                                listView.smoothScrollToPositionFromTop(position, 0, 300)
                            }
                            listView.setSelection(nearbyPosition.coerceIn(a.indices))
                        } else {
                            listView.smoothScrollToPositionFromTop(position, 0, 300)
                        }
                    }
                    post {
                        layoutParams = layoutParams.apply {
                            width = (measuredWidth * 0.8f).toInt()
                        }
                    }
                }, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ))
                content.addView(row)
            }
            addScrollButton("▲", "一覧の先頭へスクロール", 0)
            content.addView(listView, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (resources.displayMetrics.heightPixels * 0.5f).toInt()
            ))
            addScrollButton("▼", "一覧の末尾へスクロール", a.lastIndex)
            val dialog = AlertDialog.Builder(this)
                .setTitle("曲の選択")
                .setView(content)
                .setPositiveButton("キャンセル", null)
                .create()
            listView.setOnItemClickListener { _, _, which, _ ->
                numberRoll_index = which
                indexCoverFlow = -1 //CoverFlowリロード
                SetMusicToCoverFlow()
                dialog.dismiss()
            }
            dialog.show()
        }
        //
        binding.scrollviewLevelScroller.isHorizontalFadingEdgeEnabled = true

        // FrameLayout のインスタンスを取得
        val slashShadeLayout: FrameLayout = findViewById(R.id.playerView_SlashShade_layout)
        val slashShadeView = SlashShadeView(this)
        slashShadeLayout.addView(slashShadeView)
        if( USERDATA.thumbMoviePlay ){
            binding.buttonThumbMoviePlay.alpha = 1.0F
            binding.buttonThumbMoviePlay.setBackgroundResource(R.drawable.video_on)
        }
    }
    private fun setThumbMovieMaeUsiro(usiro:Boolean){
        if( !usiro ) {
            binding.playerViewOnCoverFlow.player = cacheExoPlayer
            playerView.isVisible = false
            binding.coverflowHuta.isVisible = true
            binding.playerViewOnCoverFlow.isVisible = true
            cacheExoPlayer?.volume = 0.18f * USERDATA.SoundVolumeMovie * volumeForceInResult
        }else {
            playerView.player = cacheExoPlayer
            playerView.isVisible = true
            binding.coverflowHuta.isVisible = false
            binding.playerViewOnCoverFlow.isVisible = false
            cacheExoPlayer?.volume = 0.10f * USERDATA.SoundVolumeMovie * volumeForceInResult
        }
    }
    fun gotoSelectorMenuTableForSort() {
        val selectedMusic = currentMusics.getOrNull(indexCoverFlow) ?: return
        if (segueing || coverFlowScrolling) return
        SESystemAudio.openSePlay()
        segueing = true
        flg_notHome = true
        maeTags = USERDATA.SelectedMusicCondition.tags
        maeSort = USERDATA.SelectedMusicCondition.sortItem
        maeMusicTags = selectedMusic.tags
        GLOBAL.SelectMUSIC = selectedMusic
        val intent: Intent = Intent(applicationContext, Activity_SelectorMenuTableForSort::class.java)
        intent.putExtra("fromSelector", true)
        startActivityForResult(intent, 1004)
    }

    fun SetMusicToCoverFlow() {
        SelectorDiagnostics.event { "SELECTOR_RELOAD instance=${System.identityHashCode(this)} index=$indexCoverFlow previousMusic=${maeMusic?.sqlID}" }
        val icons = listOf(R.drawable.music_new, R.drawable.music_old, R.drawable.flick_new, R.drawable.flick_old,
            R.drawable.plays_new, R.drawable.plays_old, R.drawable.favorites_many, R.drawable.favorites_few,
            R.drawable.video_id_large, R.drawable.video_id_small, R.drawable.highscore_recent, R.drawable.comment_recent,
            R.drawable.plays_recent, R.drawable.plays_user)
        val sort = USERDATA.SelectedMusicCondition.sortItem.pregReplace(" [★☆]{10}[■□]$", "")
        val position = USERDATA.SelectedMusicCondition.SortItem.indexOf(sort)
        binding.buttonSortMenu.setBackgroundResource(icons.getOrNull(position) ?: R.drawable.cursor_lr)
        binding.progressCircular.isVisible = true

        musicDatas.getSelectMusics {

            currentMusics = it

            if(indexCoverFlow !in currentMusics.indices){
                var scrollPos = 0
                if( currentMusics.size > 0 ){
                    coverflow.isVisible = true
                    coverflow.clearCache()
                    // Set before attaching: adapter initialization decides whether
                    // short lists can scroll. A single music needs no duplicate.
                    coverflow.setShouldRepeat(it.size > 1)
                    val coverFlowAdapter = CoverFlowAdapter(this, it)
                    coverflow.adapter = coverFlowAdapter
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
                        scrollPos = numberRoll_index.takeIf { it in currentMusics.indices } ?: 0
                        numberRoll_index = -1
                    }
                    // println("scrollPos=${scrollPos}")
                    SelectorDiagnostics.event { "SELECTOR_CENTER index=$scrollPos music=${currentMusics.getOrNull(scrollPos)?.sqlID} count=${currentMusics.size}" }
                    indexCoverFlow = scrollPos
                }else{
                    indexCoverFlow = -1
                    coverflow.isVisible = false
                    ThumbMovieStop() // 曲がゼロになったら背景動画を停止
                }
                // Establish the requested center before the adapter's temporary default
                // position can be drawn/notified. CoverFlow defers internally if unlaid out.
                if (scrollPos in currentMusics.indices) {
                    coverflow.scrollToPosition(scrollPos)
                }
            }
            binding.progressCircular.isVisible = false

            setCurrentLevels(indexCoverFlow)
        }

    }

    fun setCurrentLevels(index: Int) {
        val selectedMusic = currentMusics.getOrNull(index)
        if (selectedMusic == null) {
            //曲が一つもないとき
            binding.textTitleC.setText("")
            binding.textArtist.setText("")
            binding.textLength.setText("")
            binding.textTags.setText("")
            binding.textSpeed.setText("")
            binding.textNum.setText("")
            binding.star.isHidden = true
            binding.textFavoriteNum.isHidden = true
            binding.heartColor.isHidden = true
            binding.heartBlack.isHidden = true
            binding.heartMono.isHidden = true
            binding.textLikeNum.isHidden = true
            currentLevels = arrayListOf()
            indexPicker = -1
            levelScroller_index = -1
            LevelPickerContainerRedraw()
            return
        }
        binding.textTitleC.setText(HtmlNicokakuMixFixText( selectedMusic.title ))
        binding.textArtist.setText(HtmlNicokakuMixFixText( selectedMusic.artist ))
        binding.textLength.setText(selectedMusic.movieLength)
        binding.textTags.setText(selectedMusic.tags)
        var htmlText = selectedMusic.tags
        for( tagp in USERDATA.SelectedMusicCondition.tag ){
            val tag = tagp.word
            htmlText = htmlText.pregReplace(
                "(^|\\s)(${Regex.escape(tag)})(\\s|$)",
                "$1<font color='#000000'>$2</font>$3"
            )

        }
        binding.textTags.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT))

        binding.textNum.setText("${index + 1} / ${currentMusics.size}")


        if(musicDatas.levels[selectedMusic.movieURL]==null){
            currentLevels = arrayListOf()
        }else {
            currentLevels = musicDatas.getSelectMusicLevels(selectedMusic.movieURL)
        }

        indexPicker = if (currentLevels.isEmpty()) -1 else indexPicker.coerceIn(currentLevels.indices)
        levelScroller_index = if (currentLevels.isEmpty()) -1 else levelScroller_index.coerceIn(currentLevels.indices)
        // LinearLayout(levelScrollerContainer)内の Viewを新しく作り直してAddする（height変更がうまくできなかった）
        binding.levelScrollerContainer.removeAllViews()
        binding.levelScrollerContainer.addView(
            View(applicationContext), LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                scrollerOneHeight * (currentLevels.size + 1)
            )
        )
        binding.starColor.isVisible = selectedMusic.sqlID in USERDATA.MyFavorite3
        binding.starBlack.isVisible = false
        val count = (selectedMusic.favoriteCount / 5) * 5
        binding.textFavoriteNum.text = count.toString()
        binding.textFavoriteNum.isHidden = count == 0
        LevelPickerContainerRedraw()
    }

    fun ShowHowToExtendView():Boolean{

        if(USERDATA.lookedExtend == false){
            val musicIDSet:MutableSet<Int> = mutableSetOf()
            for(levelID in USERDATA.levelPlayHistories.keys){
                for( ms in musicDatas.musics ){
                    if( ms.levelIDs.contains(levelID) ){
                        musicIDSet.add(ms.sqlID)
                    }
                }
            }
            // println("$musicIDSet ; ${musicIDSet.size}")
            if( musicIDSet.size >= 3 ){
                USERDATA.lookedExtend = true
                segueing = true
                selectorHandler.postDelayed(Runnable {
                    navigation.submit {
                        if (!isFinishing && !isDestroyed) {
                            val intent = Intent(this, Activity_HowToExtend::class.java)
                            startActivityForResult(intent, 1006)
                        }
                    }
                }, 750)
                return true
            }
        }
        return false
    }

    fun Button_Go(view: View) {
        if( currentMusics.size < 1 || currentLevels.size < 1 ){ return }

        if (segueing || coverFlowScrolling) {
            return
        }
        val selectedMusic = currentMusics.getOrNull(indexCoverFlow) ?: return
        val selectedLevel = currentLevels.getOrNull(indexPicker) ?: return
        SESystemAudio.goSePlay()
        segueing = true
        flg_notHome = true
        // println(selectedLevel.noteData)
        // println("c == ${selectedLevel.noteData}")
        ThumbMovieStop()
        CachedThumbMovies.allRelease()
        //すでにゲームデータを持っている場合
        if(selectedLevel.noteData.count() >= 20){
            val intent: Intent = Intent(applicationContext, Activity_GameView::class.java)
            GLOBAL.SelectMUSIC = selectedMusic
            GLOBAL.SelectLEVEL = selectedLevel
            startActivityForResult(intent, 1001)
            return
        }
        //まだゲームデータがない場合ダウンロードしてから遷移する
        binding.progressCircular.isVisible = true
        var completed = false
        ServerDataHandler().DownloadTimetag(selectedLevel) { error ->
            if (completed || isFinishing || isDestroyed) return@DownloadTimetag
            completed = true
            navigation.submit {
            if (isFinishing || isDestroyed) return@submit
            binding.progressCircular.isVisible = false

            if (error != null || selectedLevel.noteData.count() < 20) {
                flg_notHome = false
                AlertDialog.Builder(this)
                    .setTitle("ゲームデータ取得エラー")
                    .setMessage("ゲームデータを取得できませんでした。時間をおいて、もう一度お試しください。")
                    .setPositiveButton("OK", null)
                    .setOnDismissListener {
                        if (!isFinishing && !isDestroyed) {
                            segueing = false
                            ThumbMoviePlay()
                        }
                    }
                    .show()
                return@submit
            }

            val intent: Intent = Intent(applicationContext, Activity_GameView::class.java)
            GLOBAL.SelectMUSIC = selectedMusic
            GLOBAL.SelectLEVEL = selectedLevel
            startActivityForResult(intent, 1001)
            }
        }
    }
    fun Button_Menu(view: View) {
        if (segueing || coverFlowScrolling) {
            return
        }
        SESystemAudio.openSelectorMenuSePlay()
        segueing = true
        flg_notHome = true
        val intent: Intent = Intent(applicationContext, Activity_SelectorMenu::class.java)
        GLOBAL.SelectMUSIC = currentMusics.getOrNull(indexCoverFlow)
        GLOBAL.SelectLEVEL = if (GLOBAL.SelectMUSIC != null) currentLevels.getOrNull(indexPicker) else null
        maeTags = USERDATA.SelectedMusicCondition.tags
        maeSort = USERDATA.SelectedMusicCondition.sortItem
        maeMusicTags = GLOBAL.SelectMUSIC?.tags.orEmpty()
        startActivityForResult(intent, 1002)
    }
    fun Button_Back(view: View) {
        if(segueing) {
            return
        }
        SESystemAudio.canselSePlay()
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
        val selectedMusic = currentMusics.getOrNull(indexCoverFlow) ?: return
        val selectedLevel = currentLevels.getOrNull(indexPicker) ?: return
        if (segueing || coverFlowScrolling) {
            return
        }
        SESystemAudio.openSePlay()
        segueing = true
        flg_notHome = true
        val intent: Intent = Intent(applicationContext, Activity_RankingComment::class.java)
        GLOBAL.SelectMUSIC = selectedMusic
        // println("selector title =" + selectedMusic.title)
        GLOBAL.SelectLEVEL = selectedLevel
        startActivityForResult(intent, 1003)
    }
    fun Button_StartPage(view: View) {
        if (segueing || coverFlowScrolling || isFinishing || isDestroyed) return
        segueing = true
        flg_notHome = true
        // println("gotoStartPage")
        ThumbMovieStop()
        SESystemAudio.openSePlay()
        val intent: Intent = Intent(applicationContext, Activity_WikiPageWeb::class.java)
        GLOBAL.SelectMUSIC = currentMusics.getOrNull(indexCoverFlow)
        startActivityForResult(intent, 1005)
    }
    fun Button_like(view:View) {
        if (segueing || coverFlowScrolling || !view.isEnabled) return
        val level = currentLevels.getOrNull(levelScroller_index) ?: return
        val previous = USERDATA.MyLike[level.sqlID] ?: -3
        var selected = previous
        val previousSort = USERDATA.LevelSortCondition
        val started = level.sqlID in USERDATA.levelPlayHistories.keys
        val hasScore = level.sqlID in USERDATA.Score.scores
        fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(dp(16), dp(32), dp(16), dp(32))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(24).toFloat()
            }
        }
        val nicoKaku = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.nicokaku_v1)
        for (title in listOf(level.getLevelAsString(), level.creator)) {
            panel.addView(TextView(this).apply {
                text = title
                setTextColor(Color.BLACK)
                textSize = 16f
                typeface = nicoKaku
                gravity = android.view.Gravity.CENTER
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            })
        }
        val hearts = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val buttons = mutableMapOf<Int, android.widget.ImageButton>()
        val labels = mapOf(
            -1 to R.string.level_like_dislike,
            1 to R.string.level_like_heart1,
            2 to R.string.level_like_heart2
        )
        fun updateHearts() {
            for ((rating, button) in buttons) {
                val lit = if (rating == 1) selected > 0 else selected == rating
                button.isSelected = lit
                button.setImageResource(
                    if (rating == -1) R.drawable.heart_break
                    else if (lit) R.drawable.heart_color else R.drawable.heart
                )
                button.imageAlpha = if (rating == -1 || lit) 255 else 130
                if (rating == -1) {
                    button.setColorFilter(if (lit) Color.rgb(40, 40, 40) else Color.rgb(225, 225, 225))
                } else {
                    button.clearColorFilter()
                }
                val action = if (selected == rating) R.string.level_like_clear else R.string.level_like_choose
                button.contentDescription = getString(action, getString(labels.getValue(rating)))
            }
        }
        for (rating in listOf(-1, 1, 2)) {
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
            val button = android.widget.ImageButton(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                setPadding(dp(6), dp(6), dp(6), dp(6))
            }
            buttons[rating] = button
            column.addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(96)))
            column.addView(TextView(this).apply {
                text = getString(labels.getValue(rating))
                setTextColor(Color.BLACK)
                textSize = 9f
                gravity = android.view.Gravity.CENTER
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(0)
            })
            button.setOnClickListener {
                // heart2 点灯中の中央タップは解除ではなく heart1 への切り替え。
                val next = if (selected == rating) 0 else rating
                if (next != 0 && ((rating == 2 && !hasScore) || (rating != 2 && !started))) {
                    AlertDialog.Builder(this)
                        .setMessage(if (rating == 2) R.string.level_like_requires_score else R.string.level_like_requires_play)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                } else {
                    selected = next
                    updateHearts()
                }
            }
            hearts.addView(column, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        updateHearts()
        panel.addView(hearts, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(20)
            bottomMargin = dp(28)
        })
        val sort = androidx.appcompat.widget.SwitchCompat(this).apply {
            text = getString(R.string.level_like_sort)
            setTextColor(Color.BLACK)
            textSize = 15f
            minHeight = dp(48)
            switchPadding = dp(12)
            isChecked = previousSort != 0
        }
        panel.addView(sort, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val scroll = android.widget.ScrollView(this).apply {
            isFillViewport = true
            background = panel.background
            addView(panel)
        }
        dialog.setContentView(scroll)
        dialog.setCanceledOnTouchOutside(true)
        view.isEnabled = false
        dialog.setOnDismissListener {
            view.isEnabled = true
            val ratingChanged = previous != selected
            val newSort = if (sort.isChecked) 1 else 0
            if (ratingChanged) {
                val likes = USERDATA.MyLike
                likes[level.sqlID] = selected
                USERDATA.MyLike = likes
                USERDATA.LikeCount.changeLikeCount(level.sqlID, maxOf(selected, 0) - maxOf(previous, 0))
                val dislikeDelta = (if (selected == -1) 1 else 0) - (if (previous == -1) 1 else 0)
                USERDATA.DislikeCount.changeLikeCount(level.sqlID, dislikeDelta)
            }
            USERDATA.LevelSortCondition = newSort
            if (ratingChanged || previousSort != newSort) SetMusicToCoverFlow()
            if (ratingChanged) ServerDataHandler().postUserCountData()
        }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { dimAmount = 0.3f }
            val availableWidth = binding.root.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
            val availableHeight = binding.root.height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
            setLayout(
                minOf(dp(360), (availableWidth * 0.8f).toInt()),
                minOf(dp(360), (availableHeight * 0.85f).toInt())
            )
            setGravity(android.view.Gravity.CENTER)
            // 中央から、利用可能な画面の高さの15%だけ上へ移動
            attributes = attributes.apply {
                y = -(availableHeight * 0.05f).toInt()
            }
        }
    }

    fun Button_MusicFavorite(view:View) {
        if (segueing || coverFlowScrolling || !view.isEnabled) return
        val music = currentMusics.getOrNull(indexCoverFlow) ?: return
        fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        var changed = false
        var favoriteChanged = false
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(40), dp(20), dp(40))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(24).toFloat()
            }
        }
        panel.addView(TextView(this).apply {
            text = getString(R.string.music_favorite_title)
            setTextColor(Color.BLACK)
            textSize = 20f
            typeface = androidx.core.content.res.ResourcesCompat.getFont(this@Activity_Selector, R.font.nicokaku_v1)
            gravity = android.view.Gravity.CENTER
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val favorite = android.widget.ImageButton(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        fun updateFavorite() {
            val selected = music.sqlID in USERDATA.MyFavorite3
            favorite.setImageResource(if (selected) R.drawable.star_color else R.drawable.star)
            favorite.imageAlpha = if (selected) 255 else 130
            favorite.isSelected = selected
            favorite.contentDescription = getString(
                if (selected) R.string.music_favorite_remove else R.string.music_favorite_add
            )
        }
        updateFavorite()
        panel.addView(favorite, LinearLayout.LayoutParams(dp(120), dp(120)).apply {
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            topMargin = dp(28)
            bottomMargin = dp(40)
        })
        favorite.setOnClickListener {
            val ids = USERDATA.MyFavorite3
            val selected = music.sqlID !in ids
            if (selected) ids.add(music.sqlID) else ids.remove(music.sqlID)
            USERDATA.MyFavorite3 = ids
            USERDATA.MusicFavoriteCount.changeLikeCount(music.sqlID, if (selected) 1 else -1)
            changed = true
            favoriteChanged = true
            updateFavorite()
        }
        panel.addView(androidx.appcompat.widget.SwitchCompat(this).apply {
            text = getString(R.string.music_favorite_sort)
            setTextColor(Color.BLACK)
            textSize = 15f
            minHeight = dp(48)
            switchPadding = dp(12)
            isChecked = USERDATA.MusicSortCondition != 0
            setOnCheckedChangeListener { _, checked ->
                USERDATA.MusicSortCondition = if (checked) 1 else 0
                changed = true
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        // 小さい画面や横向きでも、パネル全体をスクロールして操作できるようにする。
        val scroll = android.widget.ScrollView(this).apply {
            isFillViewport = true
            background = panel.background
            addView(panel)
        }
        dialog.setContentView(scroll)
        dialog.setCanceledOnTouchOutside(true)
        view.isEnabled = false
        dialog.setOnDismissListener {
            view.isEnabled = true
            if (changed) {
                maeMusic = music
                indexCoverFlow = -1
                SetMusicToCoverFlow()
            }
            if (favoriteChanged) ServerDataHandler().postUserCountData()
        }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { dimAmount = 0.3f }
            val availableWidth = binding.root.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
            val availableHeight = binding.root.height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
            setLayout(
                minOf(dp(360), (availableWidth * 0.8f).toInt()),
                minOf(dp(360), (availableHeight * 0.85f).toInt())
            )
            setGravity(android.view.Gravity.CENTER)
            // 中央から、利用可能な画面の高さの15%だけ上へ移動
            attributes = attributes.apply {
                y = -(availableHeight * 0.05f).toInt()
            }
        }
    }

    fun Button_thumbMoviePlay(view: View) {
        USERDATA.thumbMoviePlay = !USERDATA.thumbMoviePlay
        // println(USERDATA.thumbMoviePlay)
        if( USERDATA.thumbMoviePlay ){
            ThumbMovieStart(indexCoverFlow)
            binding.buttonThumbMoviePlay.alpha = 1.0F
            binding.buttonThumbMoviePlay.setBackgroundResource(R.drawable.video_on)
        }else {
            binding.buttonThumbMoviePlay.alpha = 0.2F
            binding.buttonThumbMoviePlay.setBackgroundResource(R.drawable.video_off)
            ThumbMovieStop()
        }
    }
    fun ThumbMoviePlay(forceType : ForceInResultType = ForceInResultType.none){
        SelectorDiagnostics.event { "THUMB_PLAY instance=${System.identityHashCode(this)} force=$forceType index=$indexCoverFlow music=${currentMusics.getOrNull(indexCoverFlow)?.sqlID}" }
        if( forceType == ForceInResultType.none && !USERDATA.thumbMoviePlay ){
            return
        }
        var typeLoadOnly = false
        if( !(forceType == ForceInResultType.none) ){
            volumeForceInResult = ReducedVolumeForceInResult
            if( forceType == ForceInResultType.loadOnly ){
                typeLoadOnly = true
            }
        }
        //ThumbMovie呼び込み
        // 一度すべて止める
        if( forceType == ForceInResultType.none ) {
            ThumbMovieStop()
        }
        // 裏側で再生
        ThumbMovieStart(indexCoverFlow, typeLoadOnly)
    }
    fun ThumbMovieStop(){
        SelectorDiagnostics.event { "THUMB_STOP instance=${System.identityHashCode(this)} index=$indexCoverFlow caller=${Throwable().stackTrace.drop(1).take(3).joinToString()}" }
        thumbGeneration++
        clearThumbCallbacks()
        //前に出していたらひっこめる
        setThumbMovieMaeUsiro(usiro = true)
        flg_thumbMovieStopping = true
        //一度すべて止める
        for( avPlayerVC in CachedThumbMovies.cachedMovies ){
            avPlayerVC.simpleExoPlayer.playWhenReady = false
            binding.playerViewAlpha.alpha = 1.0f
        }
    }
    fun ThumbMovieStart(index: Int, loadOnly: Boolean = false) {
        if (isFinishing || isDestroyed) return
        val selectedMusic = currentMusics.getOrNull(index) ?: return
        val smNum = MediaInput.watchId(selectedMusic.movieURL)
        if (smNum == null) {
            if (!loadOnly) ThumbMovieStop()
            return
        }
        if (!loadOnly) {
            ThumbMovieStop()
            flg_thumbMovieStopping = false
            flg_thumbMovieTopMost = false
        }
        val generation = thumbGeneration
        selectorHandler.postDelayed({
            if (!ownsThumbRequest(generation)) return@postDelayed
            if (!USERDATA.thumbMoviePlay && volumeForceInResult == 1.0f) return@postDelayed
            if (!loadOnly && index != indexCoverFlow) return@postDelayed
            var completed = false
            val request = MovieAccess().StreamingUrlNicoAccessForThumbMovie(smNum, selectorScope) { url ->
                selectorHandler.post callback@{
                    if (completed) return@callback
                    completed = true
                    if (!ownsThumbRequest(generation) ||
                        (loadOnly && url == "cached") ||
                        currentMusics.getOrNull(index) !== selectedMusic) return@callback
                    if (url.isBlank() || url == "error") {
                        if (!loadOnly && index == indexCoverFlow) ThumbMovieStop()
                        return@callback
                    }
                    if (!loadOnly && (flg_thumbMovieStopping || index != indexCoverFlow)) return@callback
                    val player = CachedThumbMovies.access(url, smNum)
                    player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    val firstNote = musicDatas.getNotesFirstTime(selectedMusic.movieURL)
                    val start = if (firstNote < 0.0) 30.0 else (firstNote - 3.0).coerceAtLeast(0.0)
                    fun current(): Boolean = ownsThumbRequest(generation) &&
                        cacheExoPlayer === player && !flg_thumbMovieStopping
                    fun message(seconds: Double, repeat: Boolean, action: () -> Unit) {
                        val msg = player.createMessage { _, _ ->
                            if (ownsThumbRequest(generation)) action()
                        }.setHandler(selectorHandler)
                            .setPosition((seconds * 1000).toLong())
                            .setDeleteAfterDelivery(!repeat)
                        thumbMessages.add(player to msg)
                        msg.send()
                    }
                    if (loadOnly) {
                        if (cacheExoPlayer === player && !flg_thumbMovieStopping) return@callback
                        message(start, false) {
                            if (cacheExoPlayer !== player || flg_thumbMovieStopping) {
                                CachedThumbMovies.cachedMovies.firstOrNull { it.simpleExoPlayer === player }?.just = true
                                player.playWhenReady = false
                            }
                        }
                        player.seekTo(((start - 0.5).coerceAtLeast(0.0) * 1000).toLong())
                        player.volume = 0.001f
                        player.playWhenReady = true
                        return@callback
                    }
                    cacheExoPlayer = player
                    binding.playerViewAlpha.alpha = 1.0f
                    playerView.player = player
                    val listener = object : Player.EventListener {
                        override fun onPlayerError(error: com.google.android.exoplayer2.ExoPlaybackException) {
                            SelectorDiagnostics.event { "THUMB_ERROR type=${error.type} cause=${error.cause?.javaClass?.name} current=${current()}" }
                            if (!current()) return
                            ThumbMovieStop()
                            playerView.player = null
                            cacheExoPlayer = null
                        }

                        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                            SelectorDiagnostics.event { "THUMB_STATE ready=$playWhenReady state=$playbackState current=${current()}" }
                            if (current() && playbackState == ExoPlayer.STATE_ENDED) {
                                player.seekTo(if (flg_thumbMovieTopMost) 0 else (start * 1000).toLong())
                                player.playWhenReady = true
                            }
                        }
                    }
                    thumbListeners[player] = listener
                    player.addListener(listener)
                    var prefetched = false
                    fun fade(amount: Int) {
                        if (!current() || flg_thumbMovieTopMost) return
                        if (volumeForceInResult > ReducedVolumeForceInResult) volumeForceInResult = 1.0f
                        if (volumeForceInResult >= ReducedVolumeForceInResult) {
                            player.volume = 0.10f * amount / 10 * USERDATA.SoundVolumeMovie * volumeForceInResult
                            binding.playerViewAlpha.alpha = 1.0f - 0.25f * amount / 10
                        }
                    }
                    for (i in 1..10) {
                        message(start + 0.05 * i, true) {
                            fade(i)
                            if (current() && !flg_thumbMovieTopMost && i == 1 && !prefetched) {
                                prefetched = true
                                val next = if (index + 1 >= currentMusics.size) 0 else index + 1
                                val previous = if (index == 0) currentMusics.lastIndex else index - 1
                                if (next != index) ThumbMovieStart(next, true)
                                if (previous != index && previous != next) ThumbMovieStart(previous, true)
                            }
                        }
                        message(start + 29.0 + 0.1 * i, true) { fade(10 - i) }
                    }
                    message(start + 30.1, true) {
                        if (current() && !flg_thumbMovieTopMost) player.seekTo((start * 1000).toLong())
                    }
                    val cached = CachedThumbMovies.cachedMovies.firstOrNull { it.simpleExoPlayer === player }
                    val nonSeek = cached?.just == true
                    cached?.just = false
                    if (!nonSeek) player.seekTo((start * 1000).toLong())
                    player.volume = 0.001f
                    player.playWhenReady = true
                }
            }
            thumbRequests.add(request)
            request.invokeOnCompletion {
                selectorHandler.post { thumbRequests.remove(request) }
            }
        }, 300)
    }


    private fun showNameRegistrationPromptIfNoName() {
        if (USERDATA.lookedNameRegistrationIfNoName_v2000) return
        if (USERDATA.Score.scores.count() < 10) return
        USERDATA.lookedNameRegistrationIfNoName_v2000 = true
        if (USERDATA.UserName != "NO_NAME") return
        val input = EditText(this).apply { setText(USERDATA.UserName); hint = "プレイヤー名"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("プレイヤー名を付けてみませんか？")
            .setMessage("名前はあとから設定でも変更できます").setView(input)
            .setPositiveButton("OK") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty() && name != USERDATA.UserName) {
                    reminderNameRequestPending = true
                    registerUserNameBlocking(name) { success ->
                        reminderNameRequestPending = false
                        if (!isFinishing) AlertDialog.Builder(this).setMessage(if (success) "名前を登録しました" else "登録に失敗しました。設定から再度お試しください").setPositiveButton("OK", null).show()
                    }
                }
            }.setNegativeButton("キャンセル", null).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isFinishing || isDestroyed) return
        if (GLOBAL.SuggestPlayerName) {
            GLOBAL.SuggestPlayerName = false
            window.decorView.post { if (!isFinishing) showNameRegistrationPromptIfNoName() }
        }
        segueing = false
        // startActivityForResult()の際に指定した識別コードとの比較
        when( requestCode ){
            1001 -> { //gameViewから帰還
                userScore = USERDATA.Score
                setCurrentLevels(indexCoverFlow)
                if (!ShowHowToExtendView()) {
                    //ユーザーネームデータをロードするため遷移させないようにする
                    segueing = true
                    binding.progressCircular.isVisible = true
                    ServerDataHandler().Chance_DownloadUserNameData_FirstData {
                        if (isFinishing || isDestroyed) return@Chance_DownloadUserNameData_FirstData
                        segueing = false
                        binding.progressCircular.isVisible = false
                    }
                }
                if( USERDATA.thumbMoviePlay ){
                    //ForcePlayして音量がちょい下がっていたら、徐々上げの処理を指示する必要がある
                    volumeForceInResult += 0.01f
                    if( volumeForceInResult >= 1.0f ){
                        volumeForceInResult = 1.0f
                    }
                    if( volumeForceInResult != 1.0f ){
                        //ResultViewでThumbMovieをForcePlayして戻ってきたとき（ 0.51f になっている ）[徐々上げ指示]
                        val fadeGeneration = thumbGeneration
                        val fadePlayer = cacheExoPlayer
                        selectorScope.launch {
                            while (ownsThumbRequest(fadeGeneration) && cacheExoPlayer === fadePlayer) {
                                if (volumeForceInResult >= 1.0f) {
                                    volumeForceInResult = 1.0f
                                    break
                                }
                                volumeForceInResult += (1.0f - ReducedVolumeForceInResult) / 15 //1.5秒で戻す
                                if(volumeForceInResult > 1.0f){ volumeForceInResult = 1.0f }
                                fadePlayer?.volume = 0.10f * USERDATA.SoundVolumeMovie * volumeForceInResult
                                delay(100)
                            }
                        }
                    }else {
                        //GameMenu->Back等で戻ってきたとき（ 1.0f になっている ）
                        ThumbMoviePlay()
                    }
                }else {
                    //ResultでForcePlayしていて音量がちょい下がっていたら(そのままフェードアウトしたいので)、徐々下げの処理を指示する必要がある
                    volumeForceInResult -= 0.01f
                    if( volumeForceInResult >= ReducedVolumeForceInResult ){
                        //GameMenu->Back等で戻ってきたとき（ 0.99f になっている ）-> 何もしない
                        volumeForceInResult = 1.0f
                    }else {
                        //ResultViewでThumbMovieをForcePlayして戻ってきたとき（ 0.49f になっている ）[徐々下げ指示]
                        binding.playerViewAlpha.alpha = 1.0f //音を小さくしていくだけなので動画は隠しておく
                        val fadeGeneration = thumbGeneration
                        val fadePlayer = cacheExoPlayer
                        selectorScope.launch {
                            while (ownsThumbRequest(fadeGeneration) && cacheExoPlayer === fadePlayer) {
                                if (volumeForceInResult <= 0.0f) {
                                    ThumbMovieStop()
                                    volumeForceInResult = 1.0f
                                    break
                                }
                                volumeForceInResult -= ReducedVolumeForceInResult / 10 //1.0秒で戻す
                                if(volumeForceInResult < 0.0f){ volumeForceInResult = 0.0f }
                                fadePlayer?.volume = 0.10f * USERDATA.SoundVolumeMovie * volumeForceInResult
                                delay(100)
                            }
                        }
                    }
                }
            }
            1002 -> {
                if (resultCode == RESULT_OK) { indexCoverFlow = -1; SetMusicToCoverFlow(); return }
                val currentMusicTags = currentMusics.getOrNull(indexCoverFlow)?.tags.orEmpty()
                if (maeTags != USERDATA.SelectedMusicCondition.tags || maeMusicTags != currentMusicTags || maeSort != USERDATA.SelectedMusicCondition.sortItem) {
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
                binding.progressCircular.isVisible = true
                ServerDataHandler().Chance_DownloadUserNameData_FirstData {
                        if (isFinishing || isDestroyed) return@Chance_DownloadUserNameData_FirstData
                    segueing = false
                    binding.progressCircular.isVisible = false
                }
            }
            1005 -> {
                val modoriStr = data?.getStringExtra("modori") ?: ""
                // println("url = $modoriStr")
                if (modoriStr != "") {
                    val retStr = URLDecoder.decode(modoriStr, "UTF-8")
                    // println(retStr)
                    var retTag = retStr.pregMatche_firstString("tag=(.*?)(&sort=|$)")
                    val retSort = retStr.pregMatche_firstString("sort=(.*?)(&tag=|$)")

                    var jikanSitei = ""

                    val array = retTag.split(" ")
                    if( array.any { it.startsWith("@t:") } ) {
                        array.firstOrNull { it.startsWith("@t:") }?.let {
                            jikanSitei = it
                        }
                        //時間指定が機能するように一度削除して最後にもっていく
                        retTag = retTag.pregReplace("\\s?@t:(\\d+:\\d+)?-?(\\d+:\\d+)?", "")
                    }
                    retTag = retTag.trim()
                    retTag = retTag.pregReplace("\\s*/(and|AND)/\\s", "/and/")
                    retTag = retTag.pregReplace("\\s+", " or ")
                    retTag = retTag.pregReplace("/and/", " ")
                    if( retTag != "" && jikanSitei != "" ){
                        jikanSitei = " " + jikanSitei
                    }
                    USERDATA.SelectedMusicCondition.tags = retTag + jikanSitei
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
    fun LevelPickerContainerRedraw(redraw: Boolean = true) {
        levelPickerBitmapDirty = true
        if ((levelScroller_index - 1) in currentLevels.indices) {
            setLevelPickerContainer(
                currentLevels[levelScroller_index - 1],
                binding.starM1,
                binding.creatorM1,
                binding.scoreM1,
                binding.rankM1,
                binding.dateM1, binding.calcLevelM1
            )
        }else {
            setLevelPickerContainer(null, binding.starM1, binding.creatorM1, binding.scoreM1, binding.rankM1, binding.dateM1, binding.calcLevelM1)
        }
        if (levelScroller_index in currentLevels.indices){
            setLevelPickerContainer(currentLevels[levelScroller_index], binding.star, binding.creator, binding.score, binding.rank, binding.date, binding.calcLevel)
            UpdateObject_Level(currentLevels[levelScroller_index])
        }else {
            setLevelPickerContainer(null, binding.star, binding.creator, binding.score, binding.rank, binding.date, binding.calcLevel)
            UpdateObject_Level(null)
        }
        if ((levelScroller_index + 1) in currentLevels.indices){
            setLevelPickerContainer(
                currentLevels[levelScroller_index + 1],
                binding.starP1,
                binding.creatorP1,
                binding.scoreP1,
                binding.rankP1,
                binding.dateP1, binding.calcLevelP1
            )
        }else {
            setLevelPickerContainer(null, binding.starP1, binding.creatorP1, binding.scoreP1, binding.rankP1, binding.dateP1, binding.calcLevelP1)
        }
        setLevelPickerContainer(currentLevels.getOrNull(levelScroller_index + 2),
            binding.starP2, binding.creatorP2, binding.scoreP2, binding.rankP2, binding.dateP2, binding.calcLevelP2)
        if (redraw) RedrawLevelpickerSelection()
    }
    fun UpdateObject_Level(currentLevel: levelData?){
        // Restore the base heart as well as rating overlays after an empty selection.
        binding.heartMono.isVisible = currentLevel != null
        if( currentLevel == null ){
            binding.textSpeed.setText("")
            binding.heartColor.isVisible = false
            binding.heartBlack.isVisible = false
            binding.textLikeNum.isVisible = false
            return
        }
        currentLevel.let {
            val currentLevel = it
            //スピード反映
            binding.textSpeed.setText("speed: " + currentLevel.speed)
            //like反映
            val rating = USERDATA.MyLike[currentLevel.sqlID] ?: 0
            binding.heartColor.setImageResource(if (rating == -2) R.drawable.heart_edge else R.drawable.heart_color)
            binding.heartColor.isVisible = rating > 0 || rating == -2
            binding.heartBlack.isVisible = rating == -1
            val fc = ((currentLevel.likeCount - currentLevel.dislikeCount) / 5) * 5
            binding.textLikeNum.text = fc.toString()
            binding.textLikeNum.setTextColor(if (fc < 0) Color.RED else Color.BLACK)
            binding.textLikeNum.isVisible = fc != 0
            val nowTime = (Date().time / 1000).toInt()
            val sDateFormat = SimpleDateFormat("yyy.MM.dd")
            if( (USERDATA.SelectedMusicCondition.sortItem == "最近ハイスコアが更新された曲順" || (nowTime - currentLevel.scoreTime)<2592000 ) && currentLevel.scoreTime > 0 ){
                binding.textRankingTime.text = sDateFormat.format(Date(currentLevel.scoreTime * 1000L))
                binding.textRankingTime.isVisible = true
            }else{
                binding.textRankingTime.isVisible = false
            }
            if( (USERDATA.SelectedMusicCondition.sortItem == "最近コメントされた曲順" || (nowTime - currentLevel.commentTime)<2592000 ) && currentLevel.commentTime > 0 ){
                binding.textCommentTime.text = sDateFormat.format(Date(currentLevel.commentTime * 1000L))
                binding.textCommentTime.isVisible = true
            }else{
                binding.textCommentTime.isVisible = false
            }

        }
    }
    fun setLevelPickerContainer(
        leveldata: levelData?,
        _star: TextView,
        _creator: TextView,
        _score: TextView,
        _rank: TextView,
        _date: TextView,
        _calcLevel: TextView
    ){
        // ★10個の右端を基準にし、FULLでも同じ横位置に表示する。
        val showCalculatedLevel = USERDATA.UseNewLevelCalculationKey &&
            leveldata != null && leveldata.calcLv > 0
        _calcLevel.isVisible = showCalculatedLevel
        _calcLevel.text = if (showCalculatedLevel) "★${leveldata!!.calcLv}" else ""
        if (showCalculatedLevel) {
            _calcLevel.translationX = (_star.paint.measureText("★★★★★★★★★★") +
                _calcLevel.paint.measureText(_calcLevel.text.toString())) / 2f +
                4f * resources.displayMetrics.density
        }
        _star.isVisible = leveldata != null
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
                // println(scoredata)
                _score.setText("HighScore: " + scoredata[UserScore.SCORE])
                var rankStr = Score.RankStr[scoredata[UserScore.RANK]]
                if( rankStr == "False" ){ rankStr = "" }
                if( rankStr == "PERFECT" ){
                    var htmlText = "<font color=\"#FF0000\"><small><small>PERFECT</small></small></font>"
                    _rank.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT))
                }else {
                    _rank.setText(rankStr)
                }
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
        val view = binding.levelpickerContainer
        // GONEから戻した星を測定し、通常の配置のまま子のレイアウトを更新する。
        if (view.isLayoutRequested && view.width > 0 && view.height > 0) {
            levelPickerBitmapDirty = true
            view.measure(View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY))
            view.layout(view.left, view.top, view.right, view.bottom)
        }
        val realScrollY = binding.pickerRowCenter.height * scrollY / scrollerOneHeight
        // 親の再レイアウトでも移動量を維持し、文字更新時の一瞬の位置戻りを防ぐ。
        view.translationY = -realScrollY.toFloat()
        RedrawLevelpickerSelection()
    }
    fun RedrawLevelpickerSelection(){
        val view = binding.levelpickerContainer
        val output = binding.outputImageViewLevelScroller
        if (view.width <= 0 || view.height <= 0 || output.width <= 0 || output.height <= 0) {
            return
        }
        // 内容変更直後のTextViewも、最新のレイアウトで描画する。
        if (view.isLayoutRequested) {
            levelPickerBitmapDirty = true
            view.measure(View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY))
            view.layout(view.left, view.top, view.right, view.bottom)
        }
        // TextViewを狭いCanvasで描画すると、そのクリップ範囲が文字描画にも影響する。
        // 全体を描いた画像を再利用し、スクロール中は画像だけを移動・切り取りする。
        val source = levelPickerBitmap?.takeIf {
            it.width == view.width && it.height == view.height
        } ?: Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also {
            levelPickerBitmap?.recycle()
            levelPickerBitmap = it
            levelPickerBitmapDirty = true
        }
        if (levelPickerBitmapDirty) {
            source.eraseColor(Color.TRANSPARENT)
            view.draw(Canvas(source))
            levelPickerBitmapDirty = false
        }
        val cropTop = output.height * 3 / 4
        val bitmap = Bitmap.createBitmap(output.width, output.height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawBitmap(source, 0F, view.y - cropTop, null)
        output.setImageBitmap(bitmap)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(flg_notHome == false) {
            // println("Leave!")
            leaved = true
            //Thumb止め
            ThumbMovieStop()
        }
    }
    private var reminderNameRequestPending = false
    private var reminderResumed = false
    private var reminderIdleSince = 0L
    private var flickReminderDialog: android.app.Dialog? = null
    private val flickReminderCheck = object : Runnable {
        override fun run() {
            if (!reminderResumed || isFinishing || isDestroyed) return
            if (FlickOnlyReminder.pending && flickReminderDialog == null) {
                val idle = !segueing && !coverFlowScrolling && !reminderNameRequestPending &&
                    !binding.progressCircular.isVisible && window.decorView.hasWindowFocus()
                val now = android.os.SystemClock.uptimeMillis()
                if (!idle) reminderIdleSince = 0L
                else if (reminderIdleSince == 0L) reminderIdleSince = now
                else if (now - reminderIdleSince >= 1000L) showFlickOnlyReminder()
            } else reminderIdleSince = 0L
            if (FlickOnlyReminder.pending) selectorHandler.postDelayed(this, 500)
        }
    }

    private fun showFlickOnlyReminder() {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(20), dp(20), dp(12))
            setBackgroundColor(Color.argb(220, 0, 0, 0))
        }
        panel.addView(TextView(this).apply {
            text = "キーボード設定を確認してみませんか？\n\n同じ文字を続けて入力したときの結果から、「フリックのみ」がOFFになっている可能性があります。\nすでにONの場合は、そのまま閉じてください。"
            setTextColor(Color.WHITE)
            textSize = 16f
        }, LinearLayout.LayoutParams(-1, -2))
        panel.addView(android.widget.ImageView(this).apply {
            setImageResource(R.drawable.howto2a)
            adjustViewBounds = true
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            contentDescription = "キーボードを「フリックのみ」に設定する手順"
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        panel.addView(TextView(this).apply {
            text = "閉じる"
            setTextColor(Color.rgb(220, 235, 255))
            textSize = 17f
            gravity = Gravity.CENTER
            minHeight = dp(48)
            setOnClickListener { dialog.dismiss() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        dialog.setContentView(android.widget.ScrollView(this).apply { addView(panel) })
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener { flickReminderDialog = null; reminderIdleSince = 0L }
        flickReminderDialog = dialog
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { dimAmount = 0.55f }
            setLayout((resources.displayMetrics.widthPixels * 0.94f).toInt(),
                (resources.displayMetrics.heightPixels * 0.9f).toInt())
        }
        FlickOnlyReminder.markShown()
    }

    override fun onResume() {
        super.onResume()
        navigation.resume()
        reminderResumed = true
        reminderIdleSince = 0L
        selectorHandler.post(flickReminderCheck)
        LevelPickerContainerRedraw()
        // println("Resume!")
        if( leaved && USERDATA.thumbMoviePlay ){
            //Thumb開始
            //ThumbMoviePlay()
        }
        leaved = false
        flg_notHome = false
    }

    override fun onPause() {
        reminderResumed = false
        reminderIdleSince = 0L
        selectorHandler.removeCallbacks(flickReminderCheck)
        navigation.pause()
        super.onPause()
    }

    override fun onDestroy() {
        flickReminderDialog?.dismiss()
        flickReminderDialog = null
        thumbGeneration++
        clearThumbCallbacks()
        selectorHandler.removeCallbacksAndMessages(null)
        selectorScope.cancel()
        scrollingTimer?.cancel()
        scrollingTimer = null
        cacheExoPlayer?.playWhenReady = false
        binding.playerView.player = null
        binding.playerViewOnCoverFlow.player = null
        cacheExoPlayer = null
        if (GLOBAL.Selector_instance === this) {
            GLOBAL.Selector_instance = null
            CachedThumbMovies.allRelease()
        }
        navigation.destroy()
        super.onDestroy()
    }

    fun PlayerView_Alpha_Huta(){
        binding.playerViewAlpha.alpha = 1.0f
    }

}