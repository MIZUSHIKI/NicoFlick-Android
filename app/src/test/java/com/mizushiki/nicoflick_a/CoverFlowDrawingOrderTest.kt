package com.mizushiki.nicoflick_a

import it.moondroid.coverflow.components.ui.containers.CoverFlowDrawingOrder
import org.junit.Assert.*
import org.junit.Test

class CoverFlowDrawingOrderTest {
    @Test fun allCentersProduceCompleteUniquePermutation() {
        for (count in 1..64) for (center in 0 until count) {
            val order = CoverFlowDrawingOrder.create(count, center)
            assertEquals(count, order.size)
            assertEquals((0 until count).toList(), order.sorted())
            assertEquals(center, order.last())
        }
    }

    @Test fun retainsLeftThenReverseRightDrawingAppearance() {
        assertArrayEquals(intArrayOf(0, 1, 4, 3, 2), CoverFlowDrawingOrder.create(5, 2))
        assertArrayEquals(intArrayOf(4, 3, 2, 1, 0), CoverFlowDrawingOrder.create(5, 0))
        assertArrayEquals(intArrayOf(0, 1, 2, 3, 4), CoverFlowDrawingOrder.create(5, 4))
    }

    @Test fun emptySingleAndOutdatedCentersAreSafe() {
        assertArrayEquals(intArrayOf(), CoverFlowDrawingOrder.create(0, -1))
        assertArrayEquals(intArrayOf(0), CoverFlowDrawingOrder.create(1, 9))
        assertArrayEquals(intArrayOf(2, 1, 0), CoverFlowDrawingOrder.create(3, -1))
        assertArrayEquals(intArrayOf(0, 1, 2), CoverFlowDrawingOrder.create(3, 100))
    }

    @Test fun repeatedDrawAndTouchCalculationsDoNotShareState() {
        val expected = CoverFlowDrawingOrder.create(8, 3)
        repeat(100) {
            CoverFlowDrawingOrder.create(2, 1)
            assertArrayEquals(expected, CoverFlowDrawingOrder.create(8, 3))
        }
    }
}
