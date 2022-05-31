package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.InputFilter
import android.text.Spanned
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_settings_keyboard.*
import kotlinx.android.synthetic.main.activity_settings_keyboard.editText
import kotlinx.android.synthetic.main.activity_settings_keyboard.seekBar2
import kotlinx.coroutines.*

class Activity_SettingsKeyboard : AppCompatActivity() {

    var usedSimeji:Boolean = false
    var myKeybd:MyKeyboard? = null
    var maeSource:String = ""
    var maeSourceClear = false

    private var coroutineJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_keyboard)

        myKeybd = MyKeyboard(this)
        addContentView( myKeybd, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT) )
        myKeybd?.onKeylistener = {
            println("bt word = ${it}")
            FlickInput(it)
        }
        // 使用IME名取得
        val imeString = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        println("ime = $imeString")
        usedSimeji = imeString.contains("simeji")
        println("usedSimeji=$usedSimeji")

        editText.filters = arrayOf(object : InputFilter {
            override fun filter(source: CharSequence?,start: Int,end: Int,dest: Spanned?,dstart: Int,dend: Int): CharSequence? {
                source?.let{

                    if( end > 0 && source.toString() != maeSource ){
                        if(it.dropLast(1).toString() == dest.toString()){
                            //判定
                            println("OK - ${it.last()} - ${it.dropLast(1)} $dest")
                            FlickInput( it.last().toString() )
                            maeSource = source.toString()
                        }else {
                            println("NG - ${it.last()} - ${it.dropLast(1)} $dest")
                            FlickInput( "x" )
                            maeSource = ""
                            editText.setText("")
                        }
                    }
                    if( usedSimeji && end >= 30 ) {
                        maeSource=""
                        editText.setText("")
                    }
                }
                //println("pp $source -- $maeSource == ${source.toString()} || ${maeSource.length} > ${source?.length}, $start $end $dest, $dstart $dend ")
                return null
            }
        })

        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val viewWidth = point.x.toDouble()
        val viewHeight = point.y.toDouble()
        seekBar2.layoutParams.width = (viewHeight / 4).toInt()
        seekBar2.progress = USERDATA.NicoFlickKeyboardHeightPer

        Handler().postDelayed({
            seekBar2.center = PointF((viewWidth-50).toFloat(),(viewHeight.toFloat()-GLOBAL.NavigationBarHeight)*5/8 )
            textview_Heighter.y = (viewHeight.toFloat()-GLOBAL.NavigationBarHeight)*5/8 - seekBar2.width/2 - textview_Heighter.height
            seekBar2.alpha = 1.0f
        },500)

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){ return }
                myKeybd?.setPaddingKeybd(top = (viewHeight / 4).toInt() * (10000 - progress) / 10000)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                USERDATA.NicoFlickKeyboardHeightPer = seekBar.progress
            }
        })
        myKeybd?.setPaddingKeybd(top = (viewHeight / 4).toInt() * (10000 - USERDATA.NicoFlickKeyboardHeightPer) / 10000)

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){ return }
                val sens = 6.0f + 10.0f * progress.toFloat() / 50.0f
                textview_kando.setText("フリック判定ストローク: ${  (sens*10+0.5).toInt().toFloat()/10 }")
                myKeybd?.setStrokeSens(sens)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                USERDATA.NicoFlickKeyboardSense = seekBar.progress
            }
        })
        textview_kando.setText("フリック判定ストローク: ${ ((6.0f + 10.0f * USERDATA.NicoFlickKeyboardSense.toFloat() / 50.0f)*10+0.5).toInt().toFloat()/10 }")

        ObjShowHide_useMyKeybd()
        //if( !USERDATA.UseNicoFlickKeyboard ){
            editText.requestFocus()
        //}
    }
    fun FlickInput(word:String){
        if( word == "" ){ return }
        flickText.setText(word)
        coroutineJob?.cancel()
        coroutineJob = GlobalScope.launch(Dispatchers.Main){
            delay(1500)
            maeSource = ""
            maeSourceClear = true
            editText.setText("")
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        hideKeyboard()
        return super.onTouchEvent(event)
    }

    fun Button_Back(view: View) {
        SESystemAudio.canselSePlay()
        finish()
    }

    fun Switch_useKeyboard(view: View) {
        USERDATA.UseNicoFlickKeyboard = switch1.isChecked
        if( switch1.isChecked ){
            SESystemAudio.openSubSePlay()
            hideKeyboard()
        }else{
            SESystemAudio.openSePlay()
            Handler().postDelayed(10) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
            }
        }
        ObjShowHide_useMyKeybd()
    }
    fun ObjShowHide_useMyKeybd(){
        val myHide = !USERDATA.UseNicoFlickKeyboard
        switch1.isChecked = !myHide
        textview_Heighter.isHidden = myHide
        seekBar2.isHidden = myHide
        textview_kando.isHidden = myHide
        seekBar1.isHidden = myHide
        myKeybd?.isHidden = myHide
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //if(editFlg!=0){ return }
        //editFlg=1
        if( USERDATA.UseNicoFlickKeyboard ){ return }
        println("currentFocus")
        if (hasFocus && editText == currentFocus) {
            Handler().postDelayed(10) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
            }
        }
    }
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val manager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}