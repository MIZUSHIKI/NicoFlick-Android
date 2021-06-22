package com.mizushiki.nicoflick_a

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
import android.provider.Settings
import android.text.InputFilter
import android.text.Spanned
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_game_view.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class Activity_GameView : AppCompatActivity() {

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
    var drawRange = arrayOf(-100, -50, 400)
    val greatLine = listOf(-0.080, 0.080)
    val goodLine = listOf(-0.200, 0.200)
    val safeLine = listOf(-0.400, 0.300)
    var judgeOffset = 0.0
    var xps = 0.0
    var gameviewHeight = 667.0 // あとで実際の画面サイズに変更
    var playerviewHeight = 0
    var playerviewOriginalHeight = 0

    //効果音
    val seAudio = SEAudio
    //ゲームデータ
    val noteData = Notes()

    //private lateinit var simpleExoPlayer: SimpleExoPlayer
    private var cacheExoPlayer: SimpleExoPlayer? = null
    private lateinit var playerView: PlayerView

    var maeSource = ""
    var maeSourceClear = false
    var editFlg = 0
    var paused = false
    var resultSegued = false //遷移中フラグ

    var usedSimeji:Boolean = false
        //maeSourceを30文字ごとに消去する。（editも""にする）
        //Simejiは一気に30文字以上入力できないらしい。（1秒以上の隙間なく30文字以上来られると強制ミスになってしまう）
        //今のところ見つけたのはSimejiだけ。(一応Simejiは(超軽量状態にしていたら)editを1文字ごとに""してもラグはなかった)

    val mHandler = Handler()
    var timerKill = false
    private var run: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_view)

        //Selectorからデータ受け取り
        selectMusic = GLOBAL.SelectMUSIC!!
        selectLevel = GLOBAL.SelectLEVEL!!

        //simpleExoPlayer = SimpleExoPlayer.Builder(applicationContext)
        //    .build()
        playerView = findViewById(R.id.playerView)

        // 使用IME名取得
        val imeString = Settings.Secure.getString(applicationContext.contentResolver,Settings.Secure.DEFAULT_INPUT_METHOD)
        println("ime = $imeString")
        usedSimeji = imeString.contains("simeji")
        println("usedSimeji=$usedSimeji")

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
    fun onCreate2() {
        //動画 No.
        val smNum = Regex("watch/(.+)$").find(selectMusic.movieURL)!!.groupValues.get(1)

        //Movie呼び込み
        var nicodougaURL = ""
        MovieAccess().StreamingUrlNicoAccess(smNum) {
            // 動画
            nicodougaURL = it!!
            println(nicodougaURL)
            //println(applicationContext.packageName)
            setExo(nicodougaURL,smNum)
        }

        editText.filters = arrayOf(object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence {
                if (source != null && end > 0) {
                    //println("source="+source+"  $start , $end , $dest , $dstart , $dend ")
                    /*
                    if(source == "　"){
                        Handler().postDelayed(10) {
                            val intent: Intent = Intent(applicationContext, Activity_Result::class.java)
                            GLOBAL.CurrentNOTES = noteData
                            GLOBAL.ResultFirst = true
                            startActivityForResult(intent, 1002)
                        }
                    }
                    */
                    //println("maeSourceClear="+maeSourceClear)
                    if(maeSourceClear){
                        maeSource = ""
                        maeSourceClear = false
                    }
                    //println("$source -- $maeSource == ${source.toString()} || ${maeSource.length} > ${source.length}")
                    if(maeSource == source.toString() || maeSource.length > source.length){
                        //カーソル移動等だったりで、うまくlast()で取得できなくなる可能性があるから消す。
                        FlickInput("x")
                        editText.setText("")
                        maeSource=""
                    }else{
                        FlickInput(source.last().toString())
                        maeSource = source.toString()
                        if( usedSimeji && source.length >= 30 ) {
                            editText.setText("")
                            maeSource=""
                        }
                    }
                }
                return ""
            }
        })
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        gameviewWidth = point.x.toDouble()
        gameviewHeight = point.y.toDouble()
        println("xy = $gameviewWidth x $gameviewHeight")
        flickPointX = gameviewWidth * 50 / 375
        noteData.gameviewWidth = gameviewWidth
        noteData.flickPointX = flickPointX
        //計算しておく
        xps = (gameviewWidth - flickPointX) * Double(selectLevel.speed) / 300 //ノートが一秒間に進む距離
        //
        drawRange[0] = iP2An(drawRange[0]) //-100
        drawRange[1] = iP2An(drawRange[1]) //-50
        drawRange[2] = iP2An(drawRange[2]) // 400

        run = Runnable {
            if (playerView.height == 0) {
                println("playerView.height=" + playerView.height)
                mHandler.postDelayed(run!!, 10)
                return@Runnable
            }
            //まずプレイヤービューのサイズを指定比率で端末に合わせる
            if( (playerView.width * 270 / 375) < (gameviewHeight * 0.4) ) {
                playerviewOriginalHeight = playerView.width * 270 / 375
                println("270")
            }else {
                playerviewOriginalHeight = (gameviewHeight * 0.4).toInt()
                println("0.4")
            }
            playerviewHeight = playerviewOriginalHeight * USERDATA.PlayerHeightPer / 10000
            playerView.layoutParams.height = playerviewHeight

            // FrameLayout のインスタンスを取得
            val layout: FrameLayout = findViewById(R.id.notesWindow)

            //フリックポイント
            dotCircle.centerX = (flickPointX + textView_BarWord.width / 2).toFloat()
            dotCircle.isVisible = true
            //フリックアクション
            flickCircle.centerX = dotCircle.centerX
            //Great
            textView_Great1.centerX = dotCircle.centerX
            textView_Great2.centerX = dotCircle.centerX
            textView_Great3.centerX = dotCircle.centerX
            textView_Great4.centerX = dotCircle.centerX
            textView_Great5.centerX = dotCircle.centerX
            //Good
            textView_Good1.centerX = dotCircle.centerX
            textView_Good2.centerX = dotCircle.centerX
            textView_Good3.centerX = dotCircle.centerX
            textView_Good4.centerX = dotCircle.centerX
            textView_Good5.centerX = dotCircle.centerX
            //Safe
            textView_Safe1.centerX = dotCircle.centerX
            textView_Safe2.centerX = dotCircle.centerX
            textView_Safe3.centerX = dotCircle.centerX
            textView_Safe4.centerX = dotCircle.centerX
            textView_Safe5.centerX = dotCircle.centerX
            //Bad
            textView_Bad1.centerX = dotCircle.centerX
            textView_Bad2.centerX = dotCircle.centerX
            textView_Bad3.centerX = dotCircle.centerX
            textView_Bad4.centerX = dotCircle.centerX
            textView_Bad5.centerX = dotCircle.centerX
            //Miss
            textView_Miss1.centerX = dotCircle.centerX
            textView_Miss2.centerX = dotCircle.centerX
            textView_Miss3.centerX = dotCircle.centerX
            textView_Miss4.centerX = dotCircle.centerX
            textView_Miss5.centerX = dotCircle.centerX
            //
            textView_combo.centerX = dotCircle.centerX
            //
            animationStartYsa = ( (dotCircle.y + textView_Great1.y) / 2 ) - dotCircle.y
            animationEndYsa = textView_Great1.y - dotCircle.y
            comboanimeStartYsa = ( (textView_combo.y + textView_Great1.y) / 2 ) - dotCircle.y
            comboanimeEndYsa = textView_combo.y - dotCircle.y

            //ノートデータを解析、生成
            noteData.noteAnalyze(selectLevel.noteData, selectLevel.speed, selectLevel.level)
            //ノートラベルも最初にすべて作ってしまう（移動だとか処理をさせるのは見えるやつだけにする）
            //「フリックしない文字」のLabel作成 -> 「フリックする文字」のLabel作成 の順にして、フリック文字が前面に出るようにする。
            val rectF = RectF(
                textView_BarWord.x - 100, //枠外
                textView_BarWord.y,
                textView_BarWord.x - 100 + textView_BarWord.height,
                textView_BarWord.y + textView_BarWord.height
            )
            // それぞれのTextView に設定を行い、FrameLayout のインスタンスに追加
            for (note in noteData.notes) {
                if (note.isFlickable == false) {
                    //Label生成
                    val label = OutlineTextView(this, rectF)
                    label.setTextColor(Color.WHITE)
                    label.rectF = rectF
                    label.setTextSize(DP(textView_BarWord.textSize.toInt()))
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
                    label.setTextSize(DP(textView_BarWord.textSize.toInt()))
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
                    label.setTextSize(DP(textView_BarWord.textSize.toInt()))
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

            //タイマー発動
            mHandler.post(timerRun)
        }
        mHandler.post(run!!)

        //保存データの取得
        // スコア読み込み
        USERDATA.Score.scores[selectLevel.sqlID]?.let {
            textView_HighScore.setText("High Score: " + String(it[0]) + " ")
        }
        textView_Score.setText("Score: 0 ")
        // ジャッジオフセットの取得
        USERDATA.JudgeOffset[selectLevel.sqlID]?.let {
            judgeOffset = Double(it)
        }
        textView_judgeOffset.setText("offset: %.2f".format(judgeOffset))

        if( selectLevel!!.level > 10 ){
            progressBar.isVisible = false
        }

        editText.requestFocus()
        //最下のボタン非表示
        val view = window.decorView
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
    }

    fun setExo(nicodougaURL:String,smNum:String) {

        //val loadControl = DefaultLoadControl.Builder().setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,10*60*1000,10*60*1000,0)
        //val simpleExoPlayer = SimpleExoPlayer.Builder(applicationContext).setLoadControl()
        //    .build()
        cacheExoPlayer = CachedMovies.access(nicodougaURL,smNum)

        playerView.apply {
            player = cacheExoPlayer
        }
        cacheExoPlayer!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        cacheExoPlayer!!.seekTo(0)
        Handler().postDelayed(Runnable {
            cacheExoPlayer!!.playWhenReady = true
        }, 500)
        cacheExoPlayer!!.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                println("playbackState=" + playbackState)
                when (playbackState) {
                    ExoPlayer.STATE_ENDED -> {
                        println("終了 -> リザルト画面へ")

                        if(resultSegued==false){
                            resultSegued = true
                            val intent: Intent = Intent(applicationContext, Activity_Result::class.java)
                            //intent.putExtra("SelectMusicID", selectMusic.sqlID)
                            //intent.putExtra("SelectLevelID", selectLevel.sqlID)
                            GLOBAL.CurrentNOTES = noteData
                            GLOBAL.ResultFirst = true
                            startActivityForResult(intent, 1002)
                        }
                    }
                }
            }
        })
    }

    //フリック判定
    fun FlickInput(string: String) {

        //フリックアクション
        FlickAction()

        if( cacheExoPlayer == null ){ return }
        var flickTime = cacheExoPlayer!!.currentPosition.toDouble() / 1000 - 0.05 + judgeOffset//キー打ち定量ズレ
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
            }
        }
        //着目noteがあったとき
        if (flickedNote != null) {
            //println("flickTime="+flickTime+", flickedNote="+flickedNote.time)
            //まずフリックした文字とnoteの文字があっているか判定して次にタイミング判定
            val judge = flickedNote.judgeWord(flickWord = string)
            //println("judge=$judge, ${flickedNote.word} - ${string}")
            if (judge == Note.BAD) {
                //Bad
                flickedNote.flickedTime = flickTime
                flickedNote.flicked = true
                flickedNote.judge = Note.BAD
                noteData.score.addScore(judge = Note.BAD)
                //print("Bad")
                //ゲージ
                progressBar.setProgress((noteData.score.borderScore * 10).toInt())
                //フリック済みフォントに設定
                flickedNote.setUnFlickableFont()
                flickedNote.label.y = textView_BarWord.y
                //エフェクト
                BadAction()
                //se
                seAudio.badJinglePlay()

            } else if (judge == Note.SAFE) {
                //Safe
                flickedNote.flickedTime = flickTime
                flickedNote.flicked = true
                flickedNote.judge = Note.SAFE
                noteData.score.addScore(judge = Note.SAFE)
                //print("Safe")
                //フリック済みフォントに設定
                flickedNote.setUnFlickableFont()
                flickedNote.label.y = textView_BarWord.y
                //エフェクト
                SafeAction()
                //se
                seAudio.safeJinglePlay()

            } else if (greatLine[0] <= minDiffTime && minDiffTime <= greatLine[1]) {
                //Great!!
                flickedNote.flickedTime = flickTime
                flickedNote.flicked = true
                flickedNote.judge = Note.GREAT
                noteData.score.addScore(judge = Note.GREAT)
                //print("Great!!")
                //ゲージ
                progressBar.setProgress((noteData.score.borderScore * 10).toInt())
                //フリック済みフォントに設定
                flickedNote.setUnFlickableFont()
                flickedNote.label.y = textView_BarWord.y
                //エフェクト
                GreatAction()
                //se
                seAudio.okJinglePlay()

            } else if (goodLine[0] <= minDiffTime && minDiffTime <= goodLine[1]) {
                //Good!
                flickedNote.flickedTime = flickTime
                flickedNote.flicked = true
                flickedNote.judge = Note.GOOD
                noteData.score.addScore(judge = Note.GOOD)
                //print("Good!")
                //ゲージ
                progressBar.setProgress((noteData.score.borderScore * 10).toInt())
                //フリック済みフォントに設定
                flickedNote.setUnFlickableFont()
                flickedNote.label.y = textView_BarWord.y
                //エフェクト
                GoodAction()
                //se
                seAudio.okJinglePlay()

            } else if (safeLine[0] <= minDiffTime && minDiffTime <= safeLine[1]) {
                //Safe
                flickedNote.flickedTime = flickTime
                flickedNote.flicked = true
                flickedNote.judge = Note.SAFE
                noteData.score.addScore(judge = Note.SAFE)
                //print("Safe")
                //フリック済みフォントに設定
                flickedNote.setUnFlickableFont()
                flickedNote.label.y = textView_BarWord.y
                //エフェクト
                SafeAction()
                //se
                seAudio.safeJinglePlay()

            } else {
                //何かしらには入ったはずだが、一応BAD
                flickedNote.flickedTime = flickTime
                flickedNote.flicked = true
                flickedNote.judge = Note.BAD
                noteData.score.addScore(judge = Note.BAD)
                //print("Bad")
                //ゲージ
                progressBar.setProgress((noteData.score.borderScore * 10).toInt())
                //フリック済みフォントに設定
                flickedNote.setUnFlickableFont()
                flickedNote.label.y = textView_BarWord.y
                //エフェクト
                BadAction()
                //se
                seAudio.badJinglePlay()
            }

            //
            //println("${flickedNote.word} - ${flickedNote.judge} : ${flickedNote.flickedTime} - ${flickedNote.time} = ${(flickedNote.flickedTime - flickedNote.time)}")

        } else {

            println("flickTime=" + flickTime)
        }
        textView_Score.setText("Score: ${noteData.score.totalScore} ")
        ComboAction()

        //着目ノートがあろうとなかろうと、フリックした時間 -> 次のフリッカブルまでの時間が1秒以上あれば TextView削除
        if (noteData.lastFlickedNum >= noteData.notes.size) {
            editText.setText("");
            maeSourceClear = true
        } else {
            for (i in (noteData.lastFlickedNum until noteData.notes.size)) {
                val note = noteData.notes[i]
                if (note.isFlickable && note.flicked == false) {
                    val diffTime = note.time - flickTime
                    //println("diffTime="+diffTime)
                    if (diffTime > 1.0) {
                        editText.setText("");
                        maeSourceClear = true
                    }
                    break
                }
                if (i >= noteData.notes.size) {
                    editText.setText("");
                    maeSourceClear = true
                }
            }
        }
    }
    //タイマー
    val timerRun = object : Runnable {
        override fun run() {
            if (selectLevel.level <= 10 && noteData.score.borderScore <= 0) {
                //強制終了
                println("失敗")
                if(resultSegued==false){
                    resultSegued = true
                    val intent: Intent = Intent(applicationContext, Activity_Result::class.java)
                    GLOBAL.CurrentNOTES = noteData
                    GLOBAL.ResultFirst = true
                    startActivityForResult(intent, 1002)
                }
                return
            }

            if( cacheExoPlayer != null ){
                //進捗状況
                progressBar_Loaded.progress = cacheExoPlayer!!.bufferedPercentage
                progressBar_Played.progress = (100000 * cacheExoPlayer!!.currentPosition / cacheExoPlayer!!.duration).toInt()

                val time = cacheExoPlayer!!.currentPosition.toDouble() / 1000
                //xps = (gameviewWidth-flickPointX)*Double(selectLevel.speed)/300 //ノートが一秒間に進む距離（View作成時に計算済み）
                // speed=300が出現してから打つまでの時間が１秒。つまりspeed=100は出現してから打つまでの時間が３秒。
                val offsetX: Double = time * xps
                //ノートをゾロ動かす
                for ((index, note) in noteData.notes.withIndex()) {
                    val x = note.posX - offsetX + flickPointX
                    //もしオフセット後の表示位置が400(375+25)以内なら動かす
                    if (drawRange[0] < x && x < drawRange[2]) {
                        note.label.x = x.toFloat()
                        note.label.y = textView_BarWord.y
                        note.label.isInvisible = false

                        //過ぎ去りBad判定
                        if (note.isFlickable && note.flicked == false) {
                            val diffTime = note.time - (time + judgeOffset)
                            if (diffTime < safeLine[0]) {
                                //Miss
                                note.flickedTime = -1.0
                                note.flicked = true
                                note.judge = Note.MISS
                                noteData.score.addScore(judge = Note.MISS)
                                //println("Miss")
                                //ゲージ
                                progressBar.setProgress((noteData.score.borderScore * 10).toInt())
                                if (selectLevel.level <= 10) {
                                    //フリック済みフォントに設定
                                    note.setUnFlickableFont()
                                    note.label.y = textView_BarWord.y
                                    //エフェクト
                                    MissAction()
                                }//Fullバージョンのときはエフェクトしない（MISSとしてカウント。但しリザルトも表示は0にする。）

                                //次のフリックまでの時間がSafeLine+1秒以上あって、TextViewになんか入ってたら消す
                                if (index + 1 >= noteData.notes.size) {
                                    editText.setText("");
                                    maeSourceClear = true
                                } else {
                                    val flickTime = note.time
                                    for (i in (index + 1 until noteData.notes.size)) {
                                        val note = noteData.notes[i]
                                        if (note.isFlickable && note.flicked == false) {
                                            val diffTime = note.time - flickTime
                                            if (diffTime > (1.0 - safeLine[0])) {
                                                editText.setText("");
                                                maeSourceClear = true
                                            }
                                            break
                                        }
                                        if (i >= noteData.notes.size) {
                                            editText.setText("");
                                            maeSourceClear = true
                                        }
                                    }
                                }
                                ComboAction()
                            }
                        }
                        //左に消えたらビューも消す
                        if (x <= drawRange[1]) {
                            note.label.isInvisible = true
                        }
                    }/*else {
                        if note.label.isHidden == false {
                            note.label.isHidden = true
                        }
                    }*/
                }
            }

            //繰り返し
            if (!timerKill) {
                mHandler.postDelayed(this, 1)
            }
        }
    }


    //ゲームの描写 エフェクト処理 関数 _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    fun GreatAction() {
        //println("Great!!")
        val view: TextView
        when (greatActionCount) {
            0 -> view = textView_Great1
            1 -> view = textView_Great2
            2 -> view = textView_Great3
            3 -> view = textView_Great4
            else -> view = textView_Great5
        }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        animatorSet.start()
        greatActionCount += 1
        greatActionCount = greatActionCount % 5
    }

    fun GoodAction() {
        //println("Good!")
        val view: TextView
        when (goodActionCount) {
            0 -> view = textView_Good1
            1 -> view = textView_Good2
            2 -> view = textView_Good3
            3 -> view = textView_Good4
            else -> view = textView_Good5
        }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        animatorSet.start()
        goodActionCount += 1
        goodActionCount = goodActionCount % 5
    }

    fun SafeAction() {
        //println("safe")
        val view: TextView
        when (safeActionCount) {
            0 -> view = textView_Safe1
            1 -> view = textView_Safe2
            2 -> view = textView_Safe3
            3 -> view = textView_Safe4
            else -> view = textView_Safe5
        }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        animatorSet.start()
        safeActionCount += 1
        safeActionCount = safeActionCount % 5
    }

    fun BadAction() {
        //println("bad")
        val view: TextView
        when (badActionCount) {
            0 -> view = textView_Bad1
            1 -> view = textView_Bad2
            2 -> view = textView_Bad3
            3 -> view = textView_Bad4
            else -> view = textView_Bad5
        }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        animatorSet.start()
        badActionCount += 1
        badActionCount = badActionCount % 5
    }

    fun MissAction() {
        //println("miss")
        val view: TextView
        when (missActionCount) {
            0 -> view = textView_Miss1
            1 -> view = textView_Miss2
            2 -> view = textView_Miss3
            3 -> view = textView_Miss4
            else -> view = textView_Miss5
        }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(createJudgeAction(view))
        animatorSet.start()
        missActionCount += 1
        missActionCount = missActionCount % 5
    }

    fun FlickAction() {
        if (flickAnimatorSet != null) {
            //println("flickAnimatorSet.isRunning="+flickAnimatorSet.isRunning)
            if (flickAnimatorSet.isRunning) {
                //println("cancel")
                flickAnimatorSet.cancel()
            }
        }
        flickCircle.alpha = 0.0f
        val anime1 = ObjectAnimator.ofFloat(flickCircle, "scaleX", 0.5f, 1.2f)
        anime1.duration = 150
        val anime2 = ObjectAnimator.ofFloat(flickCircle, "scaleY", 0.5f, 1.2f)
        anime2.duration = 150
        val anime3 = ObjectAnimator.ofFloat(flickCircle, "alpha", 0.0f, 1.0f)
        anime3.duration = 50
        val animatorSet1 = AnimatorSet()
        animatorSet1.playTogether(anime1, anime2, anime3)
        val anime4 = ObjectAnimator.ofFloat(flickCircle, "alpha", 1.0f, 1.0f)
        anime4.duration = 300
        val anime5 = ObjectAnimator.ofFloat(flickCircle, "alpha", 1.0f, 0.0f)
        anime5.duration = 100
        val flickAnimatorSet = AnimatorSet()
        flickAnimatorSet.playSequentially(animatorSet1, anime4, anime5)
        flickAnimatorSet.start()
    }

    private fun createJudgeAction(view: TextView): ArrayList<Animator> {
        view.alpha = 1.0f
        val animatorList: ArrayList<Animator> = arrayListOf()
        var anime = ObjectAnimator.ofFloat(view, "y", animationStartYsa+dotCircle.y, animationEndYsa+dotCircle.y)
        anime.duration = 300
        animatorList.add(anime)
        anime = ObjectAnimator.ofFloat(view, "y", animationEndYsa+dotCircle.y, animationEndYsa+dotCircle.y)
        anime.duration = 500
        animatorList.add(anime)
        anime = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f)
        anime.duration = 100
        animatorList.add(anime)

        return animatorList
    }

    fun ComboAction() {
        if (noteData.score.comboCounter == 0) {
            textView_combo.isInvisible = true
            return
        }
        if (noteData.score.comboCounter < 5) {
            return
        }
        textView_combo.setText("${noteData.score.comboCounter} combo ")
        if (noteData.score.comboCounter > 5) {
            return
        }
        //5 comboのときだけアニメーション
        textView_combo.isInvisible = false
        val anime = ObjectAnimator.ofFloat(textView_combo, "y", comboanimeStartYsa+dotCircle.y, comboanimeEndYsa+dotCircle.y)
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
        println("back")
        //super.onBackPressed()
        val intent: Intent = Intent(applicationContext, Activity_GameMenu::class.java)
        startActivityForResult(intent, 1001)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //if(editFlg!=0){ return }
        //editFlg=1
        println(currentFocus)
        if (hasFocus && editText == currentFocus) {
            Handler().postDelayed(10) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
            }
        }
    }

    fun menuButton(view: View) {
        println("push menu")
        val intent: Intent = Intent(applicationContext, Activity_GameMenu::class.java)
        startActivityForResult(intent, 1001)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        println("touch")
        Handler().postDelayed(10) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPause() {
        super.onPause()
        println("onPause!!")
        paused = true
        timerKill = true
        if( cacheExoPlayer == null ){ return }
        cacheExoPlayer!!.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        println("resume")
        if (paused) {
            timerKill = false
            mHandler.post(timerRun)
            //ゲージ
            progressBar.setProgress((noteData.score.borderScore * 10).toInt())
            val view = window.decorView
            //最下のボタン非表示
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
            if( cacheExoPlayer == null ){ return }
            cacheExoPlayer!!.playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GLOBAL.CurrentNOTES = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // startActivityForResult()の際に指定した識別コードとの比較
        when(requestCode){
            1001 -> {
                // 返却結果ステータスとの比較
                when (resultCode) {
                    10 -> finish()
                    11 -> {
                        noteData.noteReset()
                        textView_combo.isInvisible = true
                        textView_Score.setText("Score: 0 ")
                        if(cacheExoPlayer != null){
                            cacheExoPlayer!!.seekTo(0)
                        }
                    }
                }
                // ジャッジオフセットの取得
                USERDATA.JudgeOffset[selectLevel.sqlID]?.let {
                    judgeOffset = Double(it)
                }
                textView_judgeOffset.setText("offset: %.2f".format(judgeOffset))

                playerviewHeight = playerviewOriginalHeight * USERDATA.PlayerHeightPer / 10000
                playerView.layoutParams.height = playerviewHeight

            }
            1003 -> onCreate2()
            else -> finish()
        }
    }
}
