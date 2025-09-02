package com.whispertflite.core.logging

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Comprehensive logging system for WhisperIME
 * Supports multiple log levels, file output, and user-controlled extensive logging
 */
class WhisperLogger private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: WhisperLogger? = null
        
        fun getInstance(context: Context): WhisperLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WhisperLogger(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val MAX_LOG_FILES = 5
        private const val MAX_LOG_SIZE_MB = 10
        private const val LOG_FILE_PREFIX = "whisper_log_"
        private const val LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }
    
    enum class LogLevel(val priority: Int) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR)
    }
    
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null,
        val threadName: String = Thread.currentThread().name
    )
    
    private val logScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val dateFormatter = SimpleDateFormat(LOG_DATE_FORMAT, Locale.US)
    
    private var isExtensiveLoggingEnabled = false
    private var fileLoggingEnabled = true
    private var currentLogLevel = LogLevel.INFO
    
    private val _logEntries = MutableSharedFlow<LogEntry>(
        replay = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val logEntries: SharedFlow<LogEntry> = _logEntries.asSharedFlow()
    
    init {
        startLogWriter()
    }
    
    /**
     * Configure logging settings with battery optimization
     */
    fun configure(
        extensiveLogging: Boolean,
        fileLogging: Boolean,
        minLogLevel: LogLevel,
        batteryOptimized: Boolean = true
    ) {
        isExtensiveLoggingEnabled = extensiveLogging
        fileLoggingEnabled = fileLogging
        currentLogLevel = minLogLevel
        
        if (extensiveLogging) {
            i("WhisperLogger", "Extensive logging enabled - performance may be impacted")
            if (batteryOptimized) {
                i("WhisperLogger", "Battery optimization enabled - reducing log frequency")
            }
        } else {
            i("WhisperLogger", "Logging optimized for battery life")
        }
    }
    
    /**
     * Disable all logging except critical errors (maximum battery savings)
     */
    fun disableAllLogging() {
        isExtensiveLoggingEnabled = false
        fileLoggingEnabled = false
        currentLogLevel = LogLevel.ERROR
        i("WhisperLogger", "All logging disabled for maximum battery optimization")
    }
    
    /**
     * Enable minimal logging (errors and warnings only)
     */
    fun enableMinimalLogging() {
        isExtensiveLoggingEnabled = false
        fileLoggingEnabled = true
        currentLogLevel = LogLevel.WARN
        i("WhisperLogger", "Minimal logging enabled")
    }
    
    /**
     * Check if logging is completely disabled
     */
    fun isLoggingEnabled(): Boolean {
        return currentLogLevel.priority <= LogLevel.ERROR.priority || isExtensiveLoggingEnabled
    }
    
    /**
     * Verbose logging - only shown in extensive mode
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (isExtensiveLoggingEnabled) {
            log(LogLevel.VERBOSE, tag, message, throwable)
        }
    }
    
    /**
     * Debug logging - shown in extensive mode
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (isExtensiveLoggingEnabled || currentLogLevel.priority <= LogLevel.DEBUG.priority) {
            log(LogLevel.DEBUG, tag, message, throwable)
        }
    }
    
    /**
     * Info logging - always shown
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLogLevel.priority <= LogLevel.INFO.priority) {
            log(LogLevel.INFO, tag, message, throwable)
        }
    }
    
    /**
     * Warning logging - always shown
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLogLevel.priority <= LogLevel.WARN.priority) {
            log(LogLevel.WARN, tag, message, throwable)
        }
    }
    
    /**
     * Error logging - always shown
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLogLevel.priority <= LogLevel.ERROR.priority) {
            log(LogLevel.ERROR, tag, message, throwable)
        }
    }
    
    /**
     * Performance measurement logging
     */
    inline fun <T> measureTime(tag: String, operation: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            block().also {
                val duration = System.currentTimeMillis() - startTime
                if (isExtensiveLoggingEnabled) {
                    d(tag, "$operation completed in ${duration}ms")
                }
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            e(tag, "$operation failed after ${duration}ms", e)
            throw e
        }
    }
    
    /**
     * Audio processing specific logging
     */
    fun logAudioEvent(event: String, details: Map<String, Any> = emptyMap()) {
        if (isExtensiveLoggingEnabled) {
            val detailsStr = details.entries.joinToString(", ") { "${it.key}=${it.value}" }
            d("AudioEngine", "$event${if (detailsStr.isNotEmpty()) " - $detailsStr" else ""}")
        }
    }
    
    /**
     * Model management logging
     */
    fun logModelEvent(modelId: String, event: String, details: Map<String, Any> = emptyMap()) {
        val detailsStr = details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        i("ModelRepository", "Model $modelId: $event${if (detailsStr.isNotEmpty()) " - $detailsStr" else ""}")
    }
    
    /**
     * Transcription logging
     */
    fun logTranscription(text: String, confidence: Float, engine: String, duration: Long) {
        if (isExtensiveLoggingEnabled) {
            d("Transcription", "[$engine] '${text.take(50)}${if (text.length > 50) "..." else ""}' (conf: $confidence, ${duration}ms)")
        } else {
            i("Transcription", "Transcribed ${text.length} chars via $engine in ${duration}ms")
        }
    }
    
    /**
     * Get all log files
     */
    fun getLogFiles(): List<File> {
        val logsDir = File(context.filesDir, "logs")
        return logsDir.listFiles { file ->
            file.name.startsWith(LOG_FILE_PREFIX) && file.name.endsWith(".txt")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Export logs as a single string
     */
    suspend fun exportLogs(): String = withContext(Dispatchers.IO) {
        val logFiles = getLogFiles()
        val StringBuilder = StringBuilder()
        
        StringBuilder.append("=== WhisperIME Log Export ===\n")
        StringBuilder.append("Generated: ${dateFormatter.format(Date())}\n")
        StringBuilder.append("Device: ${android.os.Build.MODEL} (API ${android.os.Build.VERSION.SDK_INT})\n")
        StringBuilder.append("App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}\n")
        StringBuilder.append("\n")
        
        logFiles.take(3).forEach { file ->
            StringBuilder.append("=== ${file.name} ===\n")
            try {
                StringBuilder.append(file.readText())
                StringBuilder.append("\n\n")
            } catch (e: Exception) {
                StringBuilder.append("Error reading file: ${e.message}\n\n")
            }
        }
        
        StringBuilder.toString()
    }
    
    /**
     * Clear all log files
     */
    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        val logsDir = File(context.filesDir, "logs")
        logsDir.listFiles()?.forEach { file ->
            try {
                file.delete()
            } catch (e: Exception) {
                Log.e("WhisperLogger", "Failed to delete log file: ${file.name}", e)
            }
        }
        i("WhisperLogger", "Log files cleared")
    }
    
    /**
     * Get current log file size in MB
     */
    fun getCurrentLogSizeMB(): Float {
        val totalSize = getLogFiles().sumOf { it.length() }
        return totalSize / (1024f * 1024f)
    }
    
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )
        
        // Always log to Android's Log system
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
        
        // Add to internal log stream
        _logEntries.tryEmit(entry)
        
        // Queue for file writing if enabled
        if (fileLoggingEnabled) {
            logQueue.offer(entry)
        }
    }
    
    private fun startLogWriter() {
        logScope.launch {
            val logsDir = File(context.filesDir, "logs")
            logsDir.mkdirs()
            
            while (true) {
                try {
                    val entries = mutableListOf<LogEntry>()
                    
                    // Collect entries from queue
                    while (entries.size < 50) { // Batch write for efficiency
                        val entry = logQueue.poll() ?: break
                        entries.add(entry)
                    }
                    
                    if (entries.isNotEmpty()) {
                        writeLogEntries(logsDir, entries)
                        cleanupOldLogs(logsDir)
                    }
                    
                    delay(1000) // Write every second
                } catch (e: Exception) {
                    Log.e("WhisperLogger", "Error in log writer", e)
                    delay(5000) // Wait longer on error
                }
            }
        }
    }
    
    private suspend fun writeLogEntries(logsDir: File, entries: List<LogEntry>) = withContext(Dispatchers.IO) {
        val currentLogFile = getCurrentLogFile(logsDir)
        
        try {
            PrintWriter(currentLogFile.writer(true)).use { writer ->
                entries.forEach { entry ->
                    val timestamp = dateFormatter.format(Date(entry.timestamp))
                    writer.println("$timestamp [${entry.level.name}] ${entry.tag}: ${entry.message}")
                    
                    entry.throwable?.let { throwable ->
                        writer.println("  Exception: ${throwable.javaClass.simpleName}: ${throwable.message}")
                        throwable.stackTrace.take(10).forEach { stack ->
                            writer.println("    at $stack")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WhisperLogger", "Failed to write log entries", e)
        }
    }
    
    private fun getCurrentLogFile(logsDir: File): File {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val logFile = File(logsDir, "${LOG_FILE_PREFIX}$today.txt")
        
        // Check if current log file is too large
        if (logFile.exists() && logFile.length() > MAX_LOG_SIZE_MB * 1024 * 1024) {
            val timestamp = SimpleDateFormat("HHmmss", Locale.US).format(Date())
            return File(logsDir, "${LOG_FILE_PREFIX}${today}_$timestamp.txt")
        }
        
        return logFile
    }
    
    private fun cleanupOldLogs(logsDir: File) {
        val logFiles = getLogFiles()
        
        // Remove excess files
        if (logFiles.size > MAX_LOG_FILES) {
            logFiles.drop(MAX_LOG_FILES).forEach { file ->
                try {
                    file.delete()
                } catch (e: Exception) {
                    Log.e("WhisperLogger", "Failed to delete old log file: ${file.name}", e)
                }
            }
        }
    }
}