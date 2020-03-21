package com.mizushiki.nicoflick_a

import android.content.Intent
import android.graphics.Point
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_game_menu.*

class Activity_GameMenu : AppCompatActivity() {

    var playerHeight = 1
    var takasasa = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_menu)

        val _levelID = GLOBAL.SelectLEVEL!!.sqlID//intent.getIntExtra("SelectLevelID", -1)
        textView_judgeOffset.setText("0.0")
        USERDATA.JudgeOffset[_levelID]?.let {
            seekBar.setProgress((it * 100).toInt(),false)
            textView_judgeOffset.setText("$it")
        }
        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val jo = progress.toFloat() / 100
                textView_judgeOffset.setText("$jo")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                println("stop")
                val judges = USERDATA.JudgeOffset
                judges[_levelID] = seekBar.progress.toFloat() / 100
                USERDATA.JudgeOffset = judges
                //USERDATA.JudgeOffset[_levelID] = seekBar.progress.toFloat() / 100
            }
        })


        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val viewWidth = point.x.toDouble()
        val viewHeight = point.y.toDouble()
        println("xy = $viewWidth x $viewHeight")
        var playerHeight = 0
        if( (viewWidth * 270 / 375) < (viewHeight * 0.4) ) {
            playerHeight = viewWidth.toInt() * 270 / 375
            println("270")
        }else {
            playerHeight = (viewHeight * 0.4).toInt()
            println("0.4")
        }
        println(playerHeight)
        seekBar2.layoutParams.width = playerHeight
        seekBar2.progress = USERDATA.PlayerHeightPer

        Handler().postDelayed({
            takasasa = playerHeighter.y - playerHeighterText.y
            println("takasasa=$takasasa")
            seekBar2.center = PointF((viewWidth-30).toFloat(),playerHeight.toFloat()/2 )
            playerHeighter.centerY = (playerHeight * USERDATA.PlayerHeightPer / 10000).toFloat()
            playerHeighterText.y = playerHeighter.y - takasasa
            seekBar2.isVisible = true
            playerHeighter.isVisible = true
            playerHeighterText.isVisible = true
        },500)

        seekBar2.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){ return }
                if( seekBar.progress < 5000 ){
                    seekBar.progress = 5000
                    return
                }
                playerHeighter.centerY = (playerHeight * seekBar.progress / 10000).toFloat()
                playerHeighterText.y = playerHeighter.y - takasasa
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                USERDATA.PlayerHeightPer = seekBar.progress
            }
        })

        // ↑ かなりとっ散らかってるけど整理整頓は後回し

    }

    fun Button_GobackMusicSelect(view: View) {
        // 返却
        setResult( 10, null )
        finish()
    }
    fun Button_Retry(view: View) {
        // 返却
        setResult( 11, null )
        finish()
    }
    fun Button_Continue(view: View) {
        finish()
    }
}
