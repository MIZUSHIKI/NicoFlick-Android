package com.mizushiki.nicoflick_a

import android.R
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.core.text.HtmlCompat
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder


class MovieAccess {

    var firstAttack = true

    fun StreamingUrlNicoAccess(smNum:String, callback: (String?) -> Unit) = GlobalScope.launch(Dispatchers.Main) {

        var smNum = smNum
        if( !isWiFiConnected(GLOBAL.APPLICATIONCONTEXT) ){
            if(!smNum.endsWith("?eco=1")){
                smNum += "?eco=1"
            }
        }
        println("smNum="+smNum)
        //if(Userdata.NicoMail=="" || Userdata.NicoPass==""){
            //アカウントなしの場合　
            val strURL = "http://www.nicovideo.jp/watch/"+smNum
            println(strURL)

            //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
            async(Dispatchers.Default) { HttpUtil.httpGET(strURL) }.await().let {
                println("net")
                if (it == null) {
                    callback("error")
                    return@let
                }
                var st = it.pregMatche_firstString("<div id=\"js-initial-watch-data\" data-api-data=\"(.*?)</div>")
                if( st=="" ){ return@let }
                st = st.pregMatche_firstString("(&quot;smileInfo.*?)isSlowLine")
                if( st=="" ){ return@let }
                st = HtmlCompat.fromHtml(st,HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                st = st.pregReplace("\\\\u([0-9a-f]{2})([0-9a-f]{2})","%$1%$2")
                st = st.pregReplace("\\\\","")
                st = URLDecoder.decode(st,"UTF16")
                val nicoDougaURL = st.pregMatche_firstString(""""smileInfo":\{"url":"(.*?)"""")
                if(nicoDougaURL==""){ return@let }

                println(nicoDougaURL)

                //動画の拡張子確認
                var m = nicoDougaURL.pregMatche_firstString("(.)=")

                when( m ){
                    "v" -> {
                        println("FLV!!")
                        //もしFLVならエコノミーを試してみる
                        if( smNum.endsWith("?eco=1") == false ){
                            //StreamingUrlNicoAccess(smNum+"?eco=1", callback) //再帰の仕方がよくわからない...
                            callback("re-eco")
                        }
                    }
                    "s" -> return@let
                    else -> callback(nicoDougaURL)
                }
            }
        //}else {
            //アカウントありの場合
        //}

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

object CachedMovies {
    //いずれ．．．
    data class CachedMovie(var url:String, var simpleExoPlayer: SimpleExoPlayer )

    var cachedMovies:MutableList<CachedMovie> = mutableListOf()
    fun access(url:String) : SimpleExoPlayer {
        //読み込み済みのものがあればそれを返す
        for ( i in 0 until cachedMovies.count()){
            if( cachedMovies[i].url == url ){
                cachedMovies.add(cachedMovies[i])
                cachedMovies.removeAt(i)
                while (cachedMovies.count() > USERDATA.cachedMovieNum){
                    cachedMovies.removeAt(0)
                }
                return cachedMovies.last().simpleExoPlayer
            }
        }
        for( bi in 0 until cachedMovies.count() ){
            val i = cachedMovies.count() - bi - 1
            if( cachedMovies[i].simpleExoPlayer.bufferedPercentage < 99 ){
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

        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.volume = 0.2f

        val cachedMovie = CachedMovie(url,simpleExoPlayer)
        cachedMovies.add(cachedMovie)

        while (cachedMovies.count() > USERDATA.cachedMovieNum){
            cachedMovies.removeAt(0)
        }

        return simpleExoPlayer
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