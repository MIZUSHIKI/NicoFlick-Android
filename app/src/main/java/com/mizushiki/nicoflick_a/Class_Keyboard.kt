package com.mizushiki.nicoflick_a

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginTop
import kotlin.math.abs


//レイアウトカスタム
class MyKeyboard(context: Context) : LinearLayout(context) {

    // to get dp unit
    val dp = resources.displayMetrics.density
    val sp = resources.displayMetrics.scaledDensity

    var padding_left:Int = 0
    var padding_right:Int = 0
    var padding_top:Int = 0//(150 * dp).toInt()
    var padding_bottom:Int = 0

    private var shitaLayout:FrameLayout

    private var bt_AA:KeyButton
    private var bt_KA:KeyButton
    private var bt_SA:KeyButton
    private var bt_TA:KeyButton
    private var bt_NA:KeyButton
    private var bt_HA:KeyButton
    private var bt_MA:KeyButton
    private var bt_YA:KeyButton
    private var bt_RA:KeyButton
    private var bt_WA:KeyButton

    var onKeylistener: (String) -> Unit = {}
        set(value) {
            bt_AA.onKeylistener = value
            bt_KA.onKeylistener = value
            bt_SA.onKeylistener = value
            bt_TA.onKeylistener = value
            bt_NA.onKeylistener = value
            bt_HA.onKeylistener = value
            bt_MA.onKeylistener = value
            bt_YA.onKeylistener = value
            bt_RA.onKeylistener = value
            bt_WA.onKeylistener = value
            field  = value
        }

    init {
        this.orientation = VERTICAL

        val ueLayout = FrameLayout(context)
        val uePara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        uePara.weight = 0.5f
        ueLayout.layoutParams = uePara
        this.addView(ueLayout)

        shitaLayout = FrameLayout(context)
        val shitaPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        shitaPara.weight = 0.5f
        shitaLayout.layoutParams = shitaPara
        this.addView(shitaLayout)

        val wakuView = WakuView(context)
        wakuView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)
        shitaLayout.addView(wakuView)

        val keyboardLayout = LinearLayout(context)
        keyboardLayout.orientation = HORIZONTAL
        keyboardLayout.weightSum = 1.0f
        val keyboardPara = LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)
        keyboardLayout.layoutParams = keyboardPara
        shitaLayout.addView(keyboardLayout)

        val systemRetuLayout = LinearLayout(context)
        val systemRetuPara = LayoutParams(0,LayoutParams.MATCH_PARENT)
        systemRetuPara.weight = 0.2f
        systemRetuLayout.layoutParams = systemRetuPara
        keyboardLayout.addView(systemRetuLayout)

        val aRetuLayout = LinearLayout(context)
        aRetuLayout.orientation = VERTICAL
        aRetuLayout.weightSum = 1.0f
        val aRetuPara = LayoutParams(0,LayoutParams.MATCH_PARENT)
        aRetuPara.weight = 0.2f
        aRetuLayout.layoutParams = aRetuPara
        keyboardLayout.addView(aRetuLayout)

        val kaRetuLayout = LinearLayout(context)
        kaRetuLayout.orientation = VERTICAL
        kaRetuLayout.weightSum = 1.0f
        val kaRetuPara = LayoutParams(0,LayoutParams.MATCH_PARENT)
        kaRetuPara.weight = 0.2f
        kaRetuLayout.layoutParams = kaRetuPara
        keyboardLayout.addView(kaRetuLayout)

        val saRetuLayout = LinearLayout(context)
        saRetuLayout.orientation = VERTICAL
        saRetuLayout.weightSum = 1.0f
        val saRetuPara = LayoutParams(0,LayoutParams.MATCH_PARENT)
        saRetuPara.weight = 0.2f
        saRetuLayout.layoutParams = saRetuPara
        keyboardLayout.addView(saRetuLayout)

        val backRetuLayout = LinearLayout(context)
        val backRetuPara = LayoutParams(0,LayoutParams.MATCH_PARENT)
        backRetuPara.weight = 0.2f
        backRetuLayout.layoutParams = backRetuPara
        keyboardLayout.addView(backRetuLayout)

        bt_AA = KeyButton(context,listOf("あ","い","う","え","お"))
        val aaPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        aaPara.weight = 0.25f
        bt_AA.layoutParams = aaPara
        aRetuLayout.addView(bt_AA)

        bt_TA = KeyButton(context,listOf("た","ち","つ","て","と"))
        val taPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        taPara.weight = 0.25f
        bt_TA.layoutParams = taPara
        aRetuLayout.addView(bt_TA)

        bt_MA = KeyButton(context,listOf("ま","み","む","め","も"))
        val maPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        maPara.weight = 0.25f
        bt_MA.layoutParams = maPara
        aRetuLayout.addView(bt_MA)

        bt_KA = KeyButton(context,listOf("か","き","く","け","こ"))
        val kaPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        kaPara.weight = 0.25f
        bt_KA.layoutParams = kaPara
        kaRetuLayout.addView(bt_KA)

        bt_NA = KeyButton(context,listOf("な","に","ぬ","ね","の"))
        val naPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        naPara.weight = 0.25f
        bt_NA.layoutParams = naPara
        kaRetuLayout.addView(bt_NA)

        bt_YA = KeyButton(context,listOf("や","","ゆ","","よ"))
        val yaPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        yaPara.weight = 0.25f
        bt_YA.layoutParams = yaPara
        kaRetuLayout.addView(bt_YA)

        bt_WA = KeyButton(context,listOf("わ","を","ん"))
        val waPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        waPara.weight = 0.25f
        bt_WA.layoutParams = waPara
        kaRetuLayout.addView(bt_WA)


        bt_SA = KeyButton(context,listOf("さ","し","す","せ","そ"))
        val saPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        saPara.weight = 0.25f
        bt_SA.layoutParams = saPara
        saRetuLayout.addView(bt_SA)

        bt_HA = KeyButton(context,listOf("は","ひ","ふ","へ","ほ"))
        val haPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        haPara.weight = 0.25f
        bt_HA.layoutParams = haPara
        saRetuLayout.addView(bt_HA)

        bt_RA = KeyButton(context,listOf("ら","り","る","れ","ろ"))
        val raPara = LayoutParams(LayoutParams.MATCH_PARENT,0)
        raPara.weight = 0.25f
        bt_RA.layoutParams = raPara
        saRetuLayout.addView(bt_RA)

        setPaddingKeybd()
    }

    fun setPaddingKeybd(left:Int = padding_left, top:Int = padding_top, right:Int = padding_right, bottom:Int = padding_bottom){
        padding_left = left
        padding_top = top
        padding_right = right
        padding_bottom = bottom
        shitaLayout.setPadding(padding_left, padding_top - padding_bottom, padding_right, padding_bottom)
    }
    fun setStrokeSens(sens:Float){
        bt_AA.strokeSens = sens * dp
        bt_KA.strokeSens = sens * dp
        bt_SA.strokeSens = sens * dp
        bt_TA.strokeSens = sens * dp
        bt_NA.strokeSens = sens * dp
        bt_HA.strokeSens = sens * dp
        bt_MA.strokeSens = sens * dp
        bt_YA.strokeSens = sens * dp
        bt_RA.strokeSens = sens * dp
        bt_WA.strokeSens = sens * dp
    }

    class WakuView(context: Context) : View(context){
        private var paint: Paint = Paint()
        override fun onDraw(canvas: Canvas){
            println("canvas.width=${canvas.width}")
            println("canvas.height=${canvas.height}")
            val w = canvas.width
            val h = canvas.height
            paint.strokeWidth = 1.75f
            paint.style = Paint.Style.STROKE
            paint.color = Color.BLACK
            canvas.drawLine( Float(0), Float(h * 0 / 4 +1.75f/2), Float(w), Float(h * 0 / 4 +1.75f/2) ,paint)
            canvas.drawLine( Float(0), Float(h * 1 / 4), Float(w), Float(h * 1 / 4) ,paint)
            canvas.drawLine( Float(0), Float(h * 2 / 4), Float(w), Float(h * 2 / 4) ,paint)
            canvas.drawLine( Float(0), Float(h * 3 / 4), Float(w), Float(h * 3 / 4) ,paint)
            canvas.drawLine( Float(0), Float(h * 4 / 4 -1.75f/2), Float(w), Float(h * 4 / 4 -1.75f/2) ,paint)
            //canvas.drawLine( Float(w * 0 / 5), Float( 0), Float(w * 0 / 5), Float(h) ,paint)
            canvas.drawLine( Float(w * 1 / 5), Float( 0), Float(w * 1 / 5), Float(h) ,paint)
            canvas.drawLine( Float(w * 2 / 5), Float( 0), Float(w * 2 / 5), Float(h) ,paint)
            canvas.drawLine( Float(w * 3 / 5), Float( 0), Float(w * 3 / 5), Float(h) ,paint)
            canvas.drawLine( Float(w * 4 / 5), Float( 0), Float(w * 4 / 5), Float(h) ,paint)
            //canvas.drawLine( Float(w * 5 / 5), Float( 0), Float(w * 5 / 5), Float(h) ,paint)
        }
    }

    class KeyButton(context: Context, var wordList: List<String>) : AppCompatButton(context) {

        private var selectID = 0
        private val dp = resources.displayMetrics.density
        private val sp = resources.displayMetrics.scaledDensity
        private var touchX = 0.0f
        private var touchY = 0.0f
        private var layouted = false

        var strokeSens = 16 * dp

        init {
            setText(wordList[0])
            typeface = ResourcesCompat.getFont(context,R.font.nicokaku_v1)
            textSize = 14 * sp
            this.setBackgroundColor(Color.argb(50,155,155,255))
            includeFontPadding = false
            //this.setPadding(5,5,5,5)
        }

        var onKeylistener: ((String) -> Unit) = {}

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            if (event != null) {
                when(event.action){
                    MotionEvent.ACTION_DOWN -> {
                        touchX = event.x
                        touchY = event.y
                        selectID = 0
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dragX = event.x - touchX
                        val dragY = event.y - touchY
                        if(wordList.size >= 1 && wordList[0] != "" && -strokeSens < dragX && dragX < strokeSens && -strokeSens < dragY && dragY < strokeSens ){
                            if( selectID != 0 ) {
                                selectID = 0
                                this.setText(wordList[selectID])
                                this.gravity = Gravity.CENTER
                            }
                        }else if(wordList.size >= 2 && wordList[1] != ""  && dragX <= -strokeSens && -dragX > abs(dragY) ){
                            if( selectID != 1 ) {
                                selectID = 1
                                this.setText(wordList[selectID])
                                this.gravity = (Gravity.LEFT).or(Gravity.CENTER)
                            }
                        }else if(wordList.size >= 3 && wordList[2] != ""  && dragY <= -strokeSens && -dragY > abs(dragX) ){
                            if( selectID != 2 ) {
                                selectID = 2
                                this.setText(wordList[selectID])
                                this.gravity = (Gravity.TOP).or(Gravity.CENTER)
                            }
                        }else if(wordList.size >= 4 && wordList[3] != ""  && dragX >= strokeSens && dragX > abs(dragY) ) {
                            if( selectID != 3 ) {
                                selectID = 3
                                this.setText(wordList[selectID])
                                this.gravity = (Gravity.RIGHT).or(Gravity.CENTER)
                            }
                        }else if(wordList.size >= 5 && wordList[4] != ""  && dragY >= strokeSens && dragY > abs(dragX) ) {
                            if( selectID != 4 ) {
                                selectID = 4
                                this.setText(wordList[selectID])
                                this.gravity = (Gravity.BOTTOM).or(Gravity.CENTER)
                            }
                        }
                        println("bt xy= ${dragX},${dragY} - ${strokeSens} - ${selectID} / ${(this.height - dragY)}")
                    }
                    MotionEvent.ACTION_UP -> {
                        onKeylistener(wordList[selectID])
                        this.setText(wordList[0])
                        this.gravity = Gravity.CENTER
                    }
                }
            }
            return super.onTouchEvent(event)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            if( layouted ){ return }
            println("aa ${this.width} ${this.height} ${this.textSize}")
            val jgpad = ( (this.height - this.textSize) / 4 ).toInt()
            val sypad = ((this.width - this.textSize * 1.15 ) / 2).toInt() - jgpad
            this.setPadding( sypad, jgpad, sypad, jgpad )
        }
    }

}
