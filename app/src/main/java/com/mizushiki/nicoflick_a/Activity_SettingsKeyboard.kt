package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivitySettingsKeyboardBinding

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

import kotlinx.coroutines.*

class Activity_SettingsKeyboard : SafeAreaActivity() {

    private lateinit var binding: ActivitySettingsKeyboardBinding


    var usedSimeji:Boolean = false
    var myKeybd:MyKeyboard? = null
    var maeSource:String = ""
    var maeSourceClear = false

    private var coroutineJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myKeybd = MyKeyboard(this)
        addContentView( myKeybd, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT) )
        myKeybd?.onKeylistener = {
            // println("bt word = ${it}")
            FlickInput(it)
        }
        // 使用IME名取得
        val imeString = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        // println("ime = $imeString")
        usedSimeji = imeString.contains("simeji")
        // println("usedSimeji=$usedSimeji")

        binding.editText.filters = arrayOf(object : InputFilter {
            override fun filter(source: CharSequence?,start: Int,end: Int,dest: Spanned?,dstart: Int,dend: Int): CharSequence? {
                source?.let{

                    if( end > 0 && source.toString() != maeSource ){
                        if(it.dropLast(1).toString() == dest.toString()){
                            //判定
                            // println("OK - ${it.last()} - ${it.dropLast(1)} $dest")
                            FlickInput( it.last().toString() )
                            maeSource = source.toString()
                        }else {
                            // println("NG - ${it.last()} - ${it.dropLast(1)} $dest")
                            FlickInput( "x" )
                            maeSource = ""
                            binding.editText.setText("")
                        }
                    }
                    if( usedSimeji && end >= 30 ) {
                        maeSource=""
                        binding.editText.setText("")
                    }
                }
                //println("pp $source -- $maeSource == ${source.toString()} || ${maeSource.length} > ${source?.length}, $start $end $dest, $dstart $dend ")
                return null
            }
        })

        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val viewHeight = point.y.toDouble()
        binding.seekBar2.layoutParams.width = (viewHeight / 4).toInt()
        binding.seekBar2.progress = USERDATA.NicoFlickKeyboardHeightPer

        // Use the actual safe-area bounds and reapply after constraints are laid out.
        // Setting y only once leaves a translation relative to an outdated constraint position.
        fun positionHeightSlider() {
            val root = binding.root
            val slider = binding.seekBar2
            if (root.width == 0 || root.height == 0 || slider.width == 0) return
            val centerY = root.height * 5f / 8f
            slider.center = PointF(root.width - 50f, centerY)
            binding.textviewHeighter.y = centerY - slider.width / 2f - binding.textviewHeighter.height
            slider.alpha = 1f
        }
        binding.root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> positionHeightSlider() }
        binding.seekBar2.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> positionHeightSlider() }
        binding.textviewHeighter.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> positionHeightSlider() }

        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
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

        binding.seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){ return }
                val sens = 6.0f + 10.0f * progress.toFloat() / 50.0f
                binding.textviewKando.setText("フリック判定ストローク: ${  (sens*10+0.5).toInt().toFloat()/10 }")
                myKeybd?.setStrokeSens(sens)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                USERDATA.NicoFlickKeyboardSense = seekBar.progress
            }
        })
        binding.textviewKando.setText("フリック判定ストローク: ${ ((6.0f + 10.0f * USERDATA.NicoFlickKeyboardSense.toFloat() / 50.0f)*10+0.5).toInt().toFloat()/10 }")

        ObjShowHide_useMyKeybd()
        //if( !USERDATA.UseNicoFlickKeyboard ){
            binding.editText.requestFocus()
        //}
    }
    fun FlickInput(word:String){
        if( word == "" ){ return }
        binding.flickText.setText(word)
        coroutineJob?.cancel()
        coroutineJob = GlobalScope.launch(Dispatchers.Main){
            delay(1500)
            maeSource = ""
            maeSourceClear = true
            binding.editText.setText("")
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
        USERDATA.UseNicoFlickKeyboard = binding.switch1.isChecked
        if( binding.switch1.isChecked ){
            SESystemAudio.openSubSePlay()
            hideKeyboard()
        }else{
            SESystemAudio.openSePlay()
            Handler().postDelayed(10) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.editText, 0)
            }
        }
        ObjShowHide_useMyKeybd()
    }
    fun ObjShowHide_useMyKeybd(){
        val myHide = !USERDATA.UseNicoFlickKeyboard
        binding.switch1.isChecked = !myHide
        binding.textviewHeighter.isHidden = myHide
        binding.seekBar2.isHidden = myHide
        binding.textviewKando.isHidden = myHide
        binding.seekBar1.isHidden = myHide
        myKeybd?.isHidden = myHide
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //if(editFlg!=0){ return }
        //editFlg=1
        if( USERDATA.UseNicoFlickKeyboard ){ return }
        // println("currentFocus")
        if (hasFocus && binding.editText == currentFocus) {
            Handler().postDelayed(10) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.editText, 0)
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