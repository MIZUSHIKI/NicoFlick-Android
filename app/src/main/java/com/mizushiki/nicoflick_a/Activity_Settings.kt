package com.mizushiki.nicoflick_a

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.activity_settings.*


class Activity_Settings : AppCompatActivity() {

    //効果音
    val seAudio = SEAudio //読み込み
    //val SESystemAudio = SESystemAudio //タイトルで読み込み済み

    private var cacheExoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        edit_Mail.setText(USERDATA.NicoMail)
        edit_Pass.setText(USERDATA.NicoPass)
        edit_registName.setText(USERDATA.UserName)
        textView_uniqueUserID.text = USERDATA.UserIDxxx

        //VolumeSettings_movie
        seekBar_volumesettings_movie.setProgress( (USERDATA.SoundVolumeMovie * 100).toInt(),false)
        textView_VolumeSettings_movie_per.setText("${(USERDATA.SoundVolumeMovie * 100).toInt()}%")
        seekBar_volumesettings_movie.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //USERDATA.SoundVolumeMovie = (progress.toFloat() / 100) //やたら遅いのでBack時に
                textView_VolumeSettings_movie_per.setText("${progress}%")
                cacheExoPlayer?.volume = 0.2f * (progress.toFloat() / 100)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                println("stop")
                for ( cachedMovie in CachedMovies.cachedMovies ){
                    cachedMovie.simpleExoPlayer.volume = 0.2f * (seekBar.progress.toFloat() / 100)
                }
            }
        })
        CachedMovies.cachedMovies.lastOrNull()?.let {
            button_VolumeSettings_movie_play.setText("▶ 動画再生：${it.smNum}")
        }
        //VolumeSettings_gameSE
        seekBar_volumesettings_game.setProgress( (USERDATA.SoundVolumeGameSE * 100).toInt(),false)
        textView_VolumeSettings_game_per.setText("${(USERDATA.SoundVolumeGameSE * 100).toInt()}%")
        seekBar_volumesettings_game.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView_VolumeSettings_game_per.setText("${progress}%")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                val vol = seekBar.progress.toFloat() / 100
                println("stop")
                SEAudio.volume = vol
                SEAudio.okJinglePlay()
                //USERDATA.SoundVolumeGameSE = (seekBar.progress.toFloat() / 100) //やたら遅いのでBack時に
            }
        })
        //VolumeSettings_systemSE
        seekBar_volumesettings_system.setProgress( (USERDATA.SoundVolumeSystemSE * 100).toInt(),false)
        textView_VolumeSettings_system_per.setText("${(USERDATA.SoundVolumeSystemSE * 100).toInt()}%")
        seekBar_volumesettings_system.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView_VolumeSettings_system_per.setText("${progress}%")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                val vol = seekBar.progress.toFloat() / 100
                println("stop")
                SESystemAudio.volume = vol
                SESystemAudio.startSePlay()
                //USERDATA.SoundVolumeSystemSE = (seekBar.progress.toFloat() / 100) //やたら遅いのでBack時に
            }
        })
    }

    fun Button_Back(view: View) {
        SESystemAudio.canselSePlay()
        finish()
    }
    fun Button_settings_keyboard(view: View) {
        SESystemAudio.openSePlay()
        val intent: Intent = Intent(applicationContext, Activity_SettingsKeyboard::class.java)
        startActivity(intent)
    }

    fun Button_Regist(view: View) {
        SESystemAudio.start2SePlay()
        return
        progress_circular.isVisible = true
        USERDATA.UserName = edit_registName.text.toString()

        ServerDataHandler().postUserName(edit_registName.text.toString(),USERDATA.UserID){
            if(it){
                progress_circular.isVisible = false
                AlertDialog.Builder(this)
                    .setMessage("登録しました。")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    //GoBack時
    override fun onStop() {
        USERDATA.NicoMail = edit_Mail.text.toString()
        USERDATA.NicoPass = edit_Pass.text.toString()
        cacheExoPlayer?.playWhenReady = false
        USERDATA.SoundVolumeMovie = seekBar_volumesettings_movie.progress.toFloat() / 100
        USERDATA.SoundVolumeGameSE = seekBar_volumesettings_game.progress.toFloat() / 100
        USERDATA.SoundVolumeSystemSE = seekBar_volumesettings_system.progress.toFloat() / 100
        super.onStop()
    }

    fun ButtonLoadDataDelete(view: View) {
        AlertDialog.Builder(this)
            .setTitle("データベースからロードしたデータを全て削除")
            .setMessage("・何か不具合があった場合の初期化用です。")
            .setPositiveButton("Yes", { dialog, which ->
                MusicDataLists.reset()
                USERDATA.MusicsJson = ""
                USERDATA.LevelsJson = ""
                UserNameDataLists.reset()
                USERDATA.UserNamesJson = ""
                USERDATA.UserNamesServerJsonNumCount = 0
                USERDATA.UserNamesServerJsonCreateTime = 0
                ScoreDataLists.reset()
                USERDATA.ScoresJson = ""
                CommentDataLists.reset()
                USERDATA.CommentsJson = ""

                // ver1.9->1.10 からのDataStore整頓が働いていなかったら悲しいのでしっかりと[一定の期間 置いておく]
                USERDATA.SikkariRemove__Version1_9_0_0to1_10_0_0()
            })
            .setNegativeButton("No", { dialog, which ->
            })
            .show()
    }

    fun Button_VolumeSettings_movie_play(view: View) {

        val cachedMovie = CachedMovies.cachedMovies.lastOrNull() ?: return
        if( cacheExoPlayer == null ){
            cacheExoPlayer = cachedMovie.simpleExoPlayer
        }
        cacheExoPlayer?.let {
            if( !it.playWhenReady ){
                it.seekTo(0)
                it.playWhenReady = true
            }else {
                it.playWhenReady = false
            }
        }
    }
}
