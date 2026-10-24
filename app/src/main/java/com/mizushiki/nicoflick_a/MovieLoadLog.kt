package com.mizushiki.nicoflick_a

import android.os.SystemClock
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.HttpDataSource
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Response
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicLong

/** Logcat filter: tag:MovieLoad */
internal object MovieLoadLog {
    private val sequence by lazy { AtomicLong() }
    fun id(kind: String, movie: String) =
        if (DiagnosticSettings.enabled) "$kind/$movie/${sequence.incrementAndGet()}" else ""
    inline fun log(id: String, message: () -> String) {
        if (DiagnosticSettings.enabled) log(id, message())
    }
    fun log(id: String, message: String) {
        if (!DiagnosticSettings.enabled) return
        Log.d("MovieLoad", "[$id] t=${SystemClock.elapsedRealtime()}ms ${Thread.currentThread().name} $message")
    }
    fun failure(id: String, error: Throwable) {
        if (!DiagnosticSettings.enabled) return
        // Exception messages can contain signed URLs. Record types and HTTP codes instead.
        var cause: Throwable? = error
        repeat(8) {
            val current = cause ?: return
            val status = (current as? HttpDataSource.InvalidResponseCodeException)?.responseCode
            log(id, "ERROR cause=${current.javaClass.name} httpStatus=$status at=${current.stackTrace.firstOrNull()}")
            cause = current.cause
        }
    }
    fun httpEvents(id: String): EventListener {
        if (!DiagnosticSettings.enabled) return EventListener.NONE
        return object : EventListener() {
        override fun dnsStart(call: Call, domainName: String) { log(id, "DNS_START host=$domainName") }
        override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
            log(id, "DNS_END host=$domainName addresses=${inetAddressList.size}")
        }
        override fun connectStart(call: Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
            log(id, "CONNECT_START host=${inetSocketAddress.hostString}")
        }
        override fun secureConnectStart(call: Call) { log(id, "TLS_START") }
        override fun secureConnectEnd(call: Call, handshake: okhttp3.Handshake?) { log(id, "TLS_END") }
        override fun connectionAcquired(call: Call, connection: okhttp3.Connection) {
            log(id, "CONNECTION_ACQUIRED protocol=${connection.protocol()}")
        }
        override fun requestHeadersStart(call: Call) { log(id, "REQUEST_HEADERS_START") }
        override fun responseHeadersEnd(call: Call, response: Response) {
            log(id, "RESPONSE_HEADERS status=${response.code} retryAfter=${response.header("Retry-After")}")
        }
        override fun callEnd(call: Call) { log(id, "HTTP_CALL_END") }
        override fun callFailed(call: Call, ioe: java.io.IOException) { log(id, "HTTP_CALL_FAILED"); failure(id, ioe) }
    }
    }
    fun observe(player: SimpleExoPlayer, kind: String, movie: String) {
        if (!DiagnosticSettings.enabled) return
        val id = id(kind, movie)
        val started = SystemClock.elapsedRealtime()
        fun snapshot(event: String) {
            val state = when (player.playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> player.playbackState.toString()
            }
            log(id, "$event elapsed=${SystemClock.elapsedRealtime() - started}ms state=$state playWhenReady=${player.playWhenReady} position=${player.currentPosition} buffered=${player.bufferedPosition} duration=${player.duration}")
        }
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                snapshot("STATE")
                if (SelectorDiagnostics.ENABLED) MoviePlaybackCache.recordPlayers()
            }
            override fun onLoadingChanged(isLoading: Boolean) { snapshot("LOADING=$isLoading") }
            override fun onIsPlayingChanged(isPlaying: Boolean) { snapshot("IS_PLAYING=$isPlaying") }
            override fun onPlayerError(error: ExoPlaybackException) {
                snapshot("PLAYER_ERROR type=${error.type}")
                failure(id, error)
            }
        })
        player.addAnalyticsListener(object : com.google.android.exoplayer2.analytics.AnalyticsListener {
            override fun onLoadStarted(
                eventTime: com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime,
                loadEventInfo: com.google.android.exoplayer2.source.MediaSourceEventListener.LoadEventInfo,
                mediaLoadData: com.google.android.exoplayer2.source.MediaSourceEventListener.MediaLoadData
            ) {
                log(id, "MEDIA_LOAD_START type=${mediaLoadData.dataType} host=${loadEventInfo.uri.host}")
            }
            override fun onLoadCompleted(
                eventTime: com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime,
                loadEventInfo: com.google.android.exoplayer2.source.MediaSourceEventListener.LoadEventInfo,
                mediaLoadData: com.google.android.exoplayer2.source.MediaSourceEventListener.MediaLoadData
            ) {
                log(id, "MEDIA_LOAD_END type=${mediaLoadData.dataType} host=${loadEventInfo.uri.host} duration=${loadEventInfo.loadDurationMs}ms bytes=${loadEventInfo.bytesLoaded}")
            }
            override fun onLoadError(
                eventTime: com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime,
                loadEventInfo: com.google.android.exoplayer2.source.MediaSourceEventListener.LoadEventInfo,
                mediaLoadData: com.google.android.exoplayer2.source.MediaSourceEventListener.MediaLoadData,
                error: java.io.IOException,
                wasCanceled: Boolean
            ) {
                log(id, "MEDIA_LOAD_ERROR type=${mediaLoadData.dataType} host=${loadEventInfo.uri.host} duration=${loadEventInfo.loadDurationMs}ms bytes=${loadEventInfo.bytesLoaded} canceled=$wasCanceled")
                failure(id, error)
            }
            override fun onLoadCanceled(
                eventTime: com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime,
                loadEventInfo: com.google.android.exoplayer2.source.MediaSourceEventListener.LoadEventInfo,
                mediaLoadData: com.google.android.exoplayer2.source.MediaSourceEventListener.MediaLoadData
            ) {
                log(id, "MEDIA_LOAD_CANCELED type=${mediaLoadData.dataType} duration=${loadEventInfo.loadDurationMs}ms")
            }
        })
        snapshot("PLAYER_CREATED")
    }
}
