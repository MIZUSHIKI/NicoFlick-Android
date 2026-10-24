package com.mizushiki.nicoflick_a

import java.net.URI

internal object MediaInput {
    data class Thumbnail(val url: String, val width: Int, val height: Int)

    fun thumbnail(raw: String): Thumbnail? {
        var url = raw.trim()
        val originalSize = url.endsWith("?")
        if (originalSize) url = url.dropLast(1)
        val uri = try { URI(url) } catch (_: Exception) { return null }
        if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank()) return null
        val id = (Regex("/thumbnails/(\\d+)/").find(url)
            ?: Regex("\\?i=(\\d+)").find(url))?.groupValues?.get(1)?.toLongOrNull()
        val large = !originalSize && id != null && id >= 16371845L
        if (large && !url.endsWith(".L")) url += ".L"
        return Thumbnail(url, if (large) 360 else 130, if (large) 200 else 74)
    }

    fun preferredFormat(formats: List<String>): String? = formats.getOrNull(1) ?: formats.firstOrNull()

    fun watchId(url: String): String? =
        Regex("watch/((?:sm|nm|so)?[0-9]+)(?:[?#].*)?$").find(url.trim())?.groupValues?.get(1)
}
