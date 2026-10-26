package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivityGameViewBinding

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.MotionEvent
import android.view.Choreographer
import android.widget.SeekBar
import android.graphics.Rect
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.postDelayed
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.Util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.math.abs


class Activity_GameView : SafeAreaActivity() {

    // プロセス内だけ保持し、アプリのキル後は再び準備する。
    companion object {
        private var keyboardPrepared = false
    }
    private var waitingForKeyboard = !keyboardPrepared
    private var keyboardPreparationOverlay: FrameLayout? = null
    private var keyboardPreparationMessage: android.widget.TextView? = null

    private lateinit var binding: ActivityGameViewBinding
    private val movieScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.Main)
    private var movieLoadJob: kotlinx.coroutines.Job? = null


    //各種データ
    var musicDatas: MusicDataLists = MusicDataLists
    var scoreDatas: ScoreDataLists = ScoreDataLists
    var commentDatas: CommentDataLists = CommentDataLists

    lateinit var selectMusic: musicData
    lateinit var selectLevel: levelData

    var greatActionCount = 0
    var goodActionCount = 0
    var safeActionCount = 0
    var badActionCount = 0
    var missActionCount = 0
    val flickAnimatorSet: AnimatorSet? = null
    var animationStartYsa = 0.0f
    var animationEndYsa = 0.0f
    var comboanimeStartYsa = 0.0f
    var comboanimeEndYsa = 0.0f


    //ゲーム　表示、判定　定数データ
    var gameviewWidth = 1.0 // あとで実際の画面サイズに変更
    var flickPointX = 50.0 // あとで比率で計算して変更
    var drawRange = arrayOf(-200,-100, -50, 400)
    val greatLine = listOf(-0.080, 0.080)
    val goodLine = listOf(-0.200, 0.200)
    val safeLine = listOf(-0.400, 0.300)
    var judgeOffset = 0.0
    var xps = 0.0
    var gameviewHeight = 667.0 // あとで実際の画面サイズに変更
    var playerviewHeight = 0
    var playerviewOriginalHeight = 0

    var t03:Double = 3.0
    var time3secMediaTime:Double? = null
    var saveData_time3sec:Double? = null

    //効果音
    val seAudio = SEAudio
    //ゲームデータ
    val noteData = Notes()

    //private lateinit var simpleExoPlayer: SimpleExoPlayer
    private var cacheExoPlayer: SimpleExoPlayer? = null
    private val playerView: PlayerView get() = binding.playerView

    var paused = false
    private var gameViewResumed = false
    private var gameMenuOpen = false
    private var initialPlaybackReady = false
    private var countdownPausedAt: Double? = null
    private val startPlayback = Runnable {
        // 動画とノーツの準備が整うまで、先行ノーツの時計を開始しない。
        if (waitingForKeyboard || !gameViewResumed || initialPlaybackReady || !notesReady ||
            cacheExoPlayer?.playbackState != Player.STATE_READY ||
            !GamePlaying || timerKill || isFinishing || isDestroyed) return@Runnable
        initialPlaybackReady = true
        time3secMediaTime = if (t03 <= 0.0) null else android.os.SystemClock.elapsedRealtime() / 1000.0
        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        val shouldPlay = gameViewResumed && !gameMenuOpen &&
            GamePlaying && !timerKill && !isFinishing && !isDestroyed &&
            initialPlaybackReady && !waitingForKeyboard && !practiceSeeking && pendingSeekSeconds == null
        // 開始前のカウントダウンも、メニューやバックグラウンド中は進めない。
        val now = android.os.SystemClock.elapsedRealtime() / 1000.0
        if (time3secMediaTime == null) {
            countdownPausedAt = null
        } else if (!shouldPlay) {
            if (countdownPausedAt == null) countdownPausedAt = now
        } else {
            countdownPausedAt?.let { time3secMediaTime = time3secMediaTime!! + now - it }
            countdownPausedAt = null
        }
        cacheExoPlayer?.playWhenReady = shouldPlay
    }
    var resultSegued = false //遷移中フラグ

    var GamePlaying = true

    var myKeybd:MyKeyboard? = null
    var useMyKeybd = USERDATA.UseNicoFlickKeyboard
    var borderPosUe = USERDATA.BorderPosUe

    val mHandler = Handler()
    var timerKill = false
    var practiceMode = false
        private set(value) {
            field = value
            if (value) noteData.score.markNonStandardConditions()
        }
    private var practiceSeeking = false
    private var waitingForFlickAfterSeek = false
    private var nextMissCheckIndex = 0
    private var nextAutoPlayIndex = 0
    private var practiceRate = 1.0f
    private var notesReady = false
    private var hasPassedFirstNote = false
    private var pendingSeekSeconds:Double? = null
    private var frameScheduled = false
    private val choreographer by lazy { Choreographer.getInstance() }
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos:Long) {
            frameScheduled = false
            if (timerKill || !notesReady || !GamePlaying) return
            timerRun.run()
            startFrames()
        }
    }
    private fun startFrames() {
        if (!frameScheduled && !timerKill && notesReady && GamePlaying) {
            frameScheduled = true
            choreographer.postFrameCallback(frameCallback)
        }
    }
    private fun stopFrames() {
        choreographer.removeFrameCallback(frameCallback)
        frameScheduled = false
    }
    private var playbackListener:Player.EventListener? = null

    private var run: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectMusic = GLOBAL.SelectMUSIC ?: return returnToStartAfterStateLoss()
        selectLevel = GLOBAL.SelectLEVEL ?: return returnToStartAfterStateLoss()
        configurePracticeControls()
        binding.textViewJudgeOffset.isVisible = USERDATA.showOffsetLabel
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
            updateMovieLayout()
            updateKeyboardPreparationLayout()
        }

        // 初回描画前に実測座標で前回の高さを適用する。
        val previousKeyboardTop = USERDATA.maeKeyboardTop
        if (USERDATA.fitsMovieToKeyboardHeight && previousKeyboardTop != 0) {
            binding.root.viewTreeObserver.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                    val location = IntArray(2); binding.gameMovieFrame.getLocationOnScreen(location)
                    val progressSpace = binding.progressBarLoaded.height
                    val available = (previousKeyboardTop - location[1] - progressSpace).coerceAtLeast(1)
                    if (binding.gameMovieFrame.layoutParams.height == available) return true
                    binding.gameMovieFrame.layoutParams.height = available
                    binding.gameMovieFrame.requestLayout()
                    // 高さを反映したレイアウトが終わるまで初回描画を待つ。
                    return false
                }
            })
        }

        //Selectorからデータ受け取り
        // 必須データはUIや非同期処理の初期化前に検証済み。

        val timetag = selectLevel.noteData.pregMatche_firstString("^(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])")
        if( timetag != "" ){
            t03 = 3.0 - timetag.timetagToSeconds()
            if( t03 < 0.0 ){ t03 = 0.0 }
        }
        //simpleExoPlayer = SimpleExoPlayer.Builder(applicationContext)
        //    .build()


        //簡易キーボード
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val viewHeight = point.y.toDouble()
        myKeybd = MyKeyboard(this)
        addContentView( myKeybd, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) )
        myKeybd?.onKeylistener = {
            //println("word = ${it}")
            FlickInput(it)
        }
        myKeybd?.setPaddingKeybd(top = (viewHeight / 4).toInt() * (10000 - USERDATA.NicoFlickKeyboardHeightPer) / 10000, bottom = 0)
        myKeybd?.isHidden = !useMyKeybd
        binding.editText.isEnabled = !useMyKeybd

        binding.editText.captureInputTime = { captureFlickTime() }
        binding.editText.onFlickInput = { word, inputTime ->
            if (!useMyKeybd) FlickInput(word, inputTime)
        }

        ShowHideBorder(borderPosUe)
        binding.progressBar.alpha = 1.0f
        binding.progressBar2.alpha = 1.0f

        if(USERDATA.lookedHelp == false){
            USERDATA.lookedHelp = true
            Handler().postDelayed(Runnable {
                val intent: Intent = Intent(applicationContext, Activity_HowToGame::class.java)
                startActivityForResult(intent,1003)
            }, 500)
        }else {
            onCreate2()
        }
    }
    private fun showKeyboardPreparation() {
        if (!waitingForKeyboard || keyboardPreparationOverlay != null) return
        val overlay = FrameLayout(this)
        overlay.setBackgroundColor(0x99000000.toInt())
        val message = android.widget.TextView(this).apply {
            text = "キーボードの準備をします\n好きな文字を一度フリックしてください"
            isInvisible = true
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            setPadding(PX(16f), 0, PX(16f), 0)
        }
        overlay.addView(message, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        // ゲーム側のボタンは遮り、簡易キーボードには入力を通す。
        overlay.setOnTouchListener { _, event -> event.y < message.height }
        keyboardPreparationOverlay = overlay
        keyboardPreparationMessage = message
        addContentView(overlay, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        overlay.post { updateKeyboardPreparationLayout() }
    }

    private fun updateKeyboardPreparationLayout() {
        val overlay = keyboardPreparationOverlay ?: return
        val message = keyboardPreparationMessage ?: return
        val location = IntArray(2)
        overlay.getLocationOnScreen(location)
        val keyboardTop = if (useMyKeybd) {
            val keyboard = myKeybd ?: return
            if (!keyboard.isShown || keyboard.height == 0) {
                message.isInvisible = true
                return
            }
            keyboard.keyboardTopOnScreen()
        } else {
            val decor = window.decorView
            val visible = Rect()
            decor.getWindowVisibleDisplayFrame(visible)
            val decorLocation = IntArray(2)
            decor.getLocationOnScreen(decorLocation)
            val hiddenHeight = decorLocation[1] + decor.height - visible.bottom
            if (hiddenHeight <= GLOBAL.NavigationBarHeight + PX(100f)) {
                message.isInvisible = true
                return
            }
            visible.bottom
        }
        if (overlay.height == 0) return
        val available = (keyboardTop - location[1]).coerceIn(1, overlay.height)
        if (message.layoutParams.height != available) {
            message.isInvisible = true
            message.layoutParams.height = available
            message.requestLayout()
        } else {
            // キーボード検知後、指定した高さのレイアウトが完了してから表示する。
            message.isInvisible = message.height != available
        }
    }

    fun onCreate2() {
        showKeyboardPreparation()
        //動画 No.
        val smNum = MediaInput.watchId(selectMusic.movieURL)
        if (smNum == null) {
            android.widget.Toast.makeText(this, "動画IDを読み取れませんでした。選曲画面に戻ります。", android.widget.Toast.LENGTH_LONG).show()
            finish()
            return
        }

        //Movie呼び込み
        var nicodougaURL = ""
        movieLoadJob?.cancel()
        movieLoadJob = MovieAccess().StreamingUrlNicoAccess(smNum, movieScope) {
            // 動画
            if (isFinishing || isDestroyed) return@StreamingUrlNicoAccess
            if (it.isBlank() || it == "error") {
                android.widget.Toast.makeText(this, "動画を取得できませんでした。通信状態や動画の公開状態をご確認ください。", android.widget.Toast.LENGTH_LONG).show()
                finish()
                return@StreamingUrlNicoAccess
            }
            nicodougaURL = it
            MovieLoadLog.log("game/$smNum") { "URL_CALLBACK cached=${it == "cached"} success=${it.isNotBlank() && it != "error"}" }
            //println(applicationContext.packageName)
            setExo(nicodougaURL,smNum)
        }

        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        gameviewWidth = point.x.toDouble()
        gameviewHeight = point.y.toDouble()
        // println("xy = $gameviewWidth x $gameviewHeight")
        flickPointX = gameviewWidth * 50 / 375
        noteData.gameviewWidth = gameviewWidth
        noteData.flickPointX = flickPointX
        //計算しておく
        xps = (gameviewWidth - flickPointX) * Double(selectLevel.speed) / 300 //ノートが一秒間に進む距離
        //
        drawRange[0] = iP2An(drawRange[0]) //-200
        drawRange[1] = iP2An(drawRange[1]) //-100
        drawRange[2] = iP2An(drawRange[2]) // -50
        drawRange[3] = iP2An(drawRange[3]) // 400

        run = Runnable {
            if (playerView.height == 0) {
                // println("playerView.height=" + playerView.height)
                mHandler.postDelayed(run!!, 10)
                return@Runnable
            }
            //まずプレイヤービューのサイズを指定比率で端末に合わせる
            if( (playerView.width * 270 / 375) < (gameviewHeight * 0.4) ) {
                playerviewOriginalHeight = playerView.width * 270 / 375
                // println("270")
            }else {
                playerviewOriginalHeight = (gameviewHeight * 0.4).toInt()
                // println("0.4")
            }
            playerviewHeight = playerviewOriginalHeight * USERDATA.PlayerHeightPer / 10000
            if (!USERDATA.fitsMovieToKeyboardHeight) {
                binding.gameMovieFrame.layoutParams.height = playerviewHeight
            }

            // FrameLayout のインスタンスを取得
            val layout: FrameLayout = findViewById(R.id.notesWindow)

            //フリックポイント
            binding.dotCircle.centerX = (flickPointX + binding.textViewBarWord.width / 2).toFloat()
            binding.dotCircle.isVisible = true
            //フリックアクション
            binding.flickCircle.centerX = binding.dotCircle.centerX
            //Great
            binding.textViewGreat1.centerX = binding.dotCircle.centerX
            //Good
            binding.textViewGood1.centerX = binding.dotCircle.centerX
            //Safe
            binding.textViewSafe1.centerX = binding.dotCircle.centerX
            //Bad
            binding.textViewBad1.centerX = binding.dotCircle.centerX
            //Miss
            binding.textViewMiss1.centerX = binding.dotCircle.centerX
            //
            binding.textViewCombo.centerX = binding.dotCircle.centerX
            //
            animationStartYsa = ( (binding.dotCircle.y + binding.textViewGreat1.y) / 2 ) - binding.dotCircle.y
            animationEndYsa = binding.textViewGreat1.y - binding.dotCircle.y

            comboanimeStartYsa = ( (binding.dotCircle.y + binding.textViewCombo.y) / 2 ) - binding.dotCircle.y
            comboanimeEndYsa = binding.textViewCombo.y - binding.dotCircle.y
//            comboanimeStartYsa2 = binding.noteBarView.height.toFloat() / 2//( (textView_combo.y + textView_Great1.y) / 2 ) - dotCircle.y
//            comboanimeEndYsa2 = binding.noteBarView.height.toFloat()* 5 / 4

            //ノートデータを解析、生成
            noteData.noteAnalyze(selectLevel.noteData, selectLevel.speed, selectLevel.level)
            //ノートラベルも最初にすべて作ってしまう（移動だとか処理をさせるのは見えるやつだけにする）
            //「フリックしない文字」のLabel作成 -> 「フリックする文字」のLabel作成 の順にして、フリック文字が前面に出るようにする。
            val rectF = RectF(
                binding.textViewBarWord.x - 100, //枠外
                binding.textViewBarWord.y,
                binding.textViewBarWord.x - 100 + binding.textViewBarWord.height,
                binding.textViewBarWord.y + binding.textViewBarWord.height
            )
            // それぞれのTextView に設定を行い、FrameLayout のインスタンスに追加
            for (note in noteData.notes) {
                if (note.isFlickable == false) {
                    //Label生成
                    val label = OutlineTextView(this, rectF)
                    label.setTextColor(Color.WHITE)
                    label.rectF = rectF
                    label.setTextSize(DP(binding.textViewBarWord.textSize.toInt()))
                    label.alpha = 0.75f
                    label.isInvisible = true
                    label.text = note.word
                    layout.addView(label)
                    //  labelを保持
                    note.flickedLabel = label
                    note.label = label
                    //  文字の見た目を変える（フリックしない文字）
                    //note.setUnFlickableFont()
                }
            }
            for (note in noteData.notes) {
                if (note.isFlickable == true) {
                    //まずフリックし終わった後のLabelを生成
                    var label = OutlineTextView(this, rectF)
                    label.setTextColor(Color.WHITE)
                    label.rectF = rectF
                    label.setTextSize(DP(binding.textViewBarWord.textSize.toInt()))
                    label.alpha = 0.75f
                    label.isInvisible = true
                    label.text = note.word
                    layout.addView(label)
                    //  labelを保持
                    note.flickedLabel = label

                    //次にフリックする前のLabelを生成
                    label = OutlineTextView(this, rectF)
                    label.setTextColor(Color.BLACK)
                    label.strokeWidth = 0.0f
                    label.rectF = rectF
                    label.setTextSize(DP(binding.textViewBarWord.textSize.toInt()))
                    label.alpha = 1.0f
                    label.isInvisible = true
                    label.text = note.word
                    layout.addView(label)
                    //  labelを保持
                    note.flickableLabel = label
                    note.label = label
                    //  文字の見た目を変える（フリックする文字）
                    note.setFlickableFont()
                }
            }
            noteData.noteReset()

            // playerViewの高さ変更を含むレイアウトが確定してから画像化する。
            binding.root.doOnPreDraw {
                if (!isFinishing && !isDestroyed) {
                    prepareEffectPools()
                    notesReady = true
                    startPlayback.run()
                    startFrames()
                }
            }
        }
        mHandler.post(run!!)

        //保存データの取得
        // スコア読み込み
        USERDATA.Score.scores[selectLevel.sqlID]?.let {
            binding.textViewHighScore.setText("High Score: " + String(it[0]) + " ")
        }
        binding.textViewScore.setText("Score: 0 ")
        // ジャッジオフセットの取得
        USERDATA.JudgeOffset[selectLevel.sqlID]?.let {
            judgeOffset = Double(it)
        }
        binding.textViewJudgeOffset.setText("offset: %.2f".format(judgeOffset))

        GlobalScope.launch(Dispatchers.Main){
        }

        if( !useMyKeybd ) {
            binding.editText.requestFocus()
        }

        //最下のボタン非表示
        val view = window.decorView
        view.systemUiVisibility = view.systemUiVisibility or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    }

    fun setExo(nicodougaURL:String,smNum:String) {

        //val loadControl = DefaultLoadControl.Builder().setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,10*60*1000,10*60*1000,0)
        //val simpleExoPlayer = SimpleExoPlayer.Builder(applicationContext).setLoadControl()
        //    .build()
        playbackListener?.let { cacheExoPlayer?.removeListener(it) }
        mHandler.removeCallbacks(startPlayback)
        initialPlaybackReady = false
        time3secMediaTime = null
        countdownPausedAt = null
        cacheExoPlayer = CachedMovies.access(nicodougaURL,smNum)
        cacheExoPlayer!!.playWhenReady = false
        // Cached players retain playback parameters from previous practice sessions.
        cacheExoPlayer!!.setPlaybackParameters(
            if (practiceMode) PlaybackParameters(practiceRate) else PlaybackParameters.DEFAULT
        )
        recordPlayConditions()

        playerView.apply {
            player = cacheExoPlayer
        }
        cacheExoPlayer!!.setSeekParameters(SeekParameters.EXACT)
        cacheExoPlayer!!.seekTo(0)
        val playbackPlayer = cacheExoPlayer!!
        playbackListener = object : Player.EventListener {
            override fun onPlayerError(error: com.google.android.exoplayer2.ExoPlaybackException) {
                if (cacheExoPlayer !== playbackPlayer || isFinishing || isDestroyed) return
                GamePlaying = false
                timerKill = true
                stopFrames()
                mHandler.removeCallbacks(startPlayback)
                playbackPlayer.playWhenReady = false
                android.widget.Toast.makeText(this@Activity_GameView, "動画を再生できませんでした。選曲画面に戻ります。",
                    android.widget.Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                noteData.score.recordPlayConditions(practiceMode,
                    playbackParameters.speed, playbackParameters.pitch)
            }
            override fun onSeekProcessed() {
                pendingSeekSeconds?.let { seconds ->
                    synchronizePracticeTimeline(seconds)
                    pendingSeekSeconds = null
                    updatePlaybackState()
                }
            }
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                // println("GamePlaying = ${GamePlaying}")
                if( !GamePlaying ){ return }
                // println("playbackState=" + playbackState)
                when (playbackState) {
                    Player.STATE_READY -> startPlayback.run()
                    ExoPlayer.STATE_ENDED -> {
                            // println("終了 -> リザルト画面へ")
                        GamePlaying = false
                        if(resultSegued==false){
                            resultSegued = true
                            val intent: Intent = Intent(applicationContext, Activity_Result::class.java)
                            intent.putExtra("hasPassedFirstNote", hasPassedFirstNote)
                            intent.putExtra("completedNormally", true)
                            //intent.putExtra("SelectMusicID", selectMusic.sqlID)
                            //intent.putExtra("SelectLevelID", selectLevel.sqlID)
                            recordPlayConditions()
                            GLOBAL.CurrentNOTES = noteData
                            GLOBAL.ResultPractice = practiceMode
                            GLOBAL.ResultFirst = true
                            startActivityForResult(intent, 1002)
                        }
                    }
                }
            }
        }
        cacheExoPlayer!!.addListener(playbackListener!!)
        // キャッシュ済みで既に READY の場合は状態変更通知を待たない。
        startPlayback.run()
    }

    // Capture at IME receipt (or custom-key callback), before text editing and effects.
    private fun captureFlickTime(): Double? {
        val player = cacheExoPlayer ?: return null
        if (waitingForKeyboard || !initialPlaybackReady || !gameViewResumed ||
            !notesReady || !GamePlaying || timerKill || practiceSeeking || pendingSeekSeconds != null) return null
        var time = player.currentPosition / 1000.0 - 0.05 + judgeOffset
        if (practiceMode) time -= 0.07 * (1.0 - practiceRate)
        return time
    }

    //フリック判定
    fun FlickInput(string: String, inputTime: Double? = captureFlickTime()) {
        if (waitingForKeyboard) {
            if (!gameViewResumed || keyboardPreparationOverlay == null ||
                string.isBlank() || string == FlickTextChange.INVALID_INPUT) return
            keyboardPrepared = true
            waitingForKeyboard = false
            keyboardPreparationOverlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
            keyboardPreparationOverlay = null
            keyboardPreparationMessage = null
            // IME操作のコールバックを抜けてから開始し、準備用入力は採点しない。
            mHandler.post(startPlayback)
            return
        }
        if (inputTime == null || !notesReady || !GamePlaying || timerKill ||
            practiceSeeking || pendingSeekSeconds != null) return
        val flickTime = inputTime
        //着目ノートを決定 _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        var flickedNote: Note? = null
        var minDiffTime = 1.0 //フリックとノートのズレ時間
        for (i in (noteData.lastFlickedNum until noteData.notes.size)) {
            val note = noteData.notes[i]
            if (note.isFlickable && note.flicked == false) {
                val diffTime = note.time - flickTime
                if (safeLine[0] <= diffTime && diffTime <= safeLine[1]) {
                    if (abs(minDiffTime) > abs(diffTime)) {
                        minDiffTime = diffTime
                        flickedNote = note
                        noteData.lastFlickedNum = i + 1
                    }
                }
                if (diffTime > 1.0) break
            }
        }
        if (flickedNote != null) judgeFlick(flickedNote, string, flickTime, minDiffTime)
        // judgeFlick already updates the score/combo once for a matched note.
        FlickAction()
        // Input buffering/idle cleanup belongs to FlickInputEditText, outside the judgment path.
        if (waitingForFlickAfterSeek) {
            waitingForFlickAfterSeek = false
            for (note in noteData.notes) {
                if (!note.isFlickable || note.flicked) { continue }
                if (note.time <= flickTime) {
                    note.flicked = true; note.setUnFlickableFont()
                }else {
                    break
                }
            }
            resetTimelineCursors()
        }
    }
    private fun judgeFlick(note:Note, word:String, time:Double, difference:Double) {
        recordPlayConditions()
        val wordJudge = note.judgeWord(word)
        val judge = when {
            time == -1.0 -> Note.MISS
            wordJudge == Note.BAD -> Note.BAD
            wordJudge == Note.SAFE -> Note.SAFE
            difference in greatLine[0]..greatLine[1] -> Note.GREAT
            difference in goodLine[0]..goodLine[1] -> Note.GOOD
            difference in safeLine[0]..safeLine[1] -> Note.SAFE
            else -> Note.BAD
        }
        if (practiceMode) noteData.score.subScore(note.judge, note.comboCount)
        note.flicked = true; note.flickedTime = time; note.judge = judge
        noteData.score.addScore(judge); note.comboCount = noteData.score.comboCounter
        hasPassedFirstNote = true
        if (judge != Note.MISS || selectLevel.level <= 10) note.setUnFlickableFont()
        note.label.y = binding.textViewBarWord.y
        binding.progressBar.progress = (noteData.score.borderScore * 10).toInt()
        binding.progressBar2.progress = binding.progressBar.progress
        when (judge) {
            Note.GREAT -> { GreatAction(); seAudio.okJinglePlay() }
            Note.GOOD -> { GoodAction(); seAudio.okJinglePlay() }
            Note.SAFE -> { SafeAction(); seAudio.safeJinglePlay() }
            Note.BAD -> { BadAction(); seAudio.badJinglePlay() }
            Note.MISS -> if (selectLevel.level <= 10) MissAction()
        }
        binding.textViewScore.text = "Score: ${noteData.score.totalScore} "
        ComboAction()
    }

    // Draw once per display frame; judgments follow playback time and do not depend on visibility.
    val timerRun = object : Runnable {
        override fun run() {
            if (timerKill || !notesReady || !GamePlaying) return
            if (!practiceMode && selectLevel.level <= 10 && noteData.score.borderScore <= 0) {
                if (!resultSegued) {
                    resultSegued = true; GamePlaying = false
                    recordPlayConditions()
                    GLOBAL.CurrentNOTES = noteData; GLOBAL.ResultPractice = practiceMode; GLOBAL.ResultFirst = true
                    val intent = Intent(applicationContext, Activity_Result::class.java)
                    intent.putExtra("hasPassedFirstNote", hasPassedFirstNote)
                    startActivityForResult(intent, 1002)
                }
                return
            }
            val player = cacheExoPlayer ?: return
            recordPlayConditions()
            val duration = player.duration
            binding.progressBarLoaded.progress = player.bufferedPercentage
            if (duration > 0) binding.progressBarPlayed.progress = (100000L * player.currentPosition / duration).toInt()
            if (!player.playWhenReady && !practiceSeeking) return
            var time = player.currentPosition / 1000.0
            time3secMediaTime?.let { start ->
                val countdown = -t03 + (android.os.SystemClock.elapsedRealtime() / 1000.0 - start)
                if (countdown < -0.10 + USERDATA.Time3secExitZureTime) {
                    playerView.alpha = 0.01f; player.seekTo(0); time = countdown
                } else if (time == 0.0) {
                    time = countdown
                } else {
                    playerView.alpha = 1.0f
                    saveData_time3sec = USERDATA.Time3secExitZureTime + (time - countdown) / 2
                    time3secMediaTime = null
                }
            }
            if (practiceMode && duration > 0) {
                if (practiceSeeking) time = duration / 1000.0 * binding.practiceSeekbar.progress / 10000.0
                else binding.practiceSeekbar.progress = (10000L * player.currentPosition / duration).toInt()
            }
            if (!practiceSeeking && pendingSeekSeconds == null) {
                processAutoPlay(time)
                processMisses(time)
            }
            val offset = time * xps
            for (note in noteData.notes) {
                val x = note.posX - offset + flickPointX
                val visible = x > drawRange[1] && x < drawRange[3]
                if (visible) { note.label.x = x.toFloat(); note.label.y = binding.textViewBarWord.y }
                note.label.isInvisible = !visible
                // Notes have two Android TextViews. Always hide the inactive one too.
                if (note.label !== note.flickableLabel) note.flickableLabel?.isInvisible = true
                if (note.label !== note.flickedLabel) note.flickedLabel?.isInvisible = true
            }
        }
    }
    private fun processMisses(time:Double) {
        if (waitingForFlickAfterSeek) return
        while (nextMissCheckIndex < noteData.notes.size) {
            val note = noteData.notes[nextMissCheckIndex]
            if (!note.isFlickable || note.flicked) { nextMissCheckIndex++; continue }
            val difference = note.time - (time + judgeOffset)
            if (difference >= safeLine[0]) break
            judgeFlick(note, note.word, -1.0, difference)
            nextMissCheckIndex++
            noteData.lastFlickedNum = maxOf(noteData.lastFlickedNum, nextMissCheckIndex)
        }
    }
    private fun processAutoPlay(time:Double) {
        if (!practiceMode || !binding.practiceAutoPlay.isChecked) return
        while (nextAutoPlayIndex < noteData.notes.size) {
            val note = noteData.notes[nextAutoPlayIndex]
            if (!note.isFlickable || note.flicked) { nextAutoPlayIndex++; continue }
            if (note.time > time + 0.02) break
            waitingForFlickAfterSeek = false
            judgeFlick(note, note.judgeWorder(note.word), note.time, 0.0)
            nextAutoPlayIndex++
        }
    }
    private fun recordPlayConditions() {
        val parameters = cacheExoPlayer?.playbackParameters ?: PlaybackParameters.DEFAULT
        noteData.score.recordPlayConditions(practiceMode, parameters.speed, parameters.pitch,
            practiceSeeking || pendingSeekSeconds != null,
            binding.practiceAutoPlay.isChecked, selectLevel.isEditing)
    }

    private fun resetTimelineCursors() {
        val first = noteData.notes.indexOfFirst { it.isFlickable && !it.flicked }
        nextMissCheckIndex = if (first < 0) noteData.notes.size else first
        nextAutoPlayIndex = nextMissCheckIndex
        noteData.lastFlickedNum = nextMissCheckIndex
    }
    private fun synchronizePracticeTimeline(time:Double) {
        noteData.score.markNonStandardConditions()
        for (note in noteData.notes) {
            if (!note.isFlickable) continue
            note.flickedTime = -1.0
            note.flicked = note.time <= time
            if (note.flicked) note.setUnFlickableFont() else note.setFlickableFont()
        }
        noteData.score.comboCounter = 0
        ComboAction()
        resetTimelineCursors()
        waitingForFlickAfterSeek = true
    }
    private fun configurePracticeControls() {
        binding.practiceSeekbar.max = 10000
        binding.practiceSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar:SeekBar?, progress:Int, fromUser:Boolean) {}
            override fun onStartTrackingTouch(bar:SeekBar?) {
                if (!practiceMode) return
                practiceSeeking = true; waitingForFlickAfterSeek = false
                cacheExoPlayer?.playWhenReady = false
                noteData.score.comboCounter = 0; ComboAction()
            }
            override fun onStopTrackingTouch(bar:SeekBar?) {
                if (!practiceMode) return
                noteData.score.markNonStandardConditions()
                practiceSeeking = false
                val player = cacheExoPlayer ?: return
                if (player.duration <= 0) { updatePlaybackState(); return }
                time3secMediaTime = null
                playerView.alpha = 1.0f
                val position = (player.duration * binding.practiceSeekbar.progress / 10000L).coerceAtMost(maxOf(0L, player.duration - 2000L))
                binding.practiceSeekbar.progress = (10000L * position / player.duration).toInt()
                pendingSeekSeconds = position / 1000.0
                waitingForFlickAfterSeek = true
                player.setSeekParameters(SeekParameters.EXACT)
                player.seekTo(position)
            }
        })
        binding.practiceRatebar.max = 10; binding.practiceRatebar.progress = 5
        binding.practiceRatebar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar:SeekBar?, progress:Int, fromUser:Boolean) {
                if (!practiceMode) return
                noteData.score.markNonStandardConditions()
                practiceRate = (progress + 5) / 10.0f
                binding.practiceRateLabel.text = "× %.1f".format(practiceRate)
                cacheExoPlayer?.setPlaybackParameters(PlaybackParameters(practiceRate))
            }
            override fun onStartTrackingTouch(bar:SeekBar?) {}
            override fun onStopTrackingTouch(bar:SeekBar?) {}
        })
        binding.practiceAutoPlay.setOnCheckedChangeListener { _, checked ->
            if (checked) noteData.score.markNonStandardConditions()
            resetTimelineCursors()
        }
    }
    private fun updateMovieLayout() {
        if (!::binding.isInitialized) return
        val fitsMovie = USERDATA.fitsMovieToKeyboardHeight
        layoutVideoBackground(playerView, binding.gameMovieFrame, fitsMovie)
        layoutKeyboardBackdrop(binding.keyboardBackdrop, binding.progressBarLoaded)
        // 拡大時は縦横比を維持して領域を埋め、はみ出す左右を切り取る。
        val resizeMode = if (fitsMovie) AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            else AspectRatioFrameLayout.RESIZE_MODE_FIT
        if (playerView.resizeMode != resizeMode) playerView.resizeMode = resizeMode
        val unset = ConstraintLayout.LayoutParams.UNSET
        val laneParams = binding.noteBarView.layoutParams as ConstraintLayout.LayoutParams
        val laneTop = if (fitsMovie) unset else R.id.gameMovieFrame
        val laneBottom = if (fitsMovie) R.id.gameMovieFrame else unset
        val laneMargin = PX(48.0f)
        if (laneParams.topToBottom != laneTop || laneParams.bottomToBottom != laneBottom) {
            laneParams.topToBottom = laneTop
            laneParams.bottomToBottom = laneBottom
            laneParams.topMargin = if (fitsMovie) 0 else laneMargin
            laneParams.bottomMargin = if (fitsMovie) laneMargin else 0
            binding.noteBarView.layoutParams = laneParams
        }
        val menuParams = binding.buttonMenu.layoutParams as ConstraintLayout.LayoutParams
        val menuTop = if (fitsMovie) unset else R.id.gameMovieFrame
        if (menuParams.topToBottom != menuTop) {
            menuParams.topToBottom = menuTop
            binding.buttonMenu.layoutParams = menuParams
        }
        if (fitsMovie && !paused && hasWindowFocus()) {
            // キーボードが消えたときは、直前の動画サイズを維持する。
            val bottom = if (useMyKeybd) {
                val keyboard = myKeybd ?: return
                if (!keyboard.isShown || keyboard.height == 0) return
                keyboard.keyboardTopOnScreen()
            } else {
                val decor = window.decorView
                val rect = Rect(); decor.getWindowVisibleDisplayFrame(rect)
                val decorLocation = IntArray(2); decor.getLocationOnScreen(decorLocation)
                val hiddenHeight = decorLocation[1] + decor.height - rect.bottom
                // システムバーによる差を除外して、IME表示中だけサイズを更新する。
                if (hiddenHeight <= GLOBAL.NavigationBarHeight + PX(100.0f)) return
                rect.bottom
            }
            USERDATA.maeKeyboardTop = bottom
            val location = IntArray(2); binding.gameMovieFrame.getLocationOnScreen(location)
            val progressSpace = binding.progressBarLoaded.bottom - binding.progressBarLoaded.top
            val available = (bottom - location[1] - progressSpace).coerceAtLeast(1)
            if (binding.gameMovieFrame.layoutParams.height != available) {
                binding.gameMovieFrame.layoutParams.height = available; binding.gameMovieFrame.requestLayout()
            }
        }
    }

    private class JudgeImageView(
        context: Context,
        val source: DecolationLabel,
        val inset: Int
    ) : ImageView(context)

    private val greatPool = arrayListOf<JudgeImageView>()
    private val goodPool = arrayListOf<JudgeImageView>()
    private val safePool = arrayListOf<JudgeImageView>()
    private val badPool = arrayListOf<JudgeImageView>()
    private val missPool = arrayListOf<JudgeImageView>()
    private var judgeZ = 0f
    private fun prepareEffectPools() {
        resetEffectPools()
        fun fill(pool:ArrayList<JudgeImageView>, first:DecolationLabel) {
            pool.forEach {
                it.setImageDrawable(null)
                (it.parent as? ViewGroup)?.removeView(it)
            }
            pool.clear()
            val parent = first.parent as ViewGroup
            // ストロークの半幅と影のずれに、アンチエイリアス用の余白を加える。
            val shadowExtent = if (Color.alpha(first.strokeShadowColor) != 0)
                maxOf(abs(first.strokeShadowX), abs(first.strokeShadowY)) else 0f
            val inset = kotlin.math.ceil(first.strokeWidth.coerceAtLeast(0f) / 2f + shadowExtent).toInt() + 2
            val bitmap = first.createTextBitmap(inset)
            first.isInvisible = true
            repeat(10) {
                val copy = JudgeImageView(this, first, inset)
                copy.scaleType = ImageView.ScaleType.FIT_XY
                copy.setImageBitmap(bitmap)
                copy.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                copy.alpha = 0.0f
                parent.addView(copy, ViewGroup.LayoutParams(bitmap.width, bitmap.height))
                copy.x = first.x - inset
                copy.y = first.y - inset
                pool.add(copy)
            }
        }
        fill(greatPool, binding.textViewGreat1)
        fill(goodPool, binding.textViewGood1)
        fill(safePool, binding.textViewSafe1)
        fill(badPool, binding.textViewBad1)
        fill(missPool, binding.textViewMiss1)
    }
    private fun resetEffectPools() {
        judgementAnimations.values.forEach { it.cancel() }; judgementAnimations.clear()
        (greatPool + goodPool + safePool + badPool + missPool).forEach {
            it.animate().cancel(); it.alpha = 0.0f; it.translationZ = 0f
        }
        judgeZ = 0f
        greatActionCount = 0; goodActionCount = 0; safeActionCount = 0; badActionCount = 0; missActionCount = 0
    }

    //ゲームの描写 エフェクト処理 関数 _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    fun GreatAction() {
        //println("Great!!")
        val view = greatPool[greatActionCount]
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        judgementAnimations[view] = animatorSet
        animatorSet.start()
        greatActionCount += 1
        greatActionCount = greatActionCount % greatPool.size
    }

    fun GoodAction() {
        //println("Good!")
        val view = goodPool[goodActionCount]
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        judgementAnimations[view] = animatorSet
        animatorSet.start()
        goodActionCount += 1
        goodActionCount = goodActionCount % goodPool.size
    }

    fun SafeAction() {
        //println("safe")
        val view = safePool[safeActionCount]
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        judgementAnimations[view] = animatorSet
        animatorSet.start()
        safeActionCount += 1
        safeActionCount = safeActionCount % safePool.size
    }

    fun BadAction() {
        //println("bad")
        val view = badPool[badActionCount]
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        judgementAnimations[view] = animatorSet
        animatorSet.start()
        badActionCount += 1
        badActionCount = badActionCount % badPool.size
    }

    fun MissAction() {
        //println("miss")
        val view = missPool[missActionCount]
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        judgementAnimations[view] = animatorSet
        animatorSet.start()
        missActionCount += 1
        missActionCount = missActionCount % missPool.size
    }

    fun FlickAction() {
        if (flickAnimatorSet != null) {
            //println("flickAnimatorSet.isRunning="+flickAnimatorSet.isRunning)
            if (flickAnimatorSet.isRunning) {
                //println("cancel")
                flickAnimatorSet.cancel()
            }
        }
        binding.flickCircle.alpha = 0.0f
        val anime1 = ObjectAnimator.ofFloat(binding.flickCircle, "scaleX", 0.5f, 1.2f)
        anime1.duration = 150
        val anime2 = ObjectAnimator.ofFloat(binding.flickCircle, "scaleY", 0.5f, 1.2f)
        anime2.duration = 150
        val anime3 = ObjectAnimator.ofFloat(binding.flickCircle, "alpha", 0.0f, 1.0f)
        anime3.duration = 50
        val animatorSet1 = AnimatorSet()
        animatorSet1.playTogether(anime1, anime2, anime3)
        val anime4 = ObjectAnimator.ofFloat(binding.flickCircle, "alpha", 1.0f, 1.0f)
        anime4.duration = 300
        val anime5 = ObjectAnimator.ofFloat(binding.flickCircle, "alpha", 1.0f, 0.0f)
        anime5.duration = 100
        val flickAnimatorSet = AnimatorSet()
        flickAnimatorSet.playSequentially(animatorSet1, anime4, anime5)
        flickAnimatorSet.start()
    }

    private val judgementAnimations = mutableMapOf<JudgeImageView, AnimatorSet>()
    private fun createJudgeAction(view: JudgeImageView): ArrayList<Animator> {
        judgementAnimations.remove(view)?.cancel()
        // 判定5種類で連番を共有し、最新の判定を最前面にする。
        judgeZ += 1f
        view.translationZ = judgeZ
        view.alpha = 1.0f
        // 余白を補正し、レイアウト変更後もXMLラベルの位置を基準にする。
        view.x = view.source.x - view.inset
        val endY = view.source.y - view.inset
        val startY = (binding.dotCircle.y + view.source.y) / 2f - view.inset
        val animatorList: ArrayList<Animator> = arrayListOf()
        var anime = ObjectAnimator.ofFloat(view, "y", startY, endY)
        anime.duration = 300
        animatorList.add(anime)
        anime = ObjectAnimator.ofFloat(view, "y", endY, endY)
        anime.duration = 500
        animatorList.add(anime)
        anime = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f)
        anime.duration = 100
        animatorList.add(anime)

        return animatorList
    }

    fun ComboAction() {
        if (noteData.score.comboCounter == 0) {
            binding.textViewCombo.isInvisible = true
            return
        }
        if (noteData.score.comboCounter < 5) {
            return
        }
        binding.textViewCombo.setText("${noteData.score.comboCounter} combo ")
        if (noteData.score.comboCounter > 5) {
            return
        }
        //5 comboのときだけアニメーション
        binding.textViewCombo.isInvisible = false

        val anime = ObjectAnimator.ofFloat(binding.textViewCombo, "y", comboanimeStartYsa+binding.dotCircle.y, comboanimeEndYsa+binding.dotCircle.y)
        anime.duration = 300
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(anime)
        animatorSet.start()
    }

    fun DP(px: Int): Float {
        val metrics = applicationContext.resources.displayMetrics
        return px / metrics.density
    }
    fun PX(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, applicationContext.resources.displayMetrics)
            .toInt()
    }

    fun iP2An(n: Int): Int {
        return (n * gameviewWidth / 375).toInt()
    }
    fun iP2An(n: Float): Float {
        return (n * gameviewWidth / 375).toFloat()
    }
    fun iP2An(n: Double): Double {
        return (n * gameviewWidth / 375).toDouble()
    }

    override fun onBackPressed() {
        // println("back")
        //super.onBackPressed()
        OpenMenu()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::binding.isInitialized) {
            // 簡易キーボードへの切り替え後も、復帰して配置が確定した時点で再計算する。
            binding.root.post {
                if (!isFinishing && !isDestroyed) updateMovieLayout()
            }
        }
        if( useMyKeybd ){ return }
        // println(currentFocus)
        if (hasFocus && binding.editText == currentFocus) {
            Handler().postDelayed(10) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.editText, 0)
            }
        }
    }

    fun menuButton(view: View) {
        // println("push menu")
        OpenMenu()
    }
    fun OpenMenu() {
        if (!notesReady || resultSegued || gameMenuOpen) return
        gameMenuOpen = true
        GamePlaying = false
        stopFrames()
        updatePlaybackState()
        SESystemAudio.gameMenuSePlay()
        val intent: Intent = Intent(applicationContext, Activity_GameMenu::class.java)
        intent.putExtra("practiceMode", practiceMode)
        // println("post ${binding.progressBar.y} ${binding.progressBar2.y} ${binding.progressBar.height} ${binding.progressBar2.height}")
        intent.putExtra("progress1", binding.progressBar.centerY)
        intent.putExtra("progress2", binding.progressBar2.centerY)

        binding.keyboardBackdrop.isVisible = USERDATA.fitsMovieToKeyboardHeight
        startActivityForResult(intent, 1001)
    }
    fun ShowHideBorder(ue:Boolean){
        // 非表示のゲージも配置を維持し、メニュー遷移時に座標を取得できるようにする。
        binding.progressBar.isInvisible = !ue || selectLevel.level > 10
        binding.progressBar2.isInvisible = ue || selectLevel.level > 10
    }

    private val showIme = Runnable {
        if (!useMyKeybd && !timerKill && !isFinishing && !isDestroyed && binding.editText.hasFocus()) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.editText, 0)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (!useMyKeybd && ev?.actionMasked == MotionEvent.ACTION_DOWN) {
            mHandler.removeCallbacks(showIme)
            mHandler.postDelayed(showIme, 10)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPause() {
        super.onPause()
        // println("onPause!!")
        paused = true
        gameViewResumed = false
        mHandler.removeCallbacks(showIme)
        binding.editText.logInputDiagnostics()
        binding.editText.clearInputWhenSafe()
        timerKill = true
        stopFrames()
        GamePlaying = false
        // println("GamePlaying = ${GamePlaying}")
        updatePlaybackState()
        saveData_time3sec?.let {
            USERDATA.Time3secExitZureTime = it.toFloat()
            saveData_time3sec = null
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewResumed = true
        mHandler.post(startPlayback)
        binding.keyboardBackdrop.isVisible = false
        // println("resume")
        if (paused) {
            paused = false
            timerKill = false
            GamePlaying = true
            startFrames()
            updatePlaybackState()
            // println("GamePlaying = ${GamePlaying}")
            //ゲージ
            binding.progressBar.setProgress((noteData.score.borderScore * 10).toInt())
            binding.progressBar2.setProgress((noteData.score.borderScore * 10).toInt())
            val view = window.decorView
            //最下のボタン非表示
            view.systemUiVisibility = view.systemUiVisibility or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
        }
    }

    override fun onDestroy() {
        movieLoadJob?.cancel()
        movieScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        resetEffectPools()
        (greatPool + goodPool + safePool + badPool + missPool).forEach { it.setImageDrawable(null) }
        gameViewResumed = false
        cacheExoPlayer?.playWhenReady = false
        mHandler.removeCallbacks(startPlayback)
        mHandler.removeCallbacks(showIme)
        binding.editText.onFlickInput = null
        binding.editText.captureInputTime = null
        playbackListener?.let { cacheExoPlayer?.removeListener(it) }
        playbackListener = null
        run?.let { mHandler.removeCallbacks(it) }
        super.onDestroy()
        saveData_time3sec?.let {
            USERDATA.Time3secExitZureTime = it.toFloat()
            saveData_time3sec = null
        }
        timerKill = true
        stopFrames()
        GamePlaying = false
        // println("GamePlaying = ${GamePlaying}")
        if (GLOBAL.CurrentNOTES === noteData && !isChangingConfigurations) {
            GLOBAL.CurrentNOTES = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isFinishing || !::selectMusic.isInitialized || !::selectLevel.isInitialized) return
        // startActivityForResult()の際に指定した識別コードとの比較
        if( useMyKeybd != USERDATA.UseNicoFlickKeyboard ){
            useMyKeybd = USERDATA.UseNicoFlickKeyboard
            myKeybd?.isHidden = !useMyKeybd
            binding.editText.clearInputWhenSafe()
            binding.editText.isEnabled = !useMyKeybd
            if (!useMyKeybd) binding.editText.requestFocus()
        }
        if( borderPosUe != USERDATA.BorderPosUe ) {
            borderPosUe = USERDATA.BorderPosUe
            ShowHideBorder(borderPosUe)
        }
        when(requestCode){
            1001 -> {
                gameMenuOpen = false
                // 返却結果ステータスとの比較
                when (resultCode) {
                    10 -> {
                        if (hasPassedFirstNote) {
                            if (!selectLevel.isEditing) USERDATA.recordLevelPlay(selectLevel.sqlID)
                            GLOBAL.SuggestPlayerName = true
                        }
                        cacheExoPlayer?.let { player ->
                            if (player.duration > 0 && player.currentPosition >= player.duration / 2) USERDATA.PlayCount.addPlayCount(selectLevel.sqlID)
                        }
                        ServerDataHandler().postUserCountData()
                        finish()
                    }
                    11 -> {
                        noteData.noteReset()
                        nextMissCheckIndex = 0; nextAutoPlayIndex = 0
                        waitingForFlickAfterSeek = false; pendingSeekSeconds = null
                        resetEffectPools()
                        binding.textViewCombo.isInvisible = true
                        binding.textViewScore.setText("Score: 0 ")
                        initialPlaybackReady = false
                        time3secMediaTime = null
                        countdownPausedAt = null
                        updatePlaybackState()
                        cacheExoPlayer?.seekTo(0)
                        startPlayback.run()
                    }
                    13 -> {
                        practiceMode = true
                        binding.practiceControls.isVisible = true
                        cacheExoPlayer?.setPlaybackParameters(PlaybackParameters(practiceRate))
                        updatePlaybackState()
                        resetTimelineCursors()
                    }
                    12 -> {
                        cacheExoPlayer?.let {
                            updatePlaybackState()
                        }
                    }
                }
                // ジャッジオフセットの取得
                USERDATA.JudgeOffset[selectLevel.sqlID]?.let {
                    judgeOffset = Double(it)
                }
                binding.textViewJudgeOffset.setText("offset: %.2f".format(judgeOffset))
                binding.textViewJudgeOffset.isVisible = USERDATA.showOffsetLabel
                if (practiceMode) cacheExoPlayer?.setPlaybackParameters(PlaybackParameters(practiceRate))

                playerviewHeight = playerviewOriginalHeight * USERDATA.PlayerHeightPer / 10000
                if (!USERDATA.fitsMovieToKeyboardHeight) {
                    binding.gameMovieFrame.layoutParams.height = playerviewHeight
                    binding.gameMovieFrame.requestLayout()
                }
                updateMovieLayout()
            }
            1003 -> onCreate2()
            else -> {
                GLOBAL.SuggestPlayerName = true
                finish()
            }
        }
    }
}
