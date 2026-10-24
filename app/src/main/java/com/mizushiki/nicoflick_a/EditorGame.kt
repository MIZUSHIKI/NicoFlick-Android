package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlin.math.abs

/** Local test session. Owns no Activity, result navigation, global score or server API. */

// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
class EditorGame(
    context: Context,
    private val player: SimpleExoPlayer,
    private val document: EditorDocument,
    private val rateFactor: Double,
    initialAutoPlay: Boolean,
    private val onClose: (Boolean) -> Unit
) : FrameLayout(context) {
    private val density = resources.displayMetrics.density
    private val notes = Notes().apply {
        gameviewWidth = resources.displayMetrics.widthPixels.toDouble()
        flickPointX = gameviewWidth * 50 / 375
        noteAnalyze(document.export(EditorDocument.Export.DRAFT), document.speed,
            if (document.level == 100 && document.characters.any { it.note }) 1 else document.level)
    }.notes
    private var speed = document.speed
    private var offset = document.offsetMs
    private val history = TextView(context).apply {
        textSize = 11f; setTextColor(Color.WHITE); setPadding(dp(6), dp(6), 0, 0)
    }
    private var combo = 0
    private var judgement = ""
    private var closed = false
    private lateinit var testSeek: SeekBar
    private val auto = CheckBox(context).apply {
        text = "自動フリック"; setTextColor(Color.WHITE); isChecked = initialAutoPlay
    }
    private fun dp(value: Int) = (value * density).toInt()
    private fun time() = player.currentPosition / 1000.0 - 0.05 - rateFactor * (1.0 - player.playbackParameters.speed)
    private val lane = object : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            val now = time()
            val target = width * 50f / 375f
            val xps = (width - target) * speed / 300f
            paint.color = Color.rgb(230, 255, 185)
            canvas.drawRect(0f, height * .4f, width.toFloat(), height * .8f, paint)
            paint.color = Color.DKGRAY; paint.strokeWidth = dp(2).toFloat()
            canvas.drawLine(target, height * .35f, target, height * .85f, paint)
            paint.textAlign = Paint.Align.CENTER; paint.textSize = dp(26).toFloat()
            for (note in notes) {
                val x = target + ((note.time - now) * xps).toFloat()
                if (x < -dp(40) || x > width + dp(40)) continue
                paint.color = if (note.flicked) Color.LTGRAY else if (note.isFlickable) Color.BLACK else Color.GRAY
                canvas.drawText(note.word, x, height * .68f, paint)
            }
            paint.color = Color.rgb(130, 40, 190); paint.textSize = dp(16).toFloat()
            canvas.drawText(judgement + "  " + combo + " combo", width / 2f, height * .25f, paint)
        }
    }
    private val listener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) close()
        }
    }
    private val tick = object : Runnable {
        override fun run() {
            if (closed) return
            if (player.isPlaying) {
                val now = time()
                for (note in notes) {
                    if (!note.isFlickable || note.flicked) continue
                    if (auto.isChecked && now >= note.time) judge(note, "Great")
                    else if (now - note.time > .4) judge(note, "Miss")
                }
            }
            if (player.duration > 0) testSeek.progress = (player.currentPosition * 10000 / player.duration).toInt()
            lane.invalidate(); postOnAnimation(this)
        }
    }
    init {
        isClickable = true
        setBackgroundColor(Color.argb(100, 0, 0, 0))
        val root = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        addView(root, LayoutParams(-1, -1))
        val preview = FrameLayout(context)
        root.addView(preview, LinearLayout.LayoutParams(-1, 0, 0.4f))
        val heading = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val back = Button(context).apply {
            text = "終了"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(255, 120, 120))
            setOnClickListener { close() }
        }
        val pause = Button(context).apply {
            text = "再生 / 停止"; setOnClickListener { player.playWhenReady = !player.playWhenReady }
        }
        heading.addView(pause, LinearLayout.LayoutParams(0, dp(44), 1f))
        heading.addView(back, LinearLayout.LayoutParams(0, dp(44), 1f))
        preview.addView(heading, LayoutParams(dp(210), dp(44), android.view.Gravity.BOTTOM or android.view.Gravity.RIGHT).apply { bottomMargin = dp(36) })
        preview.addView(auto, LayoutParams(dp(140), dp(44), android.view.Gravity.BOTTOM or android.view.Gravity.LEFT).apply { bottomMargin = dp(36) })
        preview.addView(history, LayoutParams(dp(195), -1, android.view.Gravity.TOP or android.view.Gravity.LEFT).apply { bottomMargin = dp(84) })
        val seek = SeekBar(context).apply { max = 10000; contentDescription = "テスト再生位置" }
        testSeek = seek
        preview.addView(seek, LayoutParams(-1, dp(32), android.view.Gravity.BOTTOM))
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser && player.duration > 0) {
                    player.seekTo(player.duration * value / 10000)
                    resetTimeline(player.duration * value / 10000 / 1000.0)
                }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE) }
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 0.6f))
        fun slider(label: String, maxValue: Int, value: Int, change: (Int) -> String) {
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            val text = TextView(context).apply { this.text = label; textSize = 13f; setTextColor(Color.BLACK); gravity = android.view.Gravity.CENTER_VERTICAL }
            row.addView(text, LinearLayout.LayoutParams(dp(122), dp(44)))
            val bar = SeekBar(context).apply { max = maxValue; progress = value; contentDescription = label }
            row.addView(bar, LinearLayout.LayoutParams(0, dp(44), 1f))
            bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) text.text = change(value)
                }
                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) {}
            })
            body.addView(row)
        }
        slider("Speed: $speed", 450, speed - 50) { value -> speed = value + 50; "Speed: $speed" }
        slider("Offset: ${offset}ms", 1000, 500) { value ->
            val updated = (document.offsetMs + value - 500).coerceIn(-600000, 600000)
            val delta = (updated.toLong() - offset) / 1000.0
            notes.forEach { it.time += delta }
            offset = updated; resetTimeline(time())
            "Offset: ${offset}ms"
        }
        slider("再生速度: ×${player.playbackParameters.speed}", 19, (player.playbackParameters.speed * 10).toInt() - 1) { value ->
            val rate = (value + 1) / 10f
            player.setPlaybackParameters(com.google.android.exoplayer2.PlaybackParameters(rate))
            "再生速度: ×%.1f".format(rate)
        }
        body.addView(lane, LinearLayout.LayoutParams(-1, dp(76)))
        val keyboard = MyKeyboard(context)
        keyboard.getChildAt(0).visibility = View.GONE
        keyboard.onKeylistener = { word -> flick(word) }
        body.addView(keyboard, LinearLayout.LayoutParams(-1, 0, 1f))
        resetTimeline(time())
        player.addListener(listener); postOnAnimation(tick)
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.playWhenReady = true
    }
    private fun resetTimeline(now: Double) {
        notes.forEach { it.flicked = it.time < now - .4 }
        combo = 0; judgement = ""; history.text = ""
    }
    private fun judge(note: Note, value: String) {
        history.text = ("%.3fs  %s  %s".format(time() - note.time, note.word, value) + "\n" + history.text)
            .lineSequence().take(10).joinToString("\n")
        note.flicked = true; judgement = value
        combo = if (value == "Miss" || value == "Bad") 0 else combo + 1
        when (value) {
            "Great", "Good" -> SEAudio.okJinglePlay()
            "Safe" -> SEAudio.safeJinglePlay()
            "Bad" -> SEAudio.badJinglePlay()
        }
    }
    private fun flick(word: String) {
        if (closed || auto.isChecked || !player.isPlaying) return
        val now = time()
        val note = notes.filter { it.isFlickable && !it.flicked && it.time - now in -.4.. .3 }
            .minByOrNull { abs(it.time - now) } ?: return
        val diff = abs(note.time - now)
        val wordJudge = note.judgeWord(word)
        judge(note, when {
            wordJudge == Note.BAD -> "Bad"
            wordJudge == Note.SAFE -> "Safe"
            diff <= .08 -> "Great"
            diff <= .2 -> "Good"
            else -> "Safe"
        })
    }
    fun close() {
        if (closed) return
        closed = true; removeCallbacks(tick); player.removeListener(listener)
        player.playWhenReady = false
        if (speed != document.speed || offset != document.offsetMs) {
            document.setMetadata(mapOf("Speed" to speed.toString(), "Offset" to offset.toString()))
        }
        onClose(auto.isChecked)
    }
    override fun onDetachedFromWindow() {
        close()
        super.onDetachedFromWindow()
    }
}
