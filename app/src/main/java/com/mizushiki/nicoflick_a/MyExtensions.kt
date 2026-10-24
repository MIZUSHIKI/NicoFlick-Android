package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.os.Handler
import android.text.Html
import android.text.Spanned
import android.util.AttributeSet
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.eclipsesource.json.Json
import com.squareup.picasso.Picasso

import kotlinx.coroutines.*
import kotlinx.coroutines.Runnable
import java.nio.channels.FileLock
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


// String
var String.isHiragana:Boolean
    get() = this.matches("^[\\u3040-\\u309F]+$".toRegex())
    set(value) {}
var String.isKatakana:Boolean
    get() = this.matches("^[\\u30A0-\\u30FF]+$".toRegex())
    set(value) {}
fun String.toKatakana():String{
    var retStr = ""
    for( c in this ){
        if( c >= 0x3041.toChar() && c <= 0x3096.toChar() ){
            retStr += (c+96).toString()
        } else {
            retStr += c.toString()
        }
    }
    return retStr
}
fun String.toHiragana():String{
    var retStr = ""
    for( c in this ){
        if( c >= 0x30A1.toChar() && c <= 0x30F6.toChar() ){
            retStr += (c-96).toString()
        } else {
            retStr += c.toString()
        }
    }
    return retStr
}

fun String.secondsToTimetag(seconds:Double, noBrackets:Boolean = false, dot:Boolean = false) : String {
    if( seconds == -0.001 ){
        if( noBrackets ){
            if( dot ){
                return "**:**.**"
            }else {
                return "**:**:**"
            }
        }else {
            if( dot ){
                return "[**:**.**]"
            }else {
                return "[**:**:**]"
            }
        }
    }
    var format = if(dot) "%02d:%02d.%02d" else "%02d:%02d:%02d"
    if( !noBrackets ){
        format = "[${format}]"
    }
    val rmirisec = (Math.round(seconds * 100)).toInt() * 10
    return String.format( format, Int(rmirisec/60000), Int((rmirisec % 60000) / 1000), Int((rmirisec % 1000) / 10) )
}
fun String.secondsToDotTimetag(seconds:Double, noBrackets:Boolean = false) : String {
    if( seconds == -0.001 ){
        if( noBrackets ){
            return "**:**.**"
        }else {
            return "[**:**.**]"
        }
    }
    val format = if(noBrackets) "%02d:%02d.%02d" else "[%02d:%02d.%02d]"
    val rmirisec = (Math.round(seconds * 100)).toInt() * 10
    return String.format( format, Int(rmirisec/60000), Int((rmirisec % 60000) / 1000), Int((rmirisec % 1000) / 10) )
}
fun String.timetagToSeconds() : Double {
    val ans:ArrayList<String> = arrayListOf()
    if( this.pregMatche("\\[(\\d\\d)\\:(\\d\\d)[\\:|\\.](\\d\\d)\\]", matche= ans) ){
        return Double(ans[1])*60 + Double(ans[2]) + Double(ans[3])/100
    }
    return -0.001
}
fun String.timeToSec(FailureValue:Int = -1) : Int {
    val ans:ArrayList<String> = arrayListOf()
    if( this.pregMatche("(\\d+)\\:(\\d+)", matche= ans) ){
        return Int(ans[1])*60 + Int(ans[2])
    }
    return FailureValue
}
val String.isDotTimetag : Boolean
    get() = this.pregMatche("\\[[\\d\\*][\\d\\*]\\:[\\d\\*][\\d\\*]\\.[\\d\\*][\\d\\*]\\]")


fun String.pregReplace(pattern:String, with:String): String {
    return Regex(pattern,RegexOption.DOT_MATCHES_ALL).replace(this, with)
}
fun String.pregMatche(pattern: String): Boolean {
    return Regex(pattern,RegexOption.DOT_MATCHES_ALL).containsMatchIn(this)
}
//最初のマッチ（結果,括弧１,括弧２,・・・）を返す
fun String.pregMatche(pattern: String, matche: ArrayList<String>): Boolean {
    val boo = Regex(pattern,RegexOption.DOT_MATCHES_ALL).find(this)
    if(boo != null){
        for( i in 0 until matche.size ){
            matche.removeAt(0)
        }
        matche.addAll(boo.groupValues)
        return true
    }
    return false
}
 //すべてのマッチ（結果,括弧１,括弧２,・・・）を平配列（結果,括弧１,括弧２,・・・,結果,括弧１,括弧２,・・・）にして返す
fun String.pregMatches(pattern: String, matches: ArrayList<String>): Boolean {
    val boo = Regex(pattern,RegexOption.DOT_MATCHES_ALL).findAll(this)
    if(boo != null){
        for( i in 0 until matches.size ){
            matches.removeAt(0)
        }
        for( bo in boo ){
            matches.addAll(bo.groupValues)
        }
        return true
    }
    return false
}
fun String.pregMatche_strings(pattern: String): ArrayList<String> {
    val boo = Regex(pattern,RegexOption.DOT_MATCHES_ALL).findAll(this)
    val matches = arrayListOf<String>()
    if(boo != null){
        for( bo in boo ){
            matches.addAll(bo.groupValues)
        }
        return matches
    }
    return matches
}
fun String.pregMatches(pattern: String): MutableList<String> {
    return Regex(pattern,RegexOption.DOT_MATCHES_ALL).find(this)?.groupValues!!.toMutableList()
}
fun String.pregMatche_firstString(pattern: String):String {
    return Regex(pattern,RegexOption.DOT_MATCHES_ALL).find(this)?.groupValues?.get(1) ?: ""
}
fun String(p:Int):String{
    return p.toString()
}
fun String(p:Float):String{
    return p.toString()
}
fun String(p:Double):String{
    return p.toString()
}
fun String(p:String):String{
    return p//.toString()
}
fun String(p:Char):String{
    return p.toString()
}


//Int
fun Int(p:Int):Int{
    return p//.toInt()
}
fun Int(p:Float):Int{
    return p.toInt()
}
fun Int(p:Double):Int{
    return p.toInt()
}
fun Int(p:String):Int{
    return p.toInt()
}


//Float
fun Float(p:Int):Float{
    return p.toFloat()
}
fun Float(p:Float):Float{
    return p//.toFloat()
}
fun Float(p:Double):Float{
    return p.toFloat()
}
fun Float(p:String):Float{
    return p.toFloat()
}


//Double
fun Double(p:Int):Double{
    return p.toDouble()
}
fun Double(p:Float):Double{
    return p.toDouble()
}
fun Double(p:Double):Double{
    return p//.toDouble()
}
fun Double(p:String):Double{
    return p.toDouble()
}


//View
var View.isHidden: Boolean
    get() = this.isInvisible
    set(value) { this.isInvisible = value }
var View.center: PointF
    get() = PointF(this.x + this.width/2, this.y + this.height/2)
    set(value) {
        this.x = value.x - this.width/2
        this.y = value.y - this.height/2
    }
var View.centerX: Float
    get() = this.x + this.width/2
    set(value) {
        this.x = value - this.width/2
    }
var View.centerY: Float
    get() = this.y + this.height/2
    set(value) {
        this.y = value - this.height/2
    }
/*
fun View.convertToBitmap(): Bitmap {
    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(measureSpec, measureSpec)
    layout(0, 0, measuredWidth, measuredHeight)
    val r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    r.eraseColor(Color.TRANSPARENT)
    val canvas = Canvas(r)
    draw(canvas)
    return r
}
 */

//Viewカスタム
// Viewを継承したクラス
class SlashShadeView(context: Context,
                     var slashColor:Int = Color.WHITE,
                     var slashSpace:Float = 4.0f,
                     var LineWidth:Float = 1.75f) : View(context) {
    private var paint: Paint = Paint()

    override fun onDraw(canvas: Canvas){
        if( Int(slashSpace) < 1 ){
            return
        }
        // println("canvas.width=${canvas.width}")
        // println("canvas.height=${canvas.height}")
        val w = canvas.width
        val h = canvas.height
        // ペイントする色の設定
        paint.color = slashColor

        // ペイントストロークの太さを設定
        paint.strokeWidth = LineWidth
        // Styleのストロークを設定する
        paint.style = Paint.Style.STROKE

        for( y in 0..(w+h)/Int(slashSpace) ){
            canvas.drawLine( Float(0), Float(y*slashSpace), Float(w), Float(y*slashSpace-w) ,paint)
        }
    }
}
//TextView
var TextView.rectF: RectF
    get() = RectF(this.x,this.y,this.x+this.width,this.y+this.height)
    set(value) {
        this.x = value.left
        this.y = value.top
        this.width = value.width().toInt()
        this.height = value.height().toInt()
    }

//Nicokaku FontInfo定数
val FontInfo_NicoKaku_wcRange = FontInfo_NicoKaku1.wcRanges + FontInfo_NicoKaku2.wcRanges

fun HtmlNicokakuMixFixText(string: String) : Spanned {
    var htmlText = string
    var checkedWord = ""
    for ( c in string.toCharArray() ){
        if( checkedWord.indexOf(c) != -1 ){ continue }
        checkedWord += c
        val code = c.hashCode()
        var check = false
        for ( i in 0 until FontInfo_NicoKaku_wcRange.size / 2  ){
            if( FontInfo_NicoKaku_wcRange[i*2] <= code && code <= FontInfo_NicoKaku_wcRange[i*2]+FontInfo_NicoKaku_wcRange[i*2+1]-1 ){
                check = true
                break
            }
        }
        if( !check ){
            htmlText = htmlText.pregReplace(
                "(${Regex.escape(c.toString())})",
                "<font face=\'sans-serif\'><small><b>$1<b></small></font>"
            )
        }
    }
    return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
}
//TextViewカスタム
class OutlineTextView(context: Context?, val firstRect:RectF) : AppCompatTextView(context) {
    var strokeWidth = 4.0f
    var strokeColor = Color.rgb(200,200,200)

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas)
        //canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val text = text as String
        val textColor = currentTextColor
        val textSize = textSize.toFloat()
        val paint = Paint()
        paint.setAntiAlias(true)
        paint.setTextSize(textSize)
        paint.setTextAlign(Align.LEFT)
        paint.setStrokeWidth(strokeWidth)
        paint.setStrokeJoin(Paint.Join.ROUND)
        paint.setStrokeCap(Paint.Cap.ROUND)
        paint.setColor(strokeColor)
        paint.setStyle(Paint.Style.STROKE)
        val metrics = paint.fontMetrics
        val posX: Float = 0.0f
        // Use the same baseline for all glyphs so small kana retain their natural position.
        val posY: Float = firstRect.height() / 2 - (metrics.ascent + metrics.descent) / 2
        canvas.drawText(text, posX, posY, paint)
        paint.setStrokeWidth(0.0f)
        paint.setColor(textColor)
        paint.setStyle(Paint.Style.FILL)
        canvas.drawText(text, posX, posY, paint)
        //println("textBounds.height()="+textBounds.height())
    }
}
class DecolationLabel @JvmOverloads constructor (context: Context, val attrs: AttributeSet? = null, val defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    // Viewのalpha・visibilityやクリップに影響されず、文字と装飾を画像化する。
    fun createTextBitmap(inset: Int): Bitmap {
        check(width > 0 && height > 0) { "レイアウト確定後に画像化してください" }
        val bitmap = Bitmap.createBitmap(
            width + inset * 2, height + inset * 2, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.translate(inset.toFloat(), inset.toFloat())
        drawDecoratedText(canvas)
        return bitmap
    }

    var strokeWidth:Float
    var strokeColor:Int
    var strokeShadowX: Float
    var strokeShadowY: Float
    var strokeShadowColor:Int

    init {
        val a = context.obtainStyledAttributes(attrs,R.styleable.DecolationLabel,defStyleAttr,0)
        strokeWidth = a.getFloat(R.styleable.DecolationLabel_stroke_size, 0.0f )
        strokeColor = a.getColor(R.styleable.DecolationLabel_stroke_color, Color.argb(0,0,0,0) )
        strokeShadowX = a.getFloat(R.styleable.DecolationLabel_stroke_shadow_x, 0.0f )
        strokeShadowY = a.getFloat(R.styleable.DecolationLabel_stroke_shadow_y, 0.0f )
        strokeShadowColor = a.getColor(R.styleable.DecolationLabel_stroke_shadow_color, Color.argb(0,0,0,0) )
        a.recycle()

        // 指定のない既存ラベルは、従来どおり中央寄せにする。
        val viewAttrs = context.obtainStyledAttributes(
            attrs, intArrayOf(android.R.attr.gravity), defStyleAttr, 0
        )
        gravity = viewAttrs.getInt(0, android.view.Gravity.CENTER)
        viewAttrs.recycle()
    }

    // 装飾の余白を測定と描画の両方に反映する。XMLのpaddingには加算する。
    private fun decorationInset(shadowOffset: Float): Int {
        val hasOutline = Color.alpha(strokeColor) != 0
        val hasShadow = Color.alpha(strokeShadowColor) != 0
        if (!hasOutline && !hasShadow) return 0
        val shadow = if (hasShadow) shadowOffset.coerceAtLeast(0f) else 0f
        return kotlin.math.ceil(strokeWidth.coerceAtLeast(0f) / 2f + shadow).toInt() + 1
    }

    override fun getCompoundPaddingLeft(): Int =
        super.getCompoundPaddingLeft() + decorationInset(-strokeShadowX)

    override fun getCompoundPaddingRight(): Int =
        super.getCompoundPaddingRight() + decorationInset(strokeShadowX)

    override fun getCompoundPaddingTop(): Int =
        super.getCompoundPaddingTop() + decorationInset(-strokeShadowY)

    override fun getCompoundPaddingBottom(): Int =
        super.getCompoundPaddingBottom() + decorationInset(strokeShadowY)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 斜体などで文字の実際の輪郭が標準の文字幅からはみ出す場合も確保する。
        val bounds = Rect()
        val label = text.toString()
        paint.getTextBounds(label, 0, label.length, bounds)
        val desiredWidth = maxOf(
            measuredWidth, bounds.width() + compoundPaddingLeft + compoundPaddingRight
        )
        val desiredHeight = maxOf(
            measuredHeight, bounds.height() + compoundPaddingTop + compoundPaddingBottom
        )
        setMeasuredDimension(
            View.resolveSizeAndState(desiredWidth, widthMeasureSpec, measuredState),
            View.resolveSizeAndState(
                desiredHeight, heightMeasureSpec,
                measuredState shl View.MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawDecoratedText(canvas)
    }

    private fun drawDecoratedText(canvas: Canvas?) {
        val text = text.toString()
        val textColor = currentTextColor
        val textSize = textSize.toFloat()
        val paint = Paint()
        paint.setTypeface(typeface)
        paint.setAntiAlias(true)
        paint.setTextSize(textSize)
        paint.setStrokeWidth(strokeWidth)
        paint.setStrokeJoin(Paint.Join.ROUND)
        paint.setStrokeCap(Paint.Cap.ROUND)
        paint.setStyle(Paint.Style.STROKE)
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val isRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL
        val isTextRtl = layout?.getParagraphDirection(0) == -1
        val horizontalGravity = when (textAlignment) {
            View.TEXT_ALIGNMENT_CENTER -> android.view.Gravity.CENTER_HORIZONTAL
            View.TEXT_ALIGNMENT_VIEW_START ->
                if (isRtl) android.view.Gravity.RIGHT else android.view.Gravity.LEFT
            View.TEXT_ALIGNMENT_VIEW_END ->
                if (isRtl) android.view.Gravity.LEFT else android.view.Gravity.RIGHT
            View.TEXT_ALIGNMENT_TEXT_START ->
                if (isTextRtl) android.view.Gravity.RIGHT else android.view.Gravity.LEFT
            View.TEXT_ALIGNMENT_TEXT_END ->
                if (isTextRtl) android.view.Gravity.LEFT else android.view.Gravity.RIGHT
            else -> android.view.Gravity.getAbsoluteGravity(gravity, layoutDirection) and
                android.view.Gravity.HORIZONTAL_GRAVITY_MASK
        }
        val left = compoundPaddingLeft.toFloat()
        val right = (width - compoundPaddingRight).toFloat()
        val top = compoundPaddingTop.toFloat()
        val bottom = (height - compoundPaddingBottom).toFloat()
        val posX = when (horizontalGravity) {
            android.view.Gravity.LEFT -> left - textBounds.left
            android.view.Gravity.RIGHT -> right - textBounds.right
            else -> (left + right - textBounds.width()) / 2f - textBounds.left
        }
        val posY = when (gravity and android.view.Gravity.VERTICAL_GRAVITY_MASK) {
            android.view.Gravity.TOP -> top - textBounds.top
            android.view.Gravity.BOTTOM -> bottom - textBounds.bottom
            else -> (top + bottom - textBounds.height()) / 2f - textBounds.top
        }

        if(strokeShadowColor != Color.argb(0,0,0,0)){
            paint.setColor(strokeShadowColor)
            canvas!!.drawText(text, posX + strokeShadowX, posY + strokeShadowY, paint)
        }
        // println(strokeShadowColor)
        paint.setColor(strokeColor)
        canvas!!.drawText(text, posX, posY, paint)

        paint.setStrokeWidth(0.0f)
        paint.setColor(textColor)
        paint.setStyle(Paint.Style.FILL)
        //posY -= textSize / 50
        canvas.drawText(text, posX, posY, paint)
        //println("textBounds.height()="+textBounds.height())
    }
}

class ChainTextView @JvmOverloads constructor (context: Context, val attrs: AttributeSet? = null, val defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {
    private  var finished = false
    private  var breaker = false

    //private val timer = Timer()
    private val mHandler = Handler()

    enum class chainOption {
        callFinish, callStart, callStartFinish
    }
    enum class chainState {
        finish, start
    }
    fun StartNumberRoll(duration:Int, option: chainOption = chainOption.callFinish, callback: (chainState) -> Unit ){
        this.isVisible = true
        val text = this.text
        val rawText = text
        var remainingText = text
        this.setText("")

        var maeNum = 0
        var rolledText = ""
        var counter = -1

        if( rawText.count() == 0 ){ finished = true }
        val runb0 = object  : Runnable {
            override fun run() {
                if( counter < 0) { //最初の一回
                    counter = 0
                    if( (option == chainOption.callStart || option == chainOption.callStartFinish) && breaker == false ){
                        callback(chainState.start)
                    }
                    if( !finished ) {
                        mHandler.postDelayed(this, 15)
                        return
                    }
                }
                if( finished ){
                    this@ChainTextView.setText(rawText)
                    if( (option == chainOption.callFinish || option == chainOption.callStartFinish) && breaker == false ){
                        callback(chainState.finish)
                    }
                    return
                }
                var rndNum = (0..9).random()
                if( rndNum == maeNum ){
                    rndNum += 1
                    rndNum = rndNum % 10
                }
                maeNum = rndNum
                this@ChainTextView.setText(rndNum.toString() + rolledText)
                if( (counter % 20) == 0 ){
                    rolledText = remainingText.takeLast(1).toString() + rolledText
                    remainingText = remainingText.dropLast(1)
                    this@ChainTextView.setText(rolledText)
                    if( remainingText.count() <= 0 ){
                        finished = true
                        if( (option == chainOption.callFinish || option == chainOption.callStartFinish) && breaker == false ){
                            callback(chainState.finish)
                        }
                        return
                    }
                }
                counter += 1
                //
                mHandler.postDelayed(this,15)
            }
        }
        if( !finished ) {
            mHandler.postDelayed( runb0, duration.toLong() )
        }else {
            mHandler.post( runb0 )
        }
    }
    fun chainFinish(){
        finished = true
    }
    fun chainBreak(){
        finished = true
        breaker = true
    }

}

//Picasso
fun PicassoLoadImage_NicoThumb(imageView:ImageView?, url:String){
    if(imageView == null){ return }
    val thumbnail = MediaInput.thumbnail(url)
    if (thumbnail == null) {
        Picasso.get().cancelRequest(imageView)
        imageView.setImageDrawable(null)
        return
    }
    Picasso.get().load(thumbnail.url).resize(thumbnail.width, thumbnail.height).centerCrop().into(imageView)
    return
}
