package com.mizushiki.nicoflick_a

import android.media.AudioAttributes
import android.media.SoundPool

object SEAudio {
    val audioAttributes = AudioAttributes.Builder()
        // USAGE_MEDIA
        // USAGE_GAME
        .setUsage(AudioAttributes.USAGE_GAME)
        // CONTENT_TYPE_MUSIC
        // CONTENT_TYPE_SPEECH, etc.
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private lateinit var soundPool: SoundPool
    var okJingle = 0
    var safeJingle = 0
    var badJingle = 0

    var volume:Float = 1.0f

    init {
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            // ストリーム数に応じて
            .setMaxStreams(5)
            .build()

        // wav をロードしておく
        okJingle = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.okjingle, 1)
        safeJingle = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.safejingle, 1)
        badJingle = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.badjingle, 1)

        volume = USERDATA.SoundVolumeGameSE
    }

    fun okJinglePlay(){
        soundPool.play(okJingle,1.0f * volume,1.0f * volume,0,0,1.0f)
    }
    fun safeJinglePlay(){
        soundPool.play(safeJingle,1.0f * volume,1.0f * volume,0,0,1.0f)
    }
    fun badJinglePlay(){
        soundPool.play(badJingle,0.5f * volume,0.5f * volume,0,0,1.0f)
    }
}

object SESystemAudio {
    val audioAttributes = AudioAttributes.Builder()
        // USAGE_MEDIA
        // USAGE_GAME
        .setUsage(AudioAttributes.USAGE_GAME)
        // CONTENT_TYPE_MUSIC
        // CONTENT_TYPE_SPEECH, etc.
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private lateinit var soundPool: SoundPool
    private var shuffleSe = 0
    private var drumRollSe = 0
    private var rankSe = 0
    private var goSe = 0
    private var canselSe = 0
    private var cansel2Se = 0
    private var startSe = 0
    private var start2Se = 0
    private var gameMenuSe = 0
    private var openSe = 0
    private var openSubSe = 0
    private var openSelectorMenuSe = 0

    private var drumRoll_streamId = -1

    private var volRatio = 0.2f
    var volume = 1.0f

    init {
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            // ストリーム数に応じて
            .setMaxStreams(5)
            .build()

        // wav をロードしておく
        start2Se = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.start2, 1)
        startSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.start, 1)

        shuffleSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.shuffle, 1)
        drumRollSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.loop_drumroll, 1)
        rankSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.rank, 1)
        goSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.go, 1)
        canselSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.cansel, 1)
        cansel2Se = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.cansel2, 1)
        gameMenuSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.game_menu, 1)
        openSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.open, 1)
        openSubSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.open_sub, 1)
        openSelectorMenuSe = soundPool.load(GLOBAL.APPLICATIONCONTEXT, R.raw.open_selector_menu, 1)

        volume = USERDATA.SoundVolumeSystemSE
    }


    fun shuffleSePlay(){
        soundPool.play(shuffleSe,1.0f * volRatio * volume,1.0f * volRatio * volume,0,0,1.0f)
    }
    fun drumRollSeLoop(){
        if( drumRoll_streamId != -1 ){
            soundPool.stop(drumRoll_streamId)
            drumRoll_streamId = -1
        }
        drumRoll_streamId = soundPool.play(drumRollSe,1.0f * volRatio * volume,1.0f * volRatio * volume,0,-1,1.0f)
    }
    fun drumRollSeStop(){
        if( drumRoll_streamId == -1 ){ return }
        soundPool.stop(drumRoll_streamId)
        drumRoll_streamId = -1
    }
    fun rankSePlay(){
        soundPool.play(rankSe,5.0f * volRatio * volume,5.0f * volRatio * volume,0,0,1.0f)
    }
    fun goSePlay(){
        soundPool.play(goSe,3.0f * volRatio * volume,3.0f * volRatio * volume,0,0,1.0f)
    }
    fun canselSePlay(){
        soundPool.play(canselSe,1.0f * volRatio * volume,1.0f * volRatio * volume,0,0,1.0f)
    }
    fun cansel2SePlay(){
        soundPool.play(cansel2Se,3.0f * volRatio * volume,3.0f * volRatio * volume,0,0,1.0f)
    }
    fun startSePlay(){
        soundPool.play(startSe,3.0f * volRatio * volume,3.0f * volRatio * volume,0,0,1.0f)
    }
    fun start2SePlay(){
        soundPool.play(start2Se,1.0f * volRatio * volume,1.0f * volRatio * volume,0,0,1.0f)
    }
    fun gameMenuSePlay(){
        soundPool.play(gameMenuSe,3.0f * volRatio * volume,3.0f * volRatio * volume,0,0,1.0f)
    }
    fun openSePlay(){
        soundPool.play(openSe,0.8f * volRatio * volume,0.8f * volRatio * volume,0,0,1.0f)
    }
    fun openSubSePlay(){
        soundPool.play(openSubSe,0.6f * volRatio * volume,0.6f * volRatio * volume,0,0,1.0f)
    }
    fun openSelectorMenuSePlay(){
        soundPool.play(openSelectorMenuSe,2.0f * volRatio * volume,2.0f * volRatio * volume,0,0,1.0f)
    }
}