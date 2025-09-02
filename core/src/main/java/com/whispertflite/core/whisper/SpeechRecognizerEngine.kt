package com.whispertflite.core.whisper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Android SpeechRecognizer fallback engine
 * Uses the system's built-in speech recognition service
 */
class SpeechRecognizerEngine(private val context: Context) : WhisperEngine {
    
    companion object {
        private const val TAG = "SpeechRecognizerEngine"
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var _isInitialized = false
    private var _modelInfo: WhisperModelInfo? = null
    
    override val isInitialized: Boolean get() = _isInitialized
    override val modelInfo: WhisperModelInfo? get() = _modelInfo
    
    override suspend fun initialize(modelPath: String, options: WhisperOptions): Boolean {
        return try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.e(TAG, "Speech recognition not available")
                return false
            }
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            _isInitialized = true
            _modelInfo = WhisperModelInfo(
                modelId = "android-speechrecognizer",
                isMultilingual = true, // System recognizer typically supports multiple languages
                supportedLanguages = listOf("en", "es", "fr", "de", "zh", "ja", "ko", "it", "pt", "ru"),
                sampleRate = 16000,
                contextSize = 0
            )
            
            Log.d(TAG, "SpeechRecognizer engine initialized")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SpeechRecognizer engine", e)
            false
        }
    }
    
    override fun transcribe(audioSamples: Flow<FloatArray>): Flow<WhisperResult> = callbackFlow {
        if (!isInitialized) {
            throw IllegalStateException("Engine not initialized")
        }
        
        val recognizer = speechRecognizer ?: throw IllegalStateException("SpeechRecognizer not available")
        
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Could emit audio level updates here if needed
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Not typically used
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "End of speech")
            }
            
            override fun onError(error: Int) {
                val errorMessage = getErrorMessage(error)
                Log.e(TAG, "Recognition error: $errorMessage")
                
                trySend(WhisperResult(
                    text = "",
                    confidence = 0f,
                    startTime = 0L,
                    endTime = System.currentTimeMillis(),
                    isPartial = false,
                    isFinal = true,
                    language = null
                ))
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    val conf = confidence?.get(0) ?: 0.8f
                    
                    trySend(WhisperResult(
                        text = text,
                        confidence = conf,
                        startTime = 0L,
                        endTime = System.currentTimeMillis(),
                        isPartial = false,
                        isFinal = true,
                        language = "en" // System recognizer doesn't provide language info
                    ))
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    
                    trySend(WhisperResult(
                        text = text,
                        confidence = 0.5f, // Lower confidence for partial results
                        startTime = 0L,
                        endTime = System.currentTimeMillis(),
                        isPartial = true,
                        isFinal = false,
                        language = "en"
                    ))
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle any special events if needed
            }
        }
        
        recognizer.setRecognitionListener(listener)
        
        // Start recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        recognizer.startListening(intent)
        
        // We don't actually use the audioSamples flow since SpeechRecognizer
        // handles its own audio capture, but we need to consume it to avoid backpressure
        launch {
            audioSamples.collect { /* consume but ignore */ }
        }
        
        awaitClose {
            recognizer.stopListening()
        }
    }
    
    override suspend fun transcribeBuffer(samples: FloatArray): WhisperResult {
        // SpeechRecognizer doesn't support processing pre-recorded buffers directly
        // This would require streaming the audio or using a different approach
        return WhisperResult(
            text = "",
            confidence = 0f,
            startTime = 0L,
            endTime = System.currentTimeMillis(),
            isPartial = false,
            isFinal = true,
            language = null
        )
    }
    
    override suspend fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isInitialized = false
            _modelInfo = null
            Log.d(TAG, "SpeechRecognizer engine released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SpeechRecognizer engine", e)
        }
    }
    
    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
}