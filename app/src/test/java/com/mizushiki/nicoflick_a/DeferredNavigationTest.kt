package com.mizushiki.nicoflick_a

import org.junit.Assert.assertEquals
import org.junit.Test

class DeferredNavigationTest {
    @Test fun completionInBackgroundWaitsForResumeAndRunsOnce() {
        val navigation = DeferredNavigation()
        var launches = 0
        navigation.submit { launches++ }
        assertEquals(0, launches)
        navigation.resume()
        navigation.resume()
        assertEquals(1, launches)
    }

    @Test fun completionAfterPauseDoesNotOpenScreenUntilReturn() {
        val navigation = DeferredNavigation()
        var launches = 0
        navigation.resume()
        navigation.pause()
        navigation.submit { launches++ }
        assertEquals(0, launches)
        navigation.resume()
        assertEquals(1, launches)
    }

    @Test fun destroyedScreenDiscardsPendingAndFutureCompletions() {
        val navigation = DeferredNavigation()
        var launches = 0
        navigation.submit { launches++ }
        navigation.destroy()
        navigation.submit { launches++ }
        navigation.resume()
        assertEquals(0, launches)
    }

    @Test fun visibleCompletionIsConsumedBeforeActionPausesScreen() {
        val navigation = DeferredNavigation()
        var launches = 0
        navigation.resume()
        navigation.submit { launches++; navigation.pause() }
        navigation.resume()
        assertEquals(1, launches)
    }
}
