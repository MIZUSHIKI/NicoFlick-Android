package com.mizushiki.nicoflick_a

import android.R
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
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class MovieAccess(ecoThumb:Boolean = false) {

    var firstAttack = true

    val ecoThumb : Boolean

    init {
        this.ecoThumb = ecoThumb
    }

    fun StreamingUrlNicoAccess(smNum:String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {

        //先にキャッシュ内にあるか確認。あればもうニコニコの動画ページにすらアクセスしないことにする
        if( !ecoThumb ){
            if( CachedMovies.cachedMovies.any{ it.smNum == smNum }){
                callback("cached")
                return@launch
            }
        }else {
            if( CachedThumbMovies.containtsWithinExpiration(smNum)){
                callback("cached")
                return@launch
            }
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
            val nicoDmc = NicoDmc(smNum,st)
            if( nicoDmc.token == "" ){
                return@let
            }

            val url = "https://api.dmc.nico/api/sessions?_format=json"
            val body = nicoDmc.sessionFormatJson
            async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
                println("it")
                if (it != null) {
                    println(it)
                    nicoDmc.Set_session_metadata(it)
                    //短く再生するだけの場合はハートビートしない（40秒以内）
                    if( !ecoThumb ){
                        nicoDmc.Start_HeartBeat()
                    }
                    callback(nicoDmc.content_uri)
                    return@let
                }
                callback("error")
            }
        }
    }

    fun isWiFiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }
}
class NicoDmc(smNum: String, js_initial_watch_data:String) {
    val smNum : String
    var recipeId = ""
        private set
    var playerId = ""
        private set
    var videos = ""
        private set
    var audios = ""
        private set
    var signature = ""
        private set
    var contentId = ""
        private set
    var heartbeatLifetime = ""
        private set
    var contentKeyTimeout = ""
        private set
    var priority = ""
        private set
    var transferPresets = ""
        private set
    var service_id = ""
        private set
    var player_id = ""
        private set
    var service_user_id = ""
        private set
    var auth_type = ""
        private set
    var token = ""
        private set
    var session_metadata = ""
        private set
    var session_id = ""
        private set
    var content_uri = ""
        private set
    var eco = false
    private var timer = Timer()
    init{
        this.smNum = smNum
        println("DMC")
        var ans:ArrayList<String> = arrayListOf()
        if( js_initial_watch_data.pregMatche("\"session\":\\{\"recipeId\":\"(.*?)\",\"playerId\":\"(.*?)\",\"videos\":\\[(.*?)\\],\"audios\":\\[(.*?)\\],", matche = ans)){
            recipeId = ans[1]
            playerId = ans[2]
            videos = ans[3]
            audios = ans[4]
        }
        ans = arrayListOf()
        if( js_initial_watch_data.pregMatche("\"signature\":\"(.*?)\",\"contentId\":\"(.*?)\",\"heartbeatLifetime\":(.*?),\"contentKeyTimeout\":(.*?),\"priority\":(.*?),\"transferPresets\":\\[(.*?)\\],", matche= ans) ){
            signature = ans[1]
            contentId = ans[2]
            heartbeatLifetime = ans[3]
            contentKeyTimeout = ans[4]
            priority = ans[5]
            transferPresets = ans[6]
        }
        ans = arrayListOf()
        if( js_initial_watch_data.pregMatche("\\\\\"service_id\\\\\":\\\\\"(.*?)\\\\\",\\\\\"player_id\\\\\":\\\\\"(.*?)\\\\\",\\\\\"recipe_id\\\\\":\\\\\".*?\\\\\",\\\\\"service_user_id\\\\\":\\\\\"(.*?)\\\\\",\\\\\"protocols\\\\\":\\[\\{\\\\\"name\\\\\":\\\\\"http\\\\\",\\\\\"auth_type\\\\\":\\\\\"(.*?)\\\\\"\\}", matche= ans) ){
            service_id = ans[1]
            player_id = ans[2]
            service_user_id = ans[3]
            auth_type = ans[4]
        }
        ans = arrayListOf()
        if( js_initial_watch_data.pregMatche("\"token\":\"(.*?\\\\\"transfer_presets\\\\\":.*?\\})\",", matche= ans) ){
            token = ans[1]
        }
        println("recipeId= ${recipeId}")
        println("playerId= ${playerId}")
        println("videos= ${videos}")
        println("audios= ${audios}")
        println("signature= ${signature}")
        println("contentId= ${contentId}")
        println("heartbeatLifetime= ${heartbeatLifetime}")
        println("contentKeyTimeout= ${contentKeyTimeout}")
        println("transferPresets= ${transferPresets}")
        println("service_id= ${service_id}")
        println("player_id= ${player_id}")
        println("service_user_id= ${service_user_id}")
        println("auth_type= ${auth_type}")
        println("token= ${token}")
    }
    var sessionFormatJson : String = ""
        get() {
            var video_src = videos
            val audio_src = audios
            println("video_src=${video_src}")
            if( eco ){
                //強制eco(曲セレクト画面での30秒プレビューに使用)
                video_src = video_src.pregReplace("^.*,","")
                println("video_src=${video_src}")
                //hls
                return """{"session":{"recipe_id":"${recipeId}","content_id":"${contentId}","content_type":"movie","content_src_id_sets":[{"content_src_ids":[{"src_id_to_mux":{"video_src_ids":[${video_src}],"audio_src_ids":[${audio_src}]}}]}],"timing_constraint":"unlimited","keep_method":{"heartbeat":{"lifetime":${heartbeatLifetime}}},"protocol":{"name":"http","parameters":{"http_parameters":{"parameters":{"hls_parameters":{"use_well_known_port":"yes","use_ssl":"yes","transfer_preset":"${transferPresets}","segment_duration":6000}}}}},"content_uri":"","session_operation_auth":{"session_operation_auth_by_signature":{"token":"${token}","signature":"${signature}"}},"content_auth":{"auth_type":"${auth_type}","content_key_timeout":${contentKeyTimeout},"service_id":"${service_id}","service_user_id":"${service_user_id}"},"client_info":{"player_id":"${playerId}"},"priority":${priority}}}"""

            }else {
                if( !MovieAccess().isWiFiConnected(GLOBAL.APPLICATIONCONTEXT) ){
                    //Wifi接続なし
                    video_src = video_src.pregReplace("^.*,","")
                    println("video_src=${video_src}")
                }
                //mp4
                return """{"session":{"recipe_id":"${recipeId}","content_id":"${contentId}","content_type":"movie","content_src_id_sets":[{"content_src_ids":[{"src_id_to_mux":{"video_src_ids":[${video_src}],"audio_src_ids":[${audio_src}]}}]}],"timing_constraint":"unlimited","keep_method":{"heartbeat":{"lifetime":${heartbeatLifetime}}},"protocol":{"name":"http","parameters":{"http_parameters":{"parameters":{"http_output_download_parameters":{"use_well_known_port":"yes","use_ssl":"yes","transfer_preset":"${transferPresets}"}}}}},"content_uri":"","session_operation_auth":{"session_operation_auth_by_signature":{"token":"${token}","signature":"${signature}"}},"content_auth":{"auth_type":"${auth_type}","content_key_timeout":${contentKeyTimeout},"service_id":"${service_id}","service_user_id":"${service_user_id}"},"client_info":{"player_id":"${playerId}"},"priority":${priority}}}"""
            }
        }
    fun Set_session_metadata(ResponseMetadata:String){
        session_metadata = ResponseMetadata.pregMatche_firstString("\"data\":(.*)\\}$")
        session_id = session_metadata.pregMatche_firstString("\"id\":\"(.*?)\",")
        content_uri = session_metadata.pregMatche_firstString("\"content_uri\":\"(.*?)\",").pregReplace("\\\\", "")
        session_metadata = session_metadata.pregReplace("\"content_uri\":\"(.*?)\",", "\"content_uri\":\"${content_uri}\",")
    }
    fun Start_HeartBeat() {
        runBlocking {
            val url = "https://api.dmc.nico/api/sessions/${session_id}?_format=json&_method=PUT"
            val body = ""
            async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
                Post_HeartBeat()
                timer.schedule(40000, 40000, {
                     Post_HeartBeat()
                     println("smNum=${smNum}")
                     println("CachedMovies.cachedMovies.any{ it.smNum == smNum } ")
                     println(CachedMovies.cachedMovies.any{ it.smNum == smNum } )
                     for( m in CachedMovies.cachedMovies ){
                         println(m.smNum)
                     }
                     if( !CachedMovies.cachedMovies.any{ it.smNum == smNum } ){
                         println("別のをダウンロードに行ってキャッシュから消されてたら終わり")
                         End_HeartBeat()
                     }else {
                         CachedMovies.cachedMovies.indexOfLast { it.smNum == smNum }.let {
                             if( it != -1 ){
                                 if( CachedMovies.cachedMovies[it].simpleExoPlayer.bufferedPercentage >= 99 ){
                                     println("ロード終わり")
                                     End_HeartBeat()
                                 }
                             }
                         }
                     }
                })

            }
        }
    }
    fun Post_HeartBeat() {
        runBlocking {
            val url = "https://api.dmc.nico/api/sessions/${session_id}?_format=json&_method=PUT"
            val body = session_metadata
            async(Dispatchers.Default) { HttpUtil.httpPOST(url, body) }.await().let {
                if (it != null) {
                    println("HeartBeat Return!")
                    println(it)
                    Set_session_metadata(it)
                }
            }
        }
    }
    fun End_HeartBeat() {
        timer.cancel()
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
        val dataSourceFactory = DefaultDataSourceFactory(
            GLOBAL.APPLICATIONCONTEXT,
            Util.getUserAgent(GLOBAL.APPLICATIONCONTEXT, GLOBAL.APPLICATIONCONTEXT.packageName)
        )
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        simpleExoPlayer.prepare(mediaSource)

        //simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.volume = 0.2f

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
        val dataSourceFactory = DefaultDataSourceFactory(
            GLOBAL.APPLICATIONCONTEXT,
            Util.getUserAgent(GLOBAL.APPLICATIONCONTEXT, GLOBAL.APPLICATIONCONTEXT.packageName)
        )
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        simpleExoPlayer.prepare(mediaSource)

        //simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.volume = 0.2f

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