package com.whispertflite.core.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import com.github.gkonovalov.vad.VoiceActivityDetector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * Audio recording and processing engine with VAD support
 */
class AudioEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioEngine"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }

    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var vad: VoiceActivityDetector? = null
    private var isRecording = false
    private var recordingJob: Job? = null

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _isVoiceDetected = MutableStateFlow(false)
    val isVoiceDetected: StateFlow<Boolean> = _isVoiceDetected.asStateFlow()

    private val _audioSamples = MutableSharedFlow<FloatArray>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val audioSamples: SharedFlow<FloatArray> = _audioSamples.asSharedFlow()

    @SuppressLint("MissingPermission")
    fun initialize(enableVAD: Boolean = true, enableNoiseSuppression: Boolean = false) {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (enableVAD) {
                vad = VoiceActivityDetector(SAMPLE_RATE)
            }

            if (enableNoiseSuppression && NoiseSuppressor.isAvailable()) {
                audioRecord?.let { record ->
                    noiseSuppressor = NoiseSuppressor.create(record.audioSessionId)
                    noiseSuppressor?.enabled = true
                }
            }

            Log.d(TAG, "AudioEngine initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioEngine", e)
            throw e
        }
    }

    fun startRecording() {
        if (isRecording) return
        
        val record = audioRecord ?: throw IllegalStateException("AudioEngine not initialized")
        
        try {
            record.startRecording()
            isRecording = true
            
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                recordingLoop(record)
            }
            
            Log.d(TAG, "Recording started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            throw e
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        
        try {
            audioRecord?.stop()
            _audioLevel.value = 0f
            _isVoiceDetected.value = false
            Log.d(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    fun release() {
        stopRecording()
        
        try {
            noiseSuppressor?.release()
            noiseSuppressor = null
            
            audioRecord?.release()
            audioRecord = null
            
            vad?.let { detector ->
                // VAD cleanup if needed
            }
            vad = null
            
            Log.d(TAG, "AudioEngine released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioEngine", e)
        }
    }

    private suspend fun recordingLoop(record: AudioRecord) {
        val bufferSize = record.bufferSizeInFrames
        val buffer = ShortArray(bufferSize)
        val floatBuffer = FloatArray(bufferSize)

        while (isRecording && !currentCoroutineContext().isActive.not()) {
            try {
                val readSamples = record.read(buffer, 0, buffer.size)
                
                if (readSamples > 0) {
                    // Convert to float samples (-1.0 to 1.0)
                    for (i in 0 until readSamples) {
                        floatBuffer[i] = buffer[i].toFloat() / Short.MAX_VALUE
                    }
                    
                    // Calculate audio level (RMS)
                    val level = calculateAudioLevel(floatBuffer, readSamples)
                    _audioLevel.value = level
                    
                    // Voice Activity Detection
                    vad?.let { detector ->
                        val isVoice = detector.isSpeech(buffer.take(readSamples).toShortArray())
                        _isVoiceDetected.value = isVoice
                    }
                    
                    // Emit audio samples for processing
                    val samples = floatBuffer.take(readSamples).toFloatArray()
                    _audioSamples.tryEmit(samples)
                }
                
                // Small delay to prevent excessive CPU usage
                delay(10)
            } catch (e: Exception) {
                Log.e(TAG, "Error in recording loop", e)
                break
            }
        }
    }

    private fun calculateAudioLevel(samples: FloatArray, count: Int): Float {
        if (count <= 0) return 0f
        
        var sum = 0.0
        for (i in 0 until count) {
            sum += samples[i] * samples[i]
        }
        
        return sqrt(sum / count).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Get current recording parameters
     */
    fun getRecordingInfo(): AudioRecordingInfo {
        return AudioRecordingInfo(
            sampleRate = SAMPLE_RATE,
            channels = 1,
            bitDepth = 16,
            bufferSize = audioRecord?.bufferSizeInFrames ?: 0
        )
    }
}

data class AudioRecordingInfo(
    val sampleRate: Int,
    val channels: Int,
    val bitDepth: Int,
    val bufferSize: Int
)