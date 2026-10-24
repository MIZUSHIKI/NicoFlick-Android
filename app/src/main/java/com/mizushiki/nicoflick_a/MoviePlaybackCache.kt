package com.mizushiki.nicoflick_a

import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.util.Util
import java.io.File

/**
 * Keep reusable media on disk, not ten minutes of consumed samples in each player's heap.
 * Player count/thumbnail prefetch behavior stays with CachedMovies/CachedThumbMovies.
 */
internal object MoviePlaybackCache {
    private const val MIB = 1024 * 1024
    private const val DISK_BYTES = 256L * MIB
    // TEMPORARY diagnostic references must not keep old players/load controls alive.
    private val controls by lazy { java.util.WeakHashMap<DefaultLoadControl, Boolean>() }

    // One process-wide owner: multiple SimpleCache instances cannot share a directory.
    private val diskCache by lazy {
        val context = GLOBAL.APPLICATIONCONTEXT.applicationContext
        SimpleCache(File(context.cacheDir, "playback-media"),
            LeastRecentlyUsedCacheEvictor(DISK_BYTES), ExoDatabaseProvider(context))
    }

    fun dataSourceFactory(): DataSource.Factory {
        val context = GLOBAL.APPLICATIONCONTEXT.applicationContext
        val upstream = DefaultHttpDataSourceFactory(Util.getUserAgent(context, context.packageName))
        // Keep the complete URI as cache key, including signed parameters.
        // Never confuse different HLS renditions or authorization URLs.
        return cachingFactory(diskCache, upstream)
    }

    internal fun cachingFactory(
        cache: com.google.android.exoplayer2.upstream.cache.Cache,
        upstream: DataSource.Factory
    ): DataSource.Factory =
        com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory(
            cache, upstream, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    fun loadControl(thumbnail: Boolean): com.google.android.exoplayer2.LoadControl {
        val byteBudget = (if (thumbnail) 3 else 12) * MIB
        val control = DefaultLoadControl.Builder()
            .setBufferDurationsMs(5_000, 15_000, 1_000, 2_500)
            .setTargetBufferBytes(byteBudget)
            .setPrioritizeTimeOverSizeThresholds(false)
            .setBackBuffer(0, false)
            .createDefaultLoadControl()
        if (SelectorDiagnostics.ENABLED) synchronized(controls) { controls[control] = thumbnail }
        return object : com.google.android.exoplayer2.LoadControl by control {
            private var tracksSelected = false

            override fun onPrepared() {
                tracksSelected = false
                control.onPrepared()
            }

            override fun onTracksSelected(
                renderers: Array<out com.google.android.exoplayer2.Renderer>,
                trackGroups: com.google.android.exoplayer2.source.TrackGroupArray,
                trackSelections: com.google.android.exoplayer2.trackselection.TrackSelectionArray
            ) {
                control.onTracksSelected(renderers, trackGroups, trackSelections)
                tracksSelected = true
            }

            override fun shouldContinueLoading(bufferedDurationUs: Long, playbackSpeed: Float): Boolean {
                // ExoPlayer 2.11 resets its internal byte target to ZERO onPrepared.
                // HLS needs more than one chunk to discover all tracks. Until track selection,
                // use our real budget, otherwise size priority stops loading before preparation.
                return if (!tracksSelected) control.allocator.totalBytesAllocated < byteBudget
                else control.shouldContinueLoading(bufferedDurationUs, playbackSpeed)
            }
        }
    }

    fun recordPlayers() {
        if (!SelectorDiagnostics.ENABLED) return
        val games = CachedMovies.cachedMovies
        val thumbs = CachedThumbMovies.cachedMovies
        var gameBytes = 0
        var thumbBytes = 0
        synchronized(controls) {
            controls.forEach { (control, thumbnail) ->
                if (thumbnail) thumbBytes += control.allocator.totalBytesAllocated
                else gameBytes += control.allocator.totalBytesAllocated
            }
        }
        SelectorDiagnostics.event { "MEDIA_BUFFERS gamePlayers=${games.size} gameBytes=$gameBytes thumbPlayers=${thumbs.size} thumbBytes=$thumbBytes diskBytes=${diskCache.cacheSpace}" }
    }
}
