package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max

/** Virtualized lyric grid: scroll in both axes, pinch to zoom, tap/double-tap/hold to edit. */

// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
class EditorTimeline(context: Context) : View(context) {
    var document: EditorDocument? = null
    var onCursor: (() -> Unit)? = null
    var onSeek: ((Int) -> Unit)? = null
    var onEdit: (() -> Unit)? = null
    var playbackMs = -1L
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cells = mutableListOf<RectF>()
    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    private var contentWidth = 0f
    private var contentHeight = 0f
    private val scaler = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val old = zoom; zoom = (zoom * detector.scaleFactor).coerceIn(0.5f, 2.5f)
            panX = (panX + detector.focusX) * zoom / old - detector.focusX
            panY = (panY + detector.focusY) * zoom / old - detector.focusY
            rebuild(); return true
        }
    })
    private val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onScroll(a: MotionEvent?, b: MotionEvent, dx: Float, dy: Float): Boolean {
            if (scaler.isInProgress) return true
            panX += dx; panY += dy; clampPan(); invalidate(); return true
        }
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean { select(e); performClick(); return true }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            select(e); document?.characters?.get(document!!.cursor)?.let { if (it.timeMs >= 0) onSeek?.invoke(it.timeMs + document!!.offsetMs) }
            return true
        }
        override fun onLongPress(e: MotionEvent) { select(e); onEdit?.invoke() }
    })
    init { setBackgroundColor(Color.WHITE); isFocusable = true; contentDescription = "歌詞編集。タップで選択、長押しで編集、ダブルタップで再生位置へ移動" }
    override fun performClick(): Boolean { super.performClick(); return true }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        parent?.requestDisallowInterceptTouchEvent(true)
        scaler.onTouchEvent(event); if (!scaler.isInProgress) gestures.onTouchEvent(event)
        return true
    }
    private fun select(event: MotionEvent) {
        val x = event.x + panX; val y = event.y + panY
        val index = cells.indexOfFirst { it.contains(x, y) }
        if (index >= 0) { document?.cursor = index; onCursor?.invoke(); invalidate() }
    }
    fun rebuild(followCursor: Boolean = false) {
        cells.clear()
        var column = 0; var row = 0; var maxColumn = 1
        val cw = 30 * density * zoom; val ch = 43 * density * zoom
        document?.characters?.forEach {
            cells.add(RectF(8 * density + column * cw, 8 * density + row * ch,
                8 * density + (column + 1) * cw - density, 8 * density + (row + 1) * ch - density))
            column++; maxColumn = max(maxColumn, column)
            if (it.word == "\n") { row++; column = 0 }
        }
        contentWidth = maxColumn * cw + 16 * density
        contentHeight = (row + 2) * ch + 16 * density
        clampPan()
        if (followCursor) cells.getOrNull(document?.cursor ?: 0)?.let {
            if (it.left < panX || it.right > panX + width) panX = max(0f, it.left - width / 3)
            if (it.top < panY || it.bottom > panY + height) panY = max(0f, it.top - height / 3)
            clampPan()
        }
        invalidate()
    }
    private fun clampPan() { panX = panX.coerceIn(0f, max(0f, contentWidth - width)); panY = panY.coerceIn(0f, max(0f, contentHeight - height)) }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { super.onSizeChanged(w, h, oldw, oldh); rebuild() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val doc = document ?: return
        canvas.save(); canvas.translate(-panX, -panY)
        for ((index, rect) in cells.withIndex()) {
            if (rect.bottom < panY || rect.top > panY + height || rect.right < panX || rect.left > panX + width) continue
            val character = doc.characters[index]
            paint.style = Paint.Style.FILL
            paint.color = if (character.note) Color.rgb(255, 204, 204) else Color.rgb(255, 255, 205)
            canvas.drawRect(rect, paint)
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.5f * density
            paint.color = if (character.timeMs >= 0) Color.RED else Color.LTGRAY
            canvas.drawRect(rect, paint)
            if (index == doc.cursor) { paint.color = Color.rgb(0, 210, 90); paint.strokeWidth = 2.5f * density; canvas.drawRect(rect, paint) }
            paint.style = Paint.Style.FILL; paint.textAlign = Paint.Align.CENTER; paint.textSize = 21 * density * zoom
            paint.color = if (character.timeMs >= 0 && character.timeMs.toLong() + doc.offsetMs <= playbackMs) Color.RED else Color.BLACK
            canvas.drawText(if (character.word == "\n") "↵" else character.word, rect.centerX(), rect.top + 25 * density * zoom, paint)
            paint.textSize = 7 * density * zoom; paint.color = Color.DKGRAY
            if (character.timeMs >= 0) canvas.drawText(EditorDocument.formatTime(character.timeMs.toLong()).drop(1).dropLast(1), rect.centerX(), rect.bottom - 3 * density * zoom, paint)
        }
        canvas.restore()
    }
}
