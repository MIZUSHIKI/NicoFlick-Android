package com.mizushiki.nicoflick_a

import com.mizushiki.nicoflick_a.databinding.ActivitySettingsBinding

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import kotlinx.coroutines.Job



class Activity_Settings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding


    //効果音
    val seAudio = SEAudio //読み込み
    //val SESystemAudio = SESystemAudio //タイトルで読み込み済み

    private lateinit var moviePlayButtonTextColors: ColorStateList
    private val moviePlaybackListener = object : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateMoviePlayButtonColor(isPlaying)
        }
    }
    private var cacheExoPlayer: SimpleExoPlayer? = null
        set(value) {
            field?.removeListener(moviePlaybackListener)
            field = value
            value?.addListener(moviePlaybackListener)
            updateMoviePlayButtonColor(value?.isPlaying == true)
        }
    private var movieLoadJob: Job? = null
    private val defaultMovieSmNum = "sm25672022"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        moviePlayButtonTextColors = binding.buttonVolumeSettingsMoviePlay.textColors

        binding.editMail.setText(USERDATA.NicoMail)
        binding.editPass.setText(USERDATA.NicoPass)
        binding.editRegistName.setText(USERDATA.UserName)
        binding.textViewUniqueUserID.text = USERDATA.UserIDxxx

        binding.switchUseNewLevelCalculation.isChecked = USERDATA.UseNewLevelCalculationKey
        binding.switchUseNewLevelCalculation.setOnCheckedChangeListener { _, isChecked ->
            USERDATA.UseNewLevelCalculationKey = isChecked
            if (isChecked) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.settings_new_level_title)
                    .setMessage(R.string.settings_new_level_notice)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        fun updateScrollFades() {
            binding.settingsFadeTop.isVisible = binding.settingsScroll.canScrollVertically(-1)
            // 初回のコンテンツ計測が済むまではXMLの表示状態を維持する。
            if ((binding.settingsScroll.getChildAt(0)?.height ?: 0) > 0) {
                binding.settingsFadeBottom.isVisible = binding.settingsScroll.canScrollVertically(1)
            }
        }
        binding.settingsScroll.viewTreeObserver.addOnScrollChangedListener { updateScrollFades() }
        binding.settingsScroll.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateScrollFades()
        }
        binding.settingsScroll.post { updateScrollFades() }

        //VolumeSettings_movie
        binding.seekBarVolumesettingsMovie.setProgress( (USERDATA.SoundVolumeMovie * 100).toInt(),false)
        binding.textViewVolumeSettingsMoviePer.setText("${(USERDATA.SoundVolumeMovie * 100).toInt()}%")
        binding.seekBarVolumesettingsMovie.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //USERDATA.SoundVolumeMovie = (progress.toFloat() / 100) //やたら遅いのでBack時に
                binding.textViewVolumeSettingsMoviePer.setText("${progress}%")
                cacheExoPlayer?.volume = 0.2f * (progress.toFloat() / 100)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                // println("stop")
                for ( cachedMovie in CachedMovies.cachedMovies ){
                    cachedMovie.simpleExoPlayer.volume = 0.2f * (seekBar.progress.toFloat() / 100)
                }
            }
        })
        val cachedMovie = CachedMovies.cachedMovies.lastOrNull()
        if (cachedMovie != null) {
            binding.buttonVolumeSettingsMoviePlay.setText("▶ 動画再生：${cachedMovie.smNum}")
        } else {
            loadDefaultMovie()
        }
        //VolumeSettings_gameSE
        binding.seekBarVolumesettingsGame.setProgress( (USERDATA.SoundVolumeGameSE * 100).toInt(),false)
        binding.textViewVolumeSettingsGamePer.setText("${(USERDATA.SoundVolumeGameSE * 100).toInt()}%")
        binding.seekBarVolumesettingsGame.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textViewVolumeSettingsGamePer.setText("${progress}%")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                val vol = seekBar.progress.toFloat() / 100
                // println("stop")
                SEAudio.volume = vol
                SEAudio.okJinglePlay()
                //USERDATA.SoundVolumeGameSE = (seekBar.progress.toFloat() / 100) //やたら遅いのでBack時に
            }
        })
        //VolumeSettings_systemSE
        binding.seekBarVolumesettingsSystem.setProgress( (USERDATA.SoundVolumeSystemSE * 100).toInt(),false)
        binding.textViewVolumeSettingsSystemPer.setText("${(USERDATA.SoundVolumeSystemSE * 100).toInt()}%")
        binding.seekBarVolumesettingsSystem.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textViewVolumeSettingsSystemPer.setText("${progress}%")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar == null){ return }
                val vol = seekBar.progress.toFloat() / 100
                // println("stop")
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
        val name = binding.editRegistName.text.toString()
        registerUserNameBlocking(name) { success ->
            AlertDialog.Builder(this)
                .setMessage(if (success) "登録しました。" else "登録に失敗しました。再度お試しください。")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    //GoBack時
    override fun onStop() {
        USERDATA.NicoMail = binding.editMail.text.toString()
        USERDATA.NicoPass = binding.editPass.text.toString()
        cacheExoPlayer?.playWhenReady = false
        USERDATA.SoundVolumeMovie = binding.seekBarVolumesettingsMovie.progress.toFloat() / 100
        USERDATA.SoundVolumeGameSE = binding.seekBarVolumesettingsGame.progress.toFloat() / 100
        USERDATA.SoundVolumeSystemSE = binding.seekBarVolumesettingsSystem.progress.toFloat() / 100
        super.onStop()
    }

    fun ButtonLoadDataDelete(view: View) {
        AlertDialog.Builder(this)
            .setTitle("データベースからロードしたデータを全て削除")
            .setMessage("・何か不具合があった場合の初期化用です。")
            .setPositiveButton("Yes", { dialog, which ->
                MusicDataLists.reset()
                MetadataStorage.clearMusicsAndLevels()
                UserNameDataLists.reset()
                USERDATA.UserNamesServerJsonNumCount = 0
                USERDATA.UserNamesServerJsonCreateTime = 0
                ScoreDataLists.reset()
                CommentDataLists.reset()
                USERDATA.SelectedMusicCondition.tags = ""
                USERDATA.SelectedMusicCondition.sortItem = "曲の投稿日時 昇順"

                // ver1.9->1.10 からのDataStore整頓が働いていなかったら悲しいのでしっかりと[一定の期間 置いておく]
                USERDATA.SikkariRemove__Version1_9_0_0to1_10_0_0()
            })
            .setNegativeButton("No", { dialog, which ->
            })
            .show()
    }

    private fun updateMoviePlayButtonColor(isPlaying: Boolean) {
        binding.buttonVolumeSettingsMoviePlay.setTextColor(
            if (isPlaying) ColorStateList.valueOf(Color.RED) else moviePlayButtonTextColors
        )
    }

    private fun loadDefaultMovie() {
        binding.buttonVolumeSettingsMoviePlay.isEnabled = false
        binding.buttonVolumeSettingsMoviePlay.text = "読み込み中：$defaultMovieSmNum"
        movieLoadJob = MovieAccess().StreamingUrlNicoAccess(defaultMovieSmNum) { url ->
            if (isFinishing || isDestroyed) return@StreamingUrlNicoAccess
            if (url.isBlank() || url == "error") return@StreamingUrlNicoAccess
            cacheExoPlayer = CachedMovies.access(url, defaultMovieSmNum).also {
                it.volume = 0.2f * (binding.seekBarVolumesettingsMovie.progress.toFloat() / 100)
            }
        }.also { job ->
            job.invokeOnCompletion {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        binding.buttonVolumeSettingsMoviePlay.isEnabled = true
                        binding.buttonVolumeSettingsMoviePlay.text = if (cacheExoPlayer != null) {
                            "▶ 動画再生：$defaultMovieSmNum"
                        } else {
                            "読み込み失敗・再試行：$defaultMovieSmNum"
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        movieLoadJob?.cancel()
        cacheExoPlayer?.removeListener(moviePlaybackListener)
        super.onDestroy()
    }

    fun Button_VolumeSettings_movie_play(view: View) {

        if (cacheExoPlayer == null) {
            cacheExoPlayer = CachedMovies.cachedMovies.lastOrNull()?.simpleExoPlayer
        }
        if (cacheExoPlayer == null) {
            loadDefaultMovie()
            return
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
