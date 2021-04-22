package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isInvisible
import com.squareup.picasso.Picasso


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
val String.isDotTimetag : Boolean
    get() = this.pregMatche("\\[[\\d\\*][\\d\\*]\\:[\\d\\*][\\d\\*]\\.[\\d\\*][\\d\\*]\\]")


fun String.pregReplace(pattern:String, with:String): String {
    return Regex(pattern).replace(this, with)
}
fun String.pregMatche(pattern: String): Boolean {
    return Regex(pattern).containsMatchIn(this)
}
//最初のマッチ（結果,括弧１,括弧２,・・・）を返す
fun String.pregMatche(pattern: String, matche: ArrayList<String>): Boolean {
    val boo = Regex(pattern).find(this)
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
    val boo = Regex(pattern).findAll(this)
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
fun String.pregMatches(pattern: String): MutableList<String> {
    return Regex(pattern).find(this)?.groupValues!!.toMutableList()
}
fun String.pregMatche_firstString(pattern: String):String {
    return Regex(pattern).find(this)?.groupValues?.get(1) ?: ""
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
class SlashShadeView(context: Context) : View(context) {
    private var paint: Paint = Paint()

    init {
    }

    override fun onDraw(canvas: Canvas){

        println("canvas.width=${canvas.width}")
        println("canvas.height=${canvas.height}")
        val w = canvas.width
        val h = canvas.height
        // ペイントする色の設定
        paint.color = Color.argb(255, 255, 255, 255)

        // ペイントストロークの太さを設定
        paint.strokeWidth = 1.75f
        // Styleのストロークを設定する
        paint.style = Paint.Style.STROKE

        for( y in 0..(w+h)/4 ){
            canvas.drawLine( Float(0), Float(y*4), Float(w), Float(y*4-w) ,paint)
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
//TextViewカスタム
class OutlineTextView(context: Context?, val firstRect:RectF) : AppCompatTextView(context) {
    var strokeWidth = 4.0f
    var strokeColor = Color.rgb(200,200,200)

    override fun onDraw(canvas: Canvas?) {
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
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val posX: Float = 0.0f
        val posY: Float = firstRect.height() / 2 + textBounds.height() / 2 - textBounds.bottom
        canvas!!.drawText(text, posX, posY, paint)
        paint.setStrokeWidth(0.0f)
        paint.setColor(textColor)
        paint.setStyle(Paint.Style.FILL)
        canvas.drawText(text, posX, posY, paint)
        //println("textBounds.height()="+textBounds.height())
    }
}
//Picasso
fun PicassoLoadImage_NicoThumb(imageView:ImageView?, url:String){
    if(imageView == null){ return }
    var url = url
    var sz: Size = Size(130,74)
    if(url.substring(url.length-1) != "?"){
        Regex("/thumbnails/(\\d+)/").find(url)?.groupValues?.get(1)?.let {
            if(it.toInt() >= 16371845){
                url += ".L"
                sz = Size(360,200)
            }
        }
        Regex("\\?i=(\\d+)").find(url)?.groupValues?.get(1)?.let {
            if(it.toInt() >= 16371845){
                url += ".L"
                sz = Size(360,200)
            }
        }
    }else {
        url = url.substring(0 until url.length-1)
    }
    Picasso.get().load(url).resize(sz.width,sz.height).centerCrop().into(imageView)
    return
}