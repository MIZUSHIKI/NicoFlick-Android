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
    }

    fun okJinglePlay(){
        soundPool.play(okJingle,1.0f,1.0f,0,0,1.0f)
    }
    fun safeJinglePlay(){
        soundPool.play(safeJingle,1.0f,1.0f,0,0,1.0f)
    }
    fun badJinglePlay(){
        soundPool.play(badJingle,0.5f,1.0f,0,0,1.0f)
    }
}