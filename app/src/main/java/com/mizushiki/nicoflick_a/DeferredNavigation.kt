package com.mizushiki.nicoflick_a

/** Mainスレッドで使用する。非表示中の画面起動を保留し、破棄後は実行しない。 */
internal class DeferredNavigation {
    private var resumed = false
    private var destroyed = false
    private var pending: (() -> Unit)? = null

    fun submit(action: () -> Unit) {
        if (destroyed) return
        pending = action
        drain()
    }

    fun resume() { resumed = true; drain() }
    fun pause() { resumed = false }
    fun destroy() { destroyed = true; resumed = false; pending = null }
    private fun drain() {
        if (!resumed || destroyed) return
        val action = pending ?: return
        pending = null
        action()
    }
}
