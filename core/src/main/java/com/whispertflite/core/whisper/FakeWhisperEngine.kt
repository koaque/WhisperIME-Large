package com.whispertflite.core.whisper

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

/**
 * Fake Whisper engine for testing and emulator usage
 * Simulates realistic transcription behavior without actual model processing
 */
class FakeWhisperEngine : WhisperEngine {
    
    private var _isInitialized = false
    private var _modelInfo: WhisperModelInfo? = null
    private val mockResponses = listOf(
        "Hello world",
        "This is a test transcription",
        "The quick brown fox jumps over the lazy dog",
        "Testing voice recognition",
        "How are you today",
        "This is working great",
        "Voice input is active",
        "Fake transcription result",
        "Machine learning is fascinating",
        "Speech recognition technology"
    )
    
    override var isInitialized: Boolean = false
        private set
    
    override var modelInfo: WhisperModelInfo? = null
        private set
    
    override suspend fun initialize(modelPath: String, options: WhisperOptions): Boolean {
        // Simulate initialization delay
        delay(1000)
        
        isInitialized = true
        modelInfo = WhisperModelInfo(
            modelId = "fake-model",
            isMultilingual = true,
            supportedLanguages = listOf("en", "es", "fr", "de", "zh", "ja"),
            sampleRate = 16000,
            contextSize = 448
        )
        
        return true
    }
    
    override fun transcribe(audioSamples: Flow<FloatArray>): Flow<WhisperResult> = flow {
        if (!isInitialized) {
            throw IllegalStateException("Engine not initialized")
        }
        
        var accumulatedSamples = 0
        val targetSamples = 16000 * 2 // 2 seconds of audio
        var segmentCount = 0
        
        audioSamples.collect { samples ->
            accumulatedSamples += samples.size
            
            // Emit partial results every 0.5 seconds
            if (accumulatedSamples >= 16000 * 0.5) {
                val partialText = generatePartialText(segmentCount)
                emit(WhisperResult(
                    text = partialText,
                    confidence = Random.nextFloat() * 0.3f + 0.4f, // 0.4-0.7
                    startTime = segmentCount * 2000L,
                    endTime = System.currentTimeMillis(),
                    isPartial = true,
                    isFinal = false,
                    language = "en"
                ))
            }
            
            // Emit final result every 2 seconds
            if (accumulatedSamples >= targetSamples) {
                val finalText = mockResponses[segmentCount % mockResponses.size]
                emit(WhisperResult(
                    text = finalText,
                    confidence = Random.nextFloat() * 0.2f + 0.8f, // 0.8-1.0
                    startTime = segmentCount * 2000L,
                    endTime = System.currentTimeMillis(),
                    isPartial = false,
                    isFinal = true,
                    language = "en",
                    segments = listOf(
                        WhisperSegment(
                            text = finalText,
                            startTime = segmentCount * 2000L,
                            endTime = System.currentTimeMillis(),
                            confidence = Random.nextFloat() * 0.2f + 0.8f
                        )
                    )
                ))
                
                accumulatedSamples = 0
                segmentCount++
            }
        }
    }
    
    override suspend fun transcribeBuffer(samples: FloatArray): WhisperResult {
        if (!isInitialized) {
            throw IllegalStateException("Engine not initialized")
        }
        
        // Simulate processing delay
        delay(Random.nextLong(200, 800))
        
        val text = mockResponses[Random.nextInt(mockResponses.size)]
        return WhisperResult(
            text = text,
            confidence = Random.nextFloat() * 0.2f + 0.8f,
            startTime = 0L,
            endTime = System.currentTimeMillis(),
            isPartial = false,
            isFinal = true,
            language = "en",
            segments = listOf(
                WhisperSegment(
                    text = text,
                    startTime = 0L,
                    endTime = System.currentTimeMillis(),
                    confidence = Random.nextFloat() * 0.2f + 0.8f
                )
            )
        )
    }
    
    override suspend fun release() {
        isInitialized = false
        modelInfo = null
        // Simulate cleanup delay
        delay(100)
    }
    
    private fun generatePartialText(segmentIndex: Int): String {
        val fullText = mockResponses[segmentIndex % mockResponses.size]
        val words = fullText.split(" ")
        val partialWords = words.take(Random.nextInt(1, words.size + 1))
        
        // Sometimes add incomplete word at the end
        val result = partialWords.joinToString(" ")
        return if (Random.nextBoolean() && words.size > partialWords.size) {
            result + " " + words[partialWords.size].take(Random.nextInt(1, words[partialWords.size].length))
        } else {
            result
        }
    }
}