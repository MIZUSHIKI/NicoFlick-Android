package com.mizushiki.nicoflick_a

import android.net.Uri
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MoviePlaybackCacheTest {
    @Test fun consumedMediaCanBeReplayedAndSoughtWithoutNetworkAfterClosingSource() {
        val context = RuntimeEnvironment.getApplication()
        val directory = File(context.cacheDir, "media-test-" + UUID.randomUUID())
        val cache = SimpleCache(directory, LeastRecentlyUsedCacheEvictor(1024 * 1024),
            ExoDatabaseProvider(context))
        val payload = ByteArray(4096) { (it % 251).toByte() }
        val uri = Uri.parse("https://example.invalid/video/segment.ts?token=one")
        try {
            val network = DataSource.Factory { ByteArrayDataSource(payload) }
            val first = MoviePlaybackCache.cachingFactory(cache, network)
            assertArrayEquals(payload, read(first, DataSpec(uri)))

            // A new source has no RAM samples from the first read. All bytes must come from disk.
            val offline = DataSource.Factory {
                val delegate = ByteArrayDataSource(payload)
                object : DataSource by delegate {
                    override fun open(spec: DataSpec): Long = throw IOException("Network unavailable")
                }
            }
            val replay = MoviePlaybackCache.cachingFactory(cache, offline)
            assertArrayEquals(payload, read(replay, DataSpec(uri)))
            assertArrayEquals(payload.copyOfRange(128, 384),
                read(replay, DataSpec(uri).subrange(128, 256)))

            // Different signed URLs must never silently receive another URL's cached response.
            try {
                read(replay, DataSpec(Uri.parse("https://example.invalid/video/segment.ts?token=two")))
                fail("Different URI should require the unavailable upstream")
            } catch (_: IOException) { }
        } finally {
            cache.release()
            directory.deleteRecursively()
        }
    }

    @Test fun preparationLoadsBeforeTracksHaveBeenSelected() {
        for (thumbnail in listOf(false, true)) {
            val control = MoviePlaybackCache.loadControl(thumbnail)
            control.onPrepared()
            try {
                // HLS must load enough audio/video to prepare before onTracksSelected is called.
                assertTrue("Preparation must not treat an unset track target as a full buffer",
                    control.shouldContinueLoading(0, 1f))
            } finally {
                control.onReleased()
            }
        }
    }

    @Test fun byteBudgetStopsLoadingEvenWhenTimeBufferIsShortAndReleaseFreesSamples() {
        for (thumbnail in listOf(false, true)) {
            val control = MoviePlaybackCache.loadControl(thumbnail)
            control.onPrepared()
            control.onTracksSelected(emptyArray(), TrackGroupArray.EMPTY, TrackSelectionArray())
            val allocator = control.allocator
            val allocations = mutableListOf<com.google.android.exoplayer2.upstream.Allocation>()
            try {
                assertTrue(control.shouldContinueLoading(0, 1f))
                var bytes = 0
                while (control.shouldContinueLoading(0, 1f) && bytes < 16 * 1024 * 1024) {
                    allocations.add(allocator.allocate())
                    bytes = allocator.totalBytesAllocated
                }
                assertFalse("Size budget must take priority over the minimum buffer duration",
                    control.shouldContinueLoading(0, 1f))
                assertEquals((if (thumbnail) 3 else 12) * 1024 * 1024, bytes)
                assertEquals(0L, control.backBufferDurationUs)
            } finally {
                allocations.forEach { allocator.release(it) }
                control.onReleased()
            }
            assertEquals(0, allocator.totalBytesAllocated)
        }
    }

    private fun read(factory: DataSource.Factory, spec: DataSpec): ByteArray {
        val source = factory.createDataSource()
        try {
            source.open(spec)
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(512)
            while (true) {
                val count = source.read(buffer, 0, buffer.size)
                if (count == -1) break
                result.write(buffer, 0, count)
            }
            return result.toByteArray()
        } finally {
            source.close()
        }
    }
}
