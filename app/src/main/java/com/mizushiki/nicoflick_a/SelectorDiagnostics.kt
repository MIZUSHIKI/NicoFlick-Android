package com.mizushiki.nicoflick_a

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * TEMPORARY investigation. Remove this file and all SelectorDiagnostics calls after diagnosis.
 * No score bodies, user identifiers, URLs or exception messages are recorded.
 * files/selector-diagnostics/{current,previous}.log, at most about 2 MiB total.
 */
internal object SelectorDiagnostics {
    val ENABLED: Boolean get() = DiagnosticSettings.enabled
    private val writer by lazy {
        ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            ArrayBlockingQueue<Runnable>(256), ThreadPoolExecutor.DiscardPolicy())
    }
    private val lock by lazy { Any() }
    @PublishedApi @Volatile internal var directory: File? = null
    @PublishedApi internal val sequence by lazy { AtomicLong() }
    private val heartbeat by lazy { AtomicLong(SystemClock.uptimeMillis()) }

    inline fun event(message: () -> String) {
        if (ENABLED) event(message())
    }

    fun event(message: String) {
        if (!ENABLED || directory == null) return
        try {
            val runtime = Runtime.getRuntime()
            val line = "${System.currentTimeMillis()} uptime=${SystemClock.uptimeMillis()} pid=${android.os.Process.myPid()} thread=${Thread.currentThread().name} heap=${runtime.totalMemory() - runtime.freeMemory()}/${runtime.maxMemory()} native=${android.os.Debug.getNativeHeapAllocatedSize()} ${message.take(2000)}"
            Log.i("SelectorDiag", line)
            writer.execute { append(line) }
        } catch (_: Throwable) { /* Diagnosis must not affect application behavior. */ }
    }

    private fun append(line: String) {
        try {
            synchronized(lock) {
                val dir = directory ?: return
                dir.mkdirs()
                val current = File(dir, "current.log")
                if (current.length() >= 1024 * 1024) {
                    val previous = File(dir, "previous.log")
                    previous.delete()
                    current.renameTo(previous)
                }
                current.appendText(line + "\n")
            }
        } catch (_: Throwable) { }
    }

    inline fun <T> measure(name: String, action: () -> T): T {
        if (!ENABLED || directory == null) return action()
        val id = sequence.incrementAndGet()
        val start = SystemClock.elapsedRealtime()
        event("BEGIN id=$id $name")
        try {
            val result = action()
            event("END id=$id $name ms=${SystemClock.elapsedRealtime() - start}")
            return result
        } catch (error: Throwable) {
            event("FAIL id=$id $name ms=${SystemClock.elapsedRealtime() - start} type=${error.javaClass.name}")
            throw error
        }
    }

    fun install(app: Application) {
        if (!ENABLED) return
        directory = File(app.filesDir, "selector-diagnostics")
        event("PROCESS_START version=${BuildConfig.VERSION_NAME} sdk=${Build.VERSION.SDK_INT} model=${Build.MODEL}")
        val original = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                // Synchronous best effort: the asynchronous queue may not survive a crash.
                append("CRASH time=${System.currentTimeMillis()} pid=${android.os.Process.myPid()} thread=${thread.name} type=${error.javaClass.name} frames=${error.stackTrace.take(12).joinToString()}")
            } finally {
                original?.uncaughtException(thread, error)
            }
        }
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            private fun record(a: Activity, state: String) {
                val selector = a as? Activity_Selector
                event("ACTIVITY ${a.javaClass.simpleName}@${System.identityHashCode(a)} $state finishing=${a.isFinishing} changingConfig=${a.isChangingConfigurations}" +
                    (selector?.let { " index=${it.indexCoverFlow} music=${it.currentMusics.getOrNull(it.indexCoverFlow)?.sqlID} levelIndex=${it.indexPicker}" } ?: ""))
            }
            override fun onActivityCreated(a: Activity, state: Bundle?) = record(a, "created restored=${state != null}")
            override fun onActivityStarted(a: Activity) = record(a, "started")
            override fun onActivityResumed(a: Activity) = record(a, "resumed")
            override fun onActivityPaused(a: Activity) = record(a, "paused")
            override fun onActivityStopped(a: Activity) = record(a, "stopped")
            override fun onActivitySaveInstanceState(a: Activity, state: Bundle) = record(a, "saveState")
            override fun onActivityDestroyed(a: Activity) = record(a, "destroyed")
        })
        app.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) = event("TRIM_MEMORY level=$level")
            override fun onLowMemory() = event("LOW_MEMORY")
            override fun onConfigurationChanged(config: Configuration) = event("CONFIGURATION_CHANGED")
        })
        val handler = Handler(Looper.getMainLooper())
        val beat = object : Runnable {
            override fun run() {
                heartbeat.set(SystemClock.uptimeMillis())
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(beat)
        // One pending heartbeat only; no growing queue during a main-thread stall.
        Thread({
            var reported = false
            while (true) {
                try { Thread.sleep(2000) } catch (_: InterruptedException) { return@Thread }
                val delay = SystemClock.uptimeMillis() - heartbeat.get()
                if (delay >= 4000 && !reported) {
                    event("MAIN_STALL delayMs=$delay frames=${Looper.getMainLooper().thread.stackTrace.take(18).joinToString()}")
                    reported = true
                } else if (delay < 4000 && reported) {
                    event("MAIN_RECOVERED")
                    reported = false
                }
            }
        }, "SelectorDiagWatchdog").apply { isDaemon = true; start() }
        writer.execute {
            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    val manager = app.getSystemService(ActivityManager::class.java)
                    manager.getHistoricalProcessExitReasons(app.packageName, 0, 5).forEach {
                        append("PROCESS_EXIT time=${it.timestamp} pid=${it.pid} reason=${it.reason} status=${it.status} importance=${it.importance} pssKb=${it.pss} rssKb=${it.rss}")
                    }
                } catch (_: Throwable) { }
            }
        }
    }
}
