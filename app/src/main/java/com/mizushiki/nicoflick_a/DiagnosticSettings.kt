package com.mizushiki.nicoflick_a

/**
 * Set REQUESTED=true and rebuild debug only when investigating a problem.
 * Release builds never enable these diagnostics, even if REQUESTED is left true.
 */
internal object DiagnosticSettings {
    const val REQUESTED = false
    val enabled: Boolean get() = BuildConfig.DEBUG && REQUESTED
}
