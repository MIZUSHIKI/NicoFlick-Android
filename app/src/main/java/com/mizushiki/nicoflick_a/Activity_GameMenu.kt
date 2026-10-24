package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivityGameMenuBinding

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import java.util.function.Consumer
import android.graphics.Point
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Activity_GameMenu : AppCompatActivity() {

    private lateinit var binding: ActivityGameMenuBinding
    private var blurEnabledListener: Consumer<Boolean>? = null
    private var selectedLevel: levelData? = null


    var playerHeight = 1
    var takasasa = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val level = GLOBAL.SelectLEVEL ?: return returnToStartAfterStateLoss()
        selectedLevel = level
        val _levelID = level.sqlID
        binding.textViewJudgeOffset.setText("0.0")
        binding.seekBar.setProgress( 20,false)
        USERDATA.JudgeOffset[_levelID]?.let {
            binding.seekBar.setProgress((it * 100 + 20).toInt(),false)
            binding.textViewJudgeOffset.setText("$it")
        }
        binding.seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                val jo = (progress.toFloat() - 20.0F) / 100
                binding.textViewJudgeOffset.setText("$jo")
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {
                if(bar == null){ return }
                // println("stop")
                val judges = USERDATA.JudgeOffset
                judges[_levelID] = (bar.progress.toFloat() - 20.0F) / 100
                USERDATA.JudgeOffset = judges
                //USERDATA.JudgeOffset[_levelID] = bar.progress.toFloat() / 100
            }
        })


        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val viewWidth = point.x.toDouble()
        val viewHeight = point.y.toDouble()
        // println("xy = $viewWidth x $viewHeight")
        var playerHeight = 0
        if( (viewWidth * 270 / 375) < (viewHeight * 0.4) ) {
            playerHeight = viewWidth.toInt() * 270 / 375
            // println("270")
        }else {
            playerHeight = (viewHeight * 0.4).toInt()
            // println("0.4")
        }
        // println(playerHeight)
        binding.seekBar2.layoutParams.width = playerHeight
        binding.seekBar2.progress = USERDATA.PlayerHeightPer

        Handler().postDelayed({
            takasasa = binding.playerHeighter.y - binding.playerHeighterText.y
            // println("takasasa=$takasasa")
            binding.seekBar2.center = PointF((viewWidth-30).toFloat(),playerHeight.toFloat()/2 )
            binding.playerHeighter.centerY = (playerHeight * USERDATA.PlayerHeightPer / 10000).toFloat()
            binding.playerHeighterText.y = binding.playerHeighter.y - takasasa
            updateDetailSettingsVisibility()
        },500)

        binding.seekBar2.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(bar == null){ return }
                if( bar.progress < 5000 ){
                    bar.progress = 5000
                    return
                }
                binding.playerHeighter.centerY = (playerHeight * bar.progress / 10000).toFloat()
                binding.playerHeighterText.y = binding.playerHeighter.y - takasasa
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {
                if(bar == null){ return }
                USERDATA.PlayerHeightPer = bar.progress
            }
        })

        // ↑ かなりとっ散らかってるけど整理整頓は後回し

        val progress1y = getIntent().getFloatExtra("progress1",0.0f)
        val progress2y = getIntent().getFloatExtra("progress2",0.0f)
        binding.viewGauge1.centerY = progress1y - 2.0f
        binding.viewGauge2.centerY = progress2y - 2.0f
        binding.switch1GameMenu.isChecked = USERDATA.UseNicoFlickKeyboard
        binding.checkBoxExpandMovie.isChecked = USERDATA.fitsMovieToKeyboardHeight
        binding.checkBoxShowOffset.isChecked = USERDATA.showOffsetLabel
        binding.checkBoxExpandMovie.setOnCheckedChangeListener { _, checked ->
            USERDATA.fitsMovieToKeyboardHeight = checked
            updateDetailSettingsVisibility()
        }
        binding.checkBoxShowOffset.setOnCheckedChangeListener { _, checked ->
            USERDATA.showOffsetLabel = checked
        }
        updateDetailSettingsVisibility()
    }

    override fun onStart() {
        super.onStart()
        if (isFinishing) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            updateMenuBlur(windowManager.isCrossWindowBlurEnabled)
            val listener = Consumer<Boolean> { enabled -> updateMenuBlur(enabled) }
            blurEnabledListener = listener
            windowManager.addCrossWindowBlurEnabledListener(mainExecutor, listener)
        }
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurEnabledListener?.let { windowManager.removeCrossWindowBlurEnabledListener(it) }
            blurEnabledListener = null
        }
        super.onStop()
    }

    private fun updateMenuBlur(enabled: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        // メニューの文字やボタンはぼかさず、背後のゲーム画面だけをぼかす。
        window.setBackgroundBlurRadius(if (enabled) 80 else 0)
        if (enabled) {
            window.setBackgroundDrawable(ColorDrawable(Color.argb(180, 48, 48, 48)))
        } else {
            window.setBackgroundDrawableResource(R.drawable.transparameterGameMenu)
        }
    }

    fun Button_Practice(view:View) {
        if (intent.getBooleanExtra("practiceMode", false)) {  SESystemAudio.canselSePlay(); setResult(12); finish(); return }
        SESystemAudio.openSePlay()
        AlertDialog.Builder(this).setTitle("練習モード")
            .setMessage("練習モードにするとスコアが記録されなくなります。よろしいですか？")
            .setPositiveButton("OK") { _, _ ->
                SESystemAudio.canselSePlay()
                setResult(13)
                finish()
            }
            .setNegativeButton("キャンセル", null).show()
    }
    fun Button_GameSettings(view:View) {
        USERDATA.ShowGameSettings = !USERDATA.ShowGameSettings
        updateDetailSettingsVisibility()
    }

    private fun updateDetailSettingsVisibility() {
        val show = USERDATA.ShowGameSettings
        // 座標計算や他の部品の配置を保つため、非表示でもレイアウト領域は残す。
        listOf(
            binding.seekBar, binding.textViewJudgeOffset,
            binding.textView11, binding.textView12, binding.textView13,
            binding.checkBoxExpandMovie, binding.checkBoxShowOffset,
            binding.switch1GameMenu
        ).forEach { it.isInvisible = !show }
        val showPlayerHeight = show && !USERDATA.fitsMovieToKeyboardHeight
        listOf(binding.seekBar2, binding.playerHeighter, binding.playerHeighterText)
            .forEach { it.isInvisible = !showPlayerHeight }
        val showGauge = show && (selectedLevel?.level ?: 0) <= 10
        binding.buttonGauge.isInvisible = !showGauge
        binding.viewGauge1.isInvisible = !showGauge || !USERDATA.BorderPosUe
        binding.viewGauge2.isInvisible = !showGauge || USERDATA.BorderPosUe
        binding.gameSettingsButton.contentDescription =
            if (show) "詳細設定を非表示" else "詳細設定を表示"
    }

    fun Button_GobackMusicSelect(view: View) {
        SESystemAudio.cansel2SePlay()
        // 返却
        setResult( 10, null )
        finish()
    }
    fun Button_Retry(view: View) {
        SESystemAudio.canselSePlay()
        // 返却
        setResult( 11, null )
        finish()
    }
    fun Button_Continue(view: View) {
        SESystemAudio.canselSePlay()
        // 返却
        setResult( 12, null )
        finish()
    }

    fun Button_gauge(view: View) {
        USERDATA.BorderPosUe = !USERDATA.BorderPosUe
        updateDetailSettingsVisibility()
    }

    fun Switch_useKeyboard(view: View) {
        USERDATA.UseNicoFlickKeyboard = binding.switch1GameMenu.isChecked
    }
}
