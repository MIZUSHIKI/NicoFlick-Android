package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class MediaInputTest {
    @Test fun invalidThumbnailsAreRejectedWithoutThrowing() {
        for (url in listOf("", " ", "?", "https://", "invalid", "https://host/a b", "file:///tmp/image")) {
            assertNull(url, MediaInput.thumbnail(url))
        }
    }

    @Test fun newerThumbnailUsesLargeSize() {
        assertEquals(MediaInput.Thumbnail("https://tn.example/thumbnails/16371845/a.L", 360, 200),
            MediaInput.thumbnail("https://tn.example/thumbnails/16371845/a"))
        assertEquals(MediaInput.Thumbnail("https://tn.example/?i=16371845.L", 360, 200),
            MediaInput.thumbnail("https://tn.example/?i=16371845"))
    }

    @Test fun originalSizeMarkerAndOlderImagesKeepSmallSize() {
        assertEquals(MediaInput.Thumbnail("https://tn.example/thumbnails/16371845/a", 130, 74),
            MediaInput.thumbnail("https://tn.example/thumbnails/16371845/a?"))
        assertEquals(MediaInput.Thumbnail("https://tn.example/?i=123", 130, 74),
            MediaInput.thumbnail("https://tn.example/?i=123"))
    }

    @Test fun overflowingThumbnailIdIsSafe() {
        assertNotNull(MediaInput.thumbnail("https://tn.example/thumbnails/99999999999999999999999999/a"))
    }

    @Test fun onlyAvailableFormatIsUsedAndNormalPreferenceIsPreserved() {
        assertNull(MediaInput.preferredFormat(emptyList()))
        assertEquals("only", MediaInput.preferredFormat(listOf("only")))
        assertEquals("second", MediaInput.preferredFormat(listOf("first", "second", "third")))
    }

    @Test fun watchIdExcludesQueryAndFragment() {
        for (id in listOf("sm123", "nm123", "so123", "1234567890")) {
            assertEquals(id, MediaInput.watchId("https://www.nicovideo.jp/watch/$id"))
            assertEquals(id, MediaInput.watchId("https://www.nicovideo.jp/watch/$id?from=selector#top"))
        }
    }

    @Test fun invalidWatchIdsAreRejected() {
        for (url in listOf("", "watch/", "watch/sm", "watch/sm123/extra", "watch/invalid")) {
            assertNull(MediaInput.watchId(url))
        }
    }
}
