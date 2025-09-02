package com.whispertflite.core.output

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputConnection
import kotlinx.coroutines.flow.*

/**
 * Routes transcription output to different destinations
 */
class OutputRouter(private val context: Context) {
    
    companion object {
        private const val TAG = "OutputRouter"
    }
    
    private var currentSink: OutputSink? = null
    private var outputMode = OutputMode.DIRECT
    
    private val _transcriptBuffer = MutableStateFlow<List<TranscriptEntry>>(emptyList())
    val transcriptBuffer: StateFlow<List<TranscriptEntry>> = _transcriptBuffer.asStateFlow()
    
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    
    /**
     * Set the output mode
     */
    fun setOutputMode(mode: OutputMode) {
        outputMode = mode
        Log.d(TAG, "Output mode set to: $mode")
    }
    
    /**
     * Set the current input connection for direct mode
     */
    fun setInputConnection(inputConnection: InputConnection?) {
        currentSink = if (inputConnection != null) {
            InputConnectionSink(inputConnection)
        } else {
            null
        }
        Log.d(TAG, "Input connection ${if (inputConnection != null) "set" else "cleared"}")
    }
    
    /**
     * Process transcription result according to current mode
     */
    fun processTranscription(result: TranscriptionResult) {
        when (outputMode) {
            OutputMode.DIRECT -> processDirect(result)
            OutputMode.BUFFERED -> processBuffered(result)
        }
    }
    
    /**
     * Paste buffered content to the current input connection
     * Returns true if successful, false otherwise
     */
    fun pasteBufferedContent(): Boolean {
        return when (outputMode) {
            OutputMode.DIRECT -> {
                Log.w(TAG, "Paste called in direct mode - ignoring")
                false
            }
            OutputMode.BUFFERED -> {
                val content = getBufferedText()
                if (content.isNotEmpty()) {
                    val success = pasteToTarget(content)
                    if (success) {
                        clearBuffer()
                    }
                    success
                } else {
                    false
                }
            }
        }
    }
    
    /**
     * Clear the transcript buffer
     */
    fun clearBuffer() {
        _transcriptBuffer.value = emptyList()
        Log.d(TAG, "Transcript buffer cleared")
    }
    
    /**
     * Get combined text from buffer
     */
    fun getBufferedText(): String {
        return _transcriptBuffer.value.joinToString(" ") { it.text }
    }
    
    /**
     * Add text to buffer manually (for OCR results, etc.)
     */
    fun addToBuffer(text: String, source: TranscriptSource = TranscriptSource.MANUAL) {
        val entry = TranscriptEntry(
            text = text,
            timestamp = System.currentTimeMillis(),
            source = source,
            confidence = 1.0f
        )
        
        _transcriptBuffer.value = _transcriptBuffer.value + entry
        Log.d(TAG, "Added to buffer: $text")
    }
    
    private fun processDirect(result: TranscriptionResult) {
        val sink = currentSink
        if (sink != null) {
            if (result.isPartial) {
                sink.replacePartialText(result.text)
            } else {
                sink.commitText(result.text)
            }
        } else {
            // No input connection - fall back to clipboard
            copyToClipboard(result.text)
            Log.w(TAG, "No input connection available - copied to clipboard")
        }
    }
    
    private fun processBuffered(result: TranscriptionResult) {
        if (result.isFinal) {
            val entry = TranscriptEntry(
                text = result.text,
                timestamp = System.currentTimeMillis(),
                source = TranscriptSource.VOICE,
                confidence = result.confidence
            )
            
            _transcriptBuffer.value = _transcriptBuffer.value + entry
        }
        // Partial results are not added to buffer in buffered mode
    }
    
    private fun pasteToTarget(text: String): Boolean {
        val sink = currentSink
        return if (sink != null) {
            sink.commitText(text)
            true
        } else {
            copyToClipboard(text)
            false // Indicates fallback to clipboard
        }
    }
    
    private fun copyToClipboard(text: String) {
        try {
            val clip = ClipData.newPlainText("Whisper Transcription", text)
            clipboardManager.setPrimaryClip(clip)
            Log.d(TAG, "Copied to clipboard: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy to clipboard", e)
        }
    }
}

/**
 * Output routing modes
 */
enum class OutputMode {
    DIRECT,    // Stream directly to InputConnection
    BUFFERED   // Accumulate in buffer, paste on demand
}

/**
 * Source of transcription content
 */
enum class TranscriptSource {
    VOICE,     // From speech recognition
    OCR,       // From image text recognition
    MANUAL     // Manually added
}

/**
 * Entry in the transcript buffer
 */
data class TranscriptEntry(
    val text: String,
    val timestamp: Long,
    val source: TranscriptSource,
    val confidence: Float
)

/**
 * Result from transcription processing
 */
data class TranscriptionResult(
    val text: String,
    val isPartial: Boolean,
    val isFinal: Boolean,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Interface for output sinks
 */
interface OutputSink {
    fun commitText(text: String): Boolean
    fun replacePartialText(text: String): Boolean
}

/**
 * InputConnection-based output sink for direct text input
 */
class InputConnectionSink(private val inputConnection: InputConnection) : OutputSink {
    
    companion object {
        private const val TAG = "InputConnectionSink"
    }
    
    private var lastPartialLength = 0
    
    override fun commitText(text: String): Boolean {
        return try {
            // Clear any previous partial text
            if (lastPartialLength > 0) {
                inputConnection.deleteSurroundingText(lastPartialLength, 0)
                lastPartialLength = 0
            }
            
            inputConnection.commitText(text, 1)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to commit text", e)
            false
        }
    }
    
    override fun replacePartialText(text: String): Boolean {
        return try {
            // Delete previous partial text
            if (lastPartialLength > 0) {
                inputConnection.deleteSurroundingText(lastPartialLength, 0)
            }
            
            // Insert new partial text
            inputConnection.commitText(text, 1)
            lastPartialLength = text.length
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to replace partial text", e)
            false
        }
    }
}

/**
 * Buffer-based output sink for accumulating text
 */
class BufferSink(private val onTextAdded: (String) -> Unit) : OutputSink {
    
    override fun commitText(text: String): Boolean {
        onTextAdded(text)
        return true
    }
    
    override fun replacePartialText(text: String): Boolean {
        // Buffer sink doesn't handle partial text - only final results
        return true
    }
}