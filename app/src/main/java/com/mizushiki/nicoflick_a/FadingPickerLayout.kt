package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.FrameLayout

/** Fade only picker content, leaving the scene behind it untouched. */
class FadingPickerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val maskPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maskPaint.shader = if (h > 0) {
            val edge = minOf(24f * resources.displayMetrics.density, h / 2f) / h
            LinearGradient(0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.TRANSPARENT, Color.BLACK, Color.BLACK, Color.TRANSPARENT),
                floatArrayOf(0f, edge, 1f - edge, 1f), Shader.TileMode.CLAMP)
        } else null
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return
        // Isolate blending so DST_IN never erases the background video.
        val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        super.dispatchDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), maskPaint)
        canvas.restoreToCount(layer)
    }
}
