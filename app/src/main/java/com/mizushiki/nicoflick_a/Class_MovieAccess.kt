package com.mizushiki.nicoflick_a

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.media.browse.MediaBrowser
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.core.text.HtmlCompat
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import okhttp3.internal.toImmutableMap
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class MovieAccess {

    var firstAttack = true

    fun StreamingUrlNicoAccess(smNum:String, callback: (String) -> Unit) = GlobalScope.launch(Dispatchers.Main) {

        //先にキャッシュ内にあるか確認。あればもうニコニコの動画ページにすらアクセスしないことにする
        if( CachedMovies.cachedMovies.any{ it.smNum == smNum }){
            callback("cached")
            return@launch
        }
        println("smNum="+smNum)

        val strURL = "https://www.nicovideo.jp/watch/"+smNum
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(strURL) }.await().let {
            println("net")
            if (it == null) {
                callback("error")
                return@let
            }
            var st = it.pregMatche_firstString("<div id=\"js-initial-watch-data\" data-api-data=\"(.*?)\" hidden></div>")
            if( st=="" ){ return@let }
            st = HtmlCompat.fromHtml(st,HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            //st = st.pregReplace("\\\\u([0-9a-f]{2})([0-9a-f]{2})","%$1%$2")
            //st = st.pregReplace("\\\\","")
            //st = URLDecoder.decode(st,"UTF16")

            println(st)

            val watchTrackId = st.pregMatche_firstString("\"watchTrackId\":\"([a-zA-Z0-9_]+?)\"")
            println("watchTrackId=$watchTrackId")
            val accessRightKey = st.pregMatche_firstString("\"accessRightKey\":\"([a-zA-Z0-9_\\.-]+?)\"")
            println("accessRightKey=$accessRightKey")
            var videos = st.pregMatche_strings("\"id\":\"(video-[a-zA-Z0-9]+-\\d+p(?:-low(?:est)?)?)\",\"isAvailable\":true")
            println(videos.joinToString(", "))
            var audios = st.pregMatche_strings("\"id\":\"(audio-[a-zA-Z0-9]+-\\d+kbps)\",\"isAvailable\":true")
            println(audios.joinToString(", "))
            if (watchTrackId != "" && accessRightKey != "" && !videos.isEmpty() && !audios.isEmpty()) {
                var video = ""
                var audio = audios[1]
                if (isWiFiConnected(GLOBAL.APPLICATIONCONTEXT)){
                    video = videos[1]
                }else {
                    video = videos[videos.count() - 1]
                }

                val url = "https://nvapi.nicovideo.jp/v1/watch/"+smNum+"/access-rights/hls?actionTrackId="+watchTrackId
                print("https://nvapi.nicovideo.jp/v1/watch/"+smNum+"/access-rights/hls?actionTrackId="+watchTrackId)
                val headers = mutableMapOf<String,String>()
                headers.put("X-Access-Right-Key",accessRightKey)
                headers.put("X-Frontend-Id", "6")
                headers.put("X-Frontend-Version", "0")
                headers.put("X-Request-With", "https://www.nicovideo.jp")
                val body = "{\"outputs\": [[\"${video}\",\"${audio}\"]]}"
                println(body)
                print("CMAF:post")
                async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, headers = headers.toMap(), eatCookie = true) }.await().let {
                    println("it")
                    it?.let{
                        println(it)
                        val contentUrl = it.pregMatche_firstString("\"contentUrl\":\"(.+?)\"")
                        println(it)
                        callback(contentUrl)
                        return@let
                    }
                    callback("error")
                }
            }
        }
    }

    fun StreamingUrlNicoAccessForThumbMovie(smNum:String, callback: (String) -> Unit) = GlobalScope.launch(Dispatchers.Main) {

        //先にキャッシュ内にあるか確認。あればもうニコニコの動画ページにすらアクセスしないことにする
        if( CachedThumbMovies.containtsWithinExpiration(smNum)){
            callback("cached")
            return@launch
        }
        println("smNum="+smNum)

        val strURL = "https://www.nicovideo.jp/watch/"+smNum
        //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
        async(Dispatchers.Default) { HttpUtil.httpGET(strURL) }.await().let {
            println("net")
            if (it == null) {
                callback("error")
                return@let
            }
            var st = it.pregMatche_firstString("<div id=\"js-initial-watch-data\" data-api-data=\"(.*?)\" hidden></div>")
            if( st=="" ){ return@let }
            st = HtmlCompat.fromHtml(st,HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            //st = st.pregReplace("\\\\u([0-9a-f]{2})([0-9a-f]{2})","%$1%$2")
            //st = st.pregReplace("\\\\","")
            //st = URLDecoder.decode(st,"UTF16")

            println(st)

            val watchTrackId = st.pregMatche_firstString("\"watchTrackId\":\"([a-zA-Z0-9_]+?)\"")
            println("watchTrackId=$watchTrackId")
            val accessRightKey = st.pregMatche_firstString("\"accessRightKey\":\"([a-zA-Z0-9_\\.-]+?)\"")
            println("accessRightKey=$accessRightKey")
            var videos = st.pregMatche_strings("\"id\":\"(video-[a-zA-Z0-9]+-\\d+p(?:-low(?:est)?)?)\",\"isAvailable\":true")
            println(videos.joinToString(", "))
            var audios = st.pregMatche_strings("\"id\":\"(audio-[a-zA-Z0-9]+-\\d+kbps)\",\"isAvailable\":true")
            println(audios.joinToString(", "))
            if (watchTrackId != "" && accessRightKey != "" && !videos.isEmpty() && !audios.isEmpty()) {
                var video = videos[videos.count() - 1]
                var audio = audios[1]

                val url = "https://nvapi.nicovideo.jp/v1/watch/"+smNum+"/access-rights/hls?actionTrackId="+watchTrackId
                print("https://nvapi.nicovideo.jp/v1/watch/"+smNum+"/access-rights/hls?actionTrackId="+watchTrackId)
                val headers = mutableMapOf<String,String>()
                headers.put("X-Access-Right-Key",accessRightKey)
                headers.put("X-Frontend-Id", "6")
                headers.put("X-Frontend-Version", "0")
                headers.put("X-Request-With", "https://www.nicovideo.jp")
                val body = "{\"outputs\": [[\"${video}\",\"${audio}\"]]}"
                println(body)
                print("CMAF:post")
                async(Dispatchers.Default) { HttpUtil.httpPOST(url, body, headers = headers.toMap(), eatCookie = true) }.await().let {
                    println("it")
                    it?.let{
                        println(it)
                        val contentUrl = it.pregMatche_firstString("\"contentUrl\":\"(.+?)\"")
                        println(it)
                        callback(contentUrl)
                        return@let
                    }
                    callback("error")
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun isWiFiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            connectivityManager.activeNetworkInfo!!.type == ConnectivityManager.TYPE_WIFI
        }
    }
}
object CachedMovies {
    //
    data class CachedMovie(var url:String, var smNum: String, var simpleExoPlayer: SimpleExoPlayer )

    var cachedMovies:MutableList<CachedMovie> = mutableListOf()
    fun access(url:String,smNum: String) : SimpleExoPlayer {
        //読み込み済みのものがあればそれを返す
        for ( i in 0 until cachedMovies.count()){
            if( cachedMovies[i].smNum == smNum ){
                cachedMovies.add(cachedMovies[i])
                cachedMovies.removeAt(i)
                while (cachedMovies.count() > USERDATA.cachedMovieNum){
                    cachedMovies[0].simpleExoPlayer.release()
                    cachedMovies.removeAt(0)
                }
                return cachedMovies.last().simpleExoPlayer
            }
        }
        for( bi in 0 until cachedMovies.count() ){
            val i = cachedMovies.count() - bi - 1
            if( cachedMovies[i].simpleExoPlayer.bufferedPercentage < 99 ){
                cachedMovies[i].simpleExoPlayer.release()
                cachedMovies.removeAt(i)
                break
            }
        }
        val loadControl = DefaultLoadControl.Builder().setBackBuffer(10*60*1000, true).createDefaultLoadControl()
        val simpleExoPlayer = SimpleExoPlayer.Builder(GLOBAL.APPLICATIONCONTEXT).setLoadControl(loadControl)
            .build()
        val dataSourceFactory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(GLOBAL.APPLICATIONCONTEXT, GLOBAL.APPLICATIONCONTEXT.packageName)
        )
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        simpleExoPlayer.prepare(mediaSource)

        //simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.volume = 0.2f * USERDATA.SoundVolumeMovie

        val cachedMovie = CachedMovie(url,smNum,simpleExoPlayer)
        cachedMovies.add(cachedMovie)

        while (cachedMovies.count() > USERDATA.cachedMovieNum){
            cachedMovies[0].simpleExoPlayer.release()
            cachedMovies.removeAt(0)
        }

        return simpleExoPlayer
    }
}
//保持しておけるSimpleExoPlayerの数が非常に少ないのでゲームプレイ時に開放する
object CachedThumbMovies {
    //
    class CachedMovie(var url:String, var smNum: String, var simpleExoPlayer: SimpleExoPlayer, var check:Int, var time:Long = System.currentTimeMillis(), var just:Boolean = false )
    //AndroidではCheck機能は死に機能。まぁ３つしかキャッシュできないし、くるくる回してりゃリロードされる。

    var cachedMovies:MutableList<CachedMovie> = mutableListOf()
    fun access(url:String,smNum: String) : SimpleExoPlayer {
        // プレイ指示して、5秒間再生されてない状態が続くとHeartBeat切れとみなしcheck=2になっている(Selectorで)。なのでキャッシュを削除
        // もしくは、もう120秒たったら強制的に読み込みしなおしにしてしまう、か。
        val num = cachedMovies.size
        for( i in 0 until num ){
            val index = num - 1 - i
            if( !withinExpiration(cachedMovies[index]) ){
                cachedMovies[index].simpleExoPlayer.release()
                println("cached削除 - ${cachedMovies[index].smNum}")
                cachedMovies.removeAt(index)
            }
        }

        //読み込み済みのものがあればそれを返す
        for ( i in 0 until cachedMovies.count()){
            if( cachedMovies[i].smNum == smNum ){
                cachedMovies.add(cachedMovies[i])
                cachedMovies.removeAt(i)
                while (cachedMovies.count() > 3){
                    cachedMovies[0].simpleExoPlayer.release()
                    cachedMovies.removeAt(0)
                }
                cachedMovies.last().check = 0
                return cachedMovies.last().simpleExoPlayer
            }
        }

        val loadControl = DefaultLoadControl.Builder().setBackBuffer(10*60*1000, true).createDefaultLoadControl()
        val simpleExoPlayer = SimpleExoPlayer.Builder(GLOBAL.APPLICATIONCONTEXT).setLoadControl(loadControl)
            .build()
        val dataSourceFactory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(GLOBAL.APPLICATIONCONTEXT, GLOBAL.APPLICATIONCONTEXT.packageName)
        )
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        simpleExoPlayer.prepare(mediaSource)

        //simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.volume = 0.2f * USERDATA.SoundVolumeMovie

        val cachedMovie = CachedMovie(url,smNum,simpleExoPlayer,0)
        cachedMovies.add(cachedMovie)

        while (cachedMovies.count() > 3){
            cachedMovies[0].simpleExoPlayer.release()
            cachedMovies.removeAt(0)
        }

        return simpleExoPlayer
    }
    fun withinExpiration(cachedMovie:CachedMovie) : Boolean {
        println("cachedMovie.check=${cachedMovie.check}")
        println("(System.currentTimeMillis() - cachedMovie.time) =${(System.currentTimeMillis() - cachedMovie.time)}")
        println("(System.currentTimeMillis() - cachedMovie.time) < 120000=${((System.currentTimeMillis() - cachedMovie.time) < 120000)}")
        return cachedMovie.check != 2 && (System.currentTimeMillis() - cachedMovie.time) < 120000
    }
    fun containtsWithinExpiration(smNum: String) : Boolean {
        return cachedMovies.any { it.smNum == smNum && withinExpiration(it) }
    }
    //Android用
    fun allRelease(){
        while (cachedMovies.count() > 0){
            cachedMovies[0].simpleExoPlayer.release()
            cachedMovies.removeAt(0)
        }
    }
}
internal class CacheDataSourceFactory(
    private val context: Context,
    private val maxCacheSize: Long,
    private val maxFileSize: Long
) :
    DataSource.Factory {
    private val defaultDatasourceFactory: DefaultDataSourceFactory
    override fun createDataSource(): DataSource {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        val simpleCache = SimpleCache(File(context.cacheDir, "media"), evictor)
        return CacheDataSource(
            simpleCache, defaultDatasourceFactory.createDataSource(),
            FileDataSource(), CacheDataSink(simpleCache, maxFileSize),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }

    init {
        val userAgent: String = Util.getUserAgent(context, "NicoFlick_A")
        val bandwidthMeter = DefaultBandwidthMeter()
        defaultDatasourceFactory = DefaultDataSourceFactory(
            context,
            bandwidthMeter,
            DefaultHttpDataSourceFactory(userAgent, bandwidthMeter)
        )
    }
}