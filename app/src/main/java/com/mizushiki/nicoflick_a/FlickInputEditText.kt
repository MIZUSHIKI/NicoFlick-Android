package com.mizushiki.nicoflick_a

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.TextAttribute
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText

/**
 * Uses the user's IME while treating its text changes as game input.
 * The standard EditText connection remains responsible for selection, composing spans,
 * surrounding-text queries and batch notifications. Never restart the IME per character.
 */
class FlickInputEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {
    var captureInputTime: (() -> Double?)? = null
    var onFlickInput: ((String, Double?) -> Unit)? = null

    private enum class Operation { COMPOSING, COMMIT, KEY, DELETE, FINISH, REGION, FALLBACK }
    private var connection: InputConnection? = null
    private var generation = 0
    private var operationDepth = 0
    private var batchDepth = 0
    private var internalEdit = false
    private var lastInputUptime = 0L
    private var lastClearUptime = -FlickInputBufferPolicy.MIN_CLEAR_INTERVAL_MS
    private var clearRequested = false
    private var fallbackBefore = ""
    private var fallbackTime: Double? = null
    private var fallbackStartNanos = 0L

    // Debug-only bounded samples; no lyrics/input text are retained in diagnostics.
    private val durationsUs = LongArray(128)
    private val operationCounts = IntArray(Operation.values().size)
    private var sampleCount = 0L
    private var maxBufferLength = 0
    private var rolloverCount = 0
    private var invalidInputCount = 0

    private val maintenance = Runnable { maintainBuffer() }

    init {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        imeOptions = gameImeOptions(imeOptions)
        isSingleLine = true
        isCursorVisible = false
        setTextIsSelectable(false)
        // Also cover hardware keys and IME paths that modify the Editable without commitText.
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (internalEdit || operationDepth != 0) return
                fallbackStartNanos = SystemClock.elapsedRealtimeNanos()
                fallbackTime = captureInputTime?.invoke()
                fallbackBefore = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (internalEdit || operationDepth != 0 || !isEnabled || !hasFocus()) return
                val after = s?.toString() ?: ""
                emit(FlickTextChange.detect(fallbackBefore, after), fallbackTime)
                record(Operation.FALLBACK, after.length, fallbackStartNanos)
                lastInputUptime = SystemClock.uptimeMillis()
                scheduleMaintenance()
            }
        })
    }

    private fun gameImeOptions(options: Int): Int {
        var result = options or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = result or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        return result
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val target = super.onCreateInputConnection(outAttrs) ?: return null
        outAttrs.imeOptions = gameImeOptions(outAttrs.imeOptions)
        outAttrs.inputType = outAttrs.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        connection = target
        batchDepth = 0
        val token = ++generation
        scheduleMaintenance()
        return object : InputConnectionWrapper(target, false) {
            private fun current() = token == generation && connection === target

            override fun beginBatchEdit(): Boolean {
                if (!current()) return false
                val accepted = super.beginBatchEdit()
                if (accepted) batchDepth++
                return accepted
            }

            override fun endBatchEdit(): Boolean {
                if (!current()) return false
                val result = super.endBatchEdit()
                // Do not infer nesting from the platform return value (changed in API 33).
                if (batchDepth > 0) batchDepth--
                if (batchDepth == 0) scheduleMaintenance()
                return result
            }

            override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean =
                current() && observe(Operation.COMPOSING, text) { super.setComposingText(text, newCursorPosition) }

            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean =
                current() && observe(Operation.COMMIT, text) { super.commitText(text, newCursorPosition) }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun setComposingText(text: CharSequence, newCursorPosition: Int,
                                          textAttribute: TextAttribute?): Boolean =
                current() && observe(Operation.COMPOSING, text) {
                    super.setComposingText(text, newCursorPosition, textAttribute)
                }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun commitText(text: CharSequence, newCursorPosition: Int,
                                    textAttribute: TextAttribute?): Boolean =
                current() && observe(Operation.COMMIT, text) {
                    super.commitText(text, newCursorPosition, textAttribute)
                }

            override fun finishComposingText(): Boolean =
                current() && observe(Operation.FINISH) { super.finishComposingText() }

            override fun setSelection(start: Int, end: Int): Boolean =
                current() && observe(Operation.REGION) { super.setSelection(start, end) }

            override fun setComposingRegion(start: Int, end: Int): Boolean =
                current() && observe(Operation.REGION) { super.setComposingRegion(start, end) }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun setComposingRegion(start: Int, end: Int, textAttribute: TextAttribute?): Boolean =
                current() && observe(Operation.REGION) { super.setComposingRegion(start, end, textAttribute) }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean =
                current() && observe(Operation.DELETE) { super.deleteSurroundingText(beforeLength, afterLength) }

            override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean =
                current() && observe(Operation.DELETE) {
                    super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
                }

            override fun sendKeyEvent(event: KeyEvent): Boolean =
                current() && observe(Operation.KEY) { super.sendKeyEvent(event) }

            override fun closeConnection() {
                // Closing an old connection must not finish a new connection's composition.
                if (!current()) return
                removeCallbacks(maintenance)
                connection = null
                batchDepth = 0
                generation++
                super.closeConnection()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
        observe(if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL)
            Operation.DELETE else Operation.KEY) { super.onKeyDown(keyCode, event) }

    private inline fun observe(operation: Operation, submitted: CharSequence? = null,
                               action: () -> Boolean): Boolean {
        // A sendKeyEvent can reenter onKeyDown. Only the outer operation emits.
        if (internalEdit || operationDepth > 0) return action()
        val started = SystemClock.elapsedRealtimeNanos()
        val inputTime = captureInputTime?.invoke() // Before copying text or doing editor/UI work.
        val before = text?.toString() ?: ""
        val editable = text
        val composingStart = editable?.let { BaseInputConnection.getComposingSpanStart(it) } ?: -1
        val composingEnd = editable?.let { BaseInputConnection.getComposingSpanEnd(it) } ?: -1
        val composition = if (operation == Operation.COMMIT &&
            composingStart >= 0 && composingEnd > composingStart && composingEnd <= before.length)
            before.substring(composingStart, composingEnd) else null
        operationDepth++
        val accepted: Boolean
        try {
            accepted = action()
        } finally {
            operationDepth--
        }
        val after = text?.toString() ?: ""
        if (accepted && operation != Operation.DELETE && operation != Operation.FINISH &&
            operation != Operation.REGION && isEnabled && hasFocus()) {
            emit(FlickTextChange.detect(before, after, composition,
                submitted?.toString()), inputTime)
        }
        record(operation, after.length, started)
        lastInputUptime = SystemClock.uptimeMillis()
        scheduleMaintenance()
        return accepted
    }

    private fun emit(word: String?, inputTime: Double?) {
        if (word == null) return
        if (BuildConfig.DEBUG && word == FlickTextChange.INVALID_INPUT) invalidInputCount++
        onFlickInput?.invoke(word, inputTime)
    }

    private fun scheduleMaintenance() {
        removeCallbacks(maintenance)
        if (!isAttachedToWindow || batchDepth != 0 || operationDepth != 0 || text.isNullOrEmpty()) return
        val now = SystemClock.uptimeMillis()
        val delay = if (clearRequested) 0L else FlickInputBufferPolicy.delayBeforeClear(
            text?.length ?: 0, now - lastInputUptime, now - lastClearUptime)
        postDelayed(maintenance, delay)
    }

    /**
     * Asynchronous even when called from a game/menu callback. Subsequent input cancels an
     * idle clear. This must never modify the Editable inside an IME operation/TextWatcher.
     */
    fun clearInputWhenSafe() {
        clearRequested = true
        scheduleMaintenance()
    }

    private fun maintainBuffer() {
        if (batchDepth != 0 || operationDepth != 0 || internalEdit) return
        val length = text?.length ?: 0
        if (length == 0) { clearRequested = false; return }
        val now = SystemClock.uptimeMillis()
        if (!clearRequested && (!FlickInputBufferPolicy.shouldClear(length, now - lastInputUptime) ||
                FlickInputBufferPolicy.delayBeforeClear(length, now - lastInputUptime,
                    now - lastClearUptime) > 0)) {
            scheduleMaintenance()
            return
        }
        val target = connection
        lastClearUptime = now
        internalEdit = true
        // Group composing-span removal, deletion and cursor movement into one notification.
        val beganBatch = target?.beginBatchEdit() == true
        try {
            target?.finishComposingText()
            text?.let {
                BaseInputConnection.removeComposingSpans(it)
                it.clear()
            }
            setSelection(0)
        } finally {
            if (beganBatch) target?.endBatchEdit()
            internalEdit = false
            clearRequested = false
        }
        // Tell the IME about the new cursor/composition after the outer batch has finished.
        if (hasFocus() && isEnabled) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .updateSelection(this, 0, 0, -1, -1)
        }
        if (BuildConfig.DEBUG) rolloverCount++
    }

    private fun record(operation: Operation, length: Int, startedNanos: Long) {
        if (!BuildConfig.DEBUG) return
        durationsUs[(sampleCount % durationsUs.size).toInt()] =
            (SystemClock.elapsedRealtimeNanos() - startedNanos) / 1000
        sampleCount++
        operationCounts[operation.ordinal]++
        maxBufferLength = maxOf(maxBufferLength, length)
    }

    /** Durations start at app receipt; they do not measure IME-side gesture/IPC latency. */
    fun logInputDiagnostics() {
        if (!BuildConfig.DEBUG || sampleCount == 0L) return
        val samples = durationsUs.copyOf(minOf(sampleCount, durationsUs.size.toLong()).toInt()).sorted()
        val p95 = samples[((samples.size - 1) * 95) / 100]
        Log.d("FlickIme", "operations=$sampleCount composing=${operationCounts[Operation.COMPOSING.ordinal]}" +
            " commit=${operationCounts[Operation.COMMIT.ordinal]} maxBuffer=$maxBufferLength" +
            " clears=$rolloverCount invalid=$invalidInputCount" +
            " recentAppProcessingUs(p95/max)=$p95/${samples.last()}")
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(maintenance)
        connection = null
        batchDepth = 0
        generation++
        super.onDetachedFromWindow()
    }
}
