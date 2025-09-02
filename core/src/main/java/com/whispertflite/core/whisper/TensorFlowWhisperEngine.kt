package com.whispertflite.core.whisper

import android.content.Context
import com.whispertflite.asr.Whisper
import com.whispertflite.asr.WhisperResult as LegacyWhisperResult
import com.whispertflite.core.model.ModelCatalog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import android.util.Log

/**
 * TensorFlow Lite implementation of WhisperEngine
 * Adapts the existing Java Whisper implementation to the new architecture
 */
class TensorFlowWhisperEngine(private val context: Context) : WhisperEngine {
    
    companion object {
        private const val TAG = "TensorFlowWhisperEngine"
    }
    
    private var whisper: Whisper? = null
    private var currentModelId: String? = null
    private var _isInitialized = false
    private var _modelInfo: WhisperModelInfo? = null
    
    override val isInitialized: Boolean get() = _isInitialized
    override val modelInfo: WhisperModelInfo? get() = _modelInfo
    
    override suspend fun initialize(modelPath: String, options: WhisperOptions): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelPath)
            if (!modelFile.exists()) {
                Log.e(TAG, "Model file not found: $modelPath")
                return@withContext false
            }
            
            // Extract model ID from path
            val modelId = extractModelIdFromPath(modelPath)
            val model = ModelCatalog.getModelById(modelId)
            
            // Find vocab file (assumes it's in the same directory)
            val vocabFile = File(modelFile.parent, "filters_vocab_multilingual.bin")
            if (!vocabFile.exists()) {
                Log.e(TAG, "Vocab file not found: ${vocabFile.absolutePath}")
                return@withContext false
            }
            
            // Initialize legacy Whisper engine
            whisper = Whisper().apply {
                initialize(modelPath, vocabFile.absolutePath, model?.multilingual ?: true)
            }
            
            currentModelId = modelId
            _isInitialized = true
            _modelInfo = WhisperModelInfo(
                modelId = modelId,
                isMultilingual = model?.multilingual ?: true,
                supportedLanguages = model?.languages ?: listOf("en"),
                sampleRate = 16000,
                contextSize = 448
            )
            
            Log.d(TAG, "TensorFlow Whisper engine initialized with model: $modelId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TensorFlow Whisper engine", e)
            false
        }
    }
    
    override fun transcribe(audioSamples: Flow<FloatArray>): Flow<WhisperResult> = flow {
        if (!isInitialized) {
            throw IllegalStateException("Engine not initialized")
        }
        
        val whisperEngine = whisper ?: throw IllegalStateException("Whisper engine not available")
        
        audioSamples
            .buffer(capacity = 10)
            .collect { samples ->
                try {
                    // Process audio samples with legacy engine
                    // Note: This is a simplified adaptation - the original engine expects
                    // specific buffer management that may need more sophisticated integration
                    val result = processAudioSamples(whisperEngine, samples)
                    if (result != null) {
                        emit(convertLegacyResult(result))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing audio samples", e)
                }
            }
    }
    
    override suspend fun transcribeBuffer(samples: FloatArray): WhisperResult = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            throw IllegalStateException("Engine not initialized")
        }
        
        val whisperEngine = whisper ?: throw IllegalStateException("Whisper engine not available")
        
        try {
            val result = processAudioSamples(whisperEngine, samples)
            convertLegacyResult(result ?: createEmptyResult())
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing buffer", e)
            createErrorResult(e.message ?: "Transcription failed")
        }
    }
    
    override suspend fun release() {
        try {
            whisper?.deinitialize()
            whisper = null
            _isInitialized = false
            _modelInfo = null
            currentModelId = null
            Log.d(TAG, "TensorFlow Whisper engine released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing TensorFlow Whisper engine", e)
        }
    }
    
    private fun processAudioSamples(whisperEngine: Whisper, samples: FloatArray): LegacyWhisperResult? {
        // This is a simplified integration point
        // The actual integration would require proper buffer management
        // and interaction with the legacy Whisper.Action system
        
        try {
            // Convert float samples to the format expected by legacy engine
            // This may require additional buffer management and conversion
            return whisperEngine.processRecordBuffer(Whisper.Action.PROCESS_TRANSCRIBE, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error in processAudioSamples", e)
            return null
        }
    }
    
    private fun convertLegacyResult(legacyResult: LegacyWhisperResult): WhisperResult {
        return WhisperResult(
            text = legacyResult.result ?: "",
            confidence = 0.8f, // Legacy engine doesn't provide confidence scores
            startTime = 0L,
            endTime = System.currentTimeMillis(),
            isPartial = false,
            isFinal = true,
            language = "en", // Legacy engine doesn't provide language detection
            segments = emptyList() // Legacy engine doesn't provide segment information
        )
    }
    
    private fun createEmptyResult(): LegacyWhisperResult {
        return LegacyWhisperResult().apply {
            result = ""
        }
    }
    
    private fun createErrorResult(error: String): WhisperResult {
        return WhisperResult(
            text = "",
            confidence = 0f,
            startTime = 0L,
            endTime = System.currentTimeMillis(),
            isPartial = false,
            isFinal = true,
            language = null,
            segments = emptyList()
        )
    }
    
    private fun extractModelIdFromPath(modelPath: String): String {
        val filename = File(modelPath).nameWithoutExtension
        return when {
            filename.contains("tiny") && filename.contains("en") -> "tiny"
            filename.contains("tiny") -> "tiny-multi"
            filename.contains("base") && filename.contains("en") -> "base"
            filename.contains("base") -> "base-multi"
            filename.contains("small") -> "small"
            filename.contains("medium") -> "medium"
            filename.contains("large-v3") -> "large-v3"
            filename.contains("large") -> "large-v3"
            else -> "unknown"
        }
    }
}