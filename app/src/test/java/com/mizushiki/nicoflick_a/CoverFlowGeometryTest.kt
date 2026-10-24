package com.mizushiki.nicoflick_a

import it.moondroid.coverflow.components.ui.containers.CoverFlowGeometry
import org.junit.Assert.assertEquals
import org.junit.Test

class CoverFlowGeometryTest {
    @Test fun leftNeighboursKeepTheCenteredFirstItemInPlaceAfterRelayout() {
        // Selector's 240dp covers with spacing 0.4: the old calculation moved
        // the first item by 48px after two prepends, selecting the next item.
        val width = 240
        val spacing = 0.4f
        val step = (width * spacing).toInt()
        val firstLeft = -step
        val firstCenter = firstLeft + width / 2
        var right = firstLeft + width - step
        var leftmost = firstLeft
        repeat(2) {
            leftmost = right - width
            right = CoverFlowGeometry.precedingRight(right, width, spacing)
        }
        val firstLeftAfterRelayout = leftmost + 2 * step
        assertEquals(firstLeft, firstLeftAfterRelayout)
        assertEquals(firstCenter, firstLeftAfterRelayout + width / 2)
    }

    @Test fun prependingAndRelayoutAgreeAcrossDensitiesAndSpacing() {
        for (width in listOf(240, 480, 630, 720)) {
            for (spacing in listOf(0.25f, 0.4f, 0.5f, 0.75f, 1f)) {
                for (count in 1..12) {
                    val step = (width * spacing).toInt()
                    val originalLeft = -step
                    var right = originalLeft + width - step
                    var leftmost = originalLeft
                    repeat(count) {
                        leftmost = right - width
                        right = CoverFlowGeometry.precedingRight(right, width, spacing)
                    }
                    assertEquals(originalLeft, leftmost + count * step)
                }
            }
        }
    }
}
