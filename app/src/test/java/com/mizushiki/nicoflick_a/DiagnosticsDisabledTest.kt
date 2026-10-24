package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DiagnosticsDisabledTest {
    @Test fun disabledDiagnosticsSkipPayloadsFilesAndCrashHandlerButStillRunApplicationWork() {
        assertFalse("Normal builds should leave diagnostics off", DiagnosticSettings.enabled)
        val app = RuntimeEnvironment.getApplication()
        val directory = File(app.filesDir, "selector-diagnostics")
        directory.deleteRecursively()
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        SelectorDiagnostics.install(app)
        assertSame(handler, Thread.getDefaultUncaughtExceptionHandler())
        SelectorDiagnostics.event { error("Disabled log must not compute its payload") }
        MovieLoadLog.log("test") { error("Disabled movie log must not compute its payload") }
        assertEquals("", MovieLoadLog.id("test", "movie"))
        assertSame(okhttp3.EventListener.NONE, MovieLoadLog.httpEvents("test"))
        var executions = 0
        assertEquals(42, SelectorDiagnostics.measure("test") { executions++; 42 })
        assertEquals(1, executions)
        assertFalse(directory.exists())
        assertFalse(Thread.getAllStackTraces().keys.any {
            it.isAlive && it.name == "SelectorDiagWatchdog"
        })
    }
}
