package com.whispertflite.core.whisper

import kotlinx.coroutines.flow.Flow

/**
 * Interface for Whisper speech recognition engines
 */
interface WhisperEngine {
    
    /**
     * Initialize the engine with a model
     */
    suspend fun initialize(modelPath: String, options: WhisperOptions = WhisperOptions()): Boolean
    
    /**
     * Process audio samples and return transcription results
     */
    fun transcribe(audioSamples: Flow<FloatArray>): Flow<WhisperResult>
    
    /**
     * Process a single audio buffer (for batch processing)
     */
    suspend fun transcribeBuffer(samples: FloatArray): WhisperResult
    
    /**
     * Check if the engine is initialized and ready
     */
    val isInitialized: Boolean
    
    /**
     * Get information about the loaded model
     */
    val modelInfo: WhisperModelInfo?
    
    /**
     * Release resources
     */
    suspend fun release()
}

/**
 * Configuration options for Whisper processing
 */
data class WhisperOptions(
    val language: String? = null, // null for auto-detect
    val enableTranslation: Boolean = false,
    val enableTimestamps: Boolean = false,
    val enableWordTimestamps: Boolean = false,
    val temperature: Float = 0.0f,
    val maxTokens: Int = 224,
    val suppressNonSpeechTokens: Boolean = true,
    val suppressBlank: Boolean = true,
    val initialPrompt: String? = null,
    val beamSize: Int = 1,
    val patience: Float = 1.0f
)

/**
 * Result from Whisper transcription
 */
data class WhisperResult(
    val text: String,
    val confidence: Float = 0f,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val isPartial: Boolean = false,
    val isFinal: Boolean = false,
    val language: String? = null,
    val segments: List<WhisperSegment> = emptyList()
)

/**
 * Individual segment within a transcription result
 */
data class WhisperSegment(
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val confidence: Float,
    val words: List<WhisperWord> = emptyList()
)

/**
 * Individual word with timing information
 */
data class WhisperWord(
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val confidence: Float
)

/**
 * Information about a loaded Whisper model
 */
data class WhisperModelInfo(
    val modelId: String,
    val isMultilingual: Boolean,
    val supportedLanguages: List<String>,
    val sampleRate: Int,
    val contextSize: Int
)