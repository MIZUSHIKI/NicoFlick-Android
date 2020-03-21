package com.mizushiki.nicoflick_a

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import kotlinx.android.synthetic.main.activity_selector.*
import kotlin.math.max

/**
 * TODO: document your custom view class.
 */
class CustomPicker : NumberPicker {

    var oneScrollOffset:Int? = null
    var oneScrollLength:Int? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        //this.maxValue = 1
        //
        /*
        val pickerFields = NumberPicker::class.java.declaredFields
        for( pf in pickerFields ){
            println("pf.name="+pf.name)
            if(pf.name.equals("mSelectionDivider")){
                pf.isAccessible = true
                try {
                    pf.set(this,ColorDrawable(Color.argb(10,0,0,0)))
                }catch (e: Exception){
                    println(e)
                }
                //break
            }
        }
         */
        this.descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        this.wrapSelectorWheel = false
        this.alpha = 0.01f
    }

    fun checkContainerHeight(){

        if(oneScrollLength != null){ return }

        val maeValue = this.maxValue
        //println(this.children.filterIsInstance<EditText>().first().height)
        this.maxValue = 1
        oneScrollOffset = this.computeVerticalScrollOffset()
        val range = this.computeVerticalScrollRange()
        this.maxValue = 2
        oneScrollLength = this.computeVerticalScrollRange() - range

        this.maxValue = maeValue
    }

}
