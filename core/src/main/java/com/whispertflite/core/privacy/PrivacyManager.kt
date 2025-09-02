package com.whispertflite.core.privacy

import android.content.Context
import com.whispertflite.core.settings.WhisperSettings
import com.whispertflite.core.logging.WhisperLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * Comprehensive privacy manager for WhisperIME
 * Handles all data collection, telemetry, and user privacy controls
 */
class PrivacyManager(
    private val context: Context,
    private val settings: WhisperSettings,
    private val logger: WhisperLogger
) {
    private val privacyScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "PrivacyManager"
    }

    /**
     * Check if any data collection is enabled
     */
    suspend fun isAnyDataCollectionEnabled(): Boolean {
        return settings.telemetryEnabled.first() ||
                settings.crashReportingEnabled.first() ||
                settings.analyticsEnabled.first() ||
                settings.dataCollectionEnabled.first() ||
                settings.anonymousUsageStats.first()
    }

    /**
     * Check if user has accepted privacy policy and terms
     */
    suspend fun hasAcceptedPrivacyTerms(): Boolean {
        return settings.privacyPolicyAccepted.first() && settings.termsOfUseAccepted.first()
    }

    /**
     * Enable maximum privacy mode - disable all data collection
     */
    suspend fun enableMaximumPrivacy() {
        logger.i(TAG, "Enabling maximum privacy mode")
        
        // Disable all data collection
        settings.enableMaximumPrivacy()
        
        // Configure logger for minimal impact
        logger.disableAllLogging()
        
        // Clear any existing data
        clearAllStoredData()
        
        logger.i(TAG, "Maximum privacy mode enabled - all data collection disabled")
    }

    /**
     * Configure logging based on user preferences and battery optimization
     */
    suspend fun configureLogging() {
        val loggingLevel = settings.loggingLevel.first()
        val fileLoggingEnabled = settings.fileLoggingEnabled.first()
        
        val logLevel = when (loggingLevel) {
            "DISABLED" -> {
                logger.disableAllLogging()
                return
            }
            "ERROR_ONLY" -> WhisperLogger.LogLevel.ERROR
            "MINIMAL" -> WhisperLogger.LogLevel.WARN
            "STANDARD" -> WhisperLogger.LogLevel.INFO
            "EXTENSIVE" -> WhisperLogger.LogLevel.DEBUG
            else -> WhisperLogger.LogLevel.WARN
        }
        
        val extensiveLogging = loggingLevel == "EXTENSIVE"
        
        logger.configure(
            extensiveLogging = extensiveLogging,
            fileLogging = fileLoggingEnabled,
            minLogLevel = logLevel,
            batteryOptimized = true
        )
        
        logger.i(TAG, "Logging configured: level=$loggingLevel, fileLogging=$fileLoggingEnabled")
    }

    /**
     * Clear all stored data (logs, cache, preferences)
     */
    suspend fun clearAllStoredData() {
        try {
            logger.i(TAG, "Clearing all stored data")
            
            // Clear logs
            logger.clearLogs()
            
            // Clear cache directories
            clearCacheDirectories()
            
            // Clear temporary files
            clearTemporaryFiles()
            
            logger.i(TAG, "All stored data cleared successfully")
        } catch (e: Exception) {
            logger.e(TAG, "Error clearing stored data", e)
        }
    }

    /**
     * Get data storage summary for user transparency
     */
    suspend fun getDataStorageSummary(): DataStorageSummary {
        val logSize = getLogStorageSize()
        val cacheSize = getCacheStorageSize()
        val modelSize = getModelStorageSize()
        val totalSize = logSize + cacheSize + modelSize
        
        return DataStorageSummary(
            totalSizeMB = totalSize / (1024 * 1024),
            logSizeMB = logSize / (1024 * 1024),
            cacheSizeMB = cacheSize / (1024 * 1024),
            modelSizeMB = modelSize / (1024 * 1024),
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Check if telemetry should be collected (respects user settings)
     */
    suspend fun shouldCollectTelemetry(): Boolean {
        return settings.telemetryEnabled.first() && hasAcceptedPrivacyTerms()
    }

    /**
     * Check if crash reports should be sent (respects user settings)
     */
    suspend fun shouldSendCrashReports(): Boolean {
        return settings.crashReportingEnabled.first() && hasAcceptedPrivacyTerms()
    }

    /**
     * Check if analytics should be collected (respects user settings)
     */
    suspend fun shouldCollectAnalytics(): Boolean {
        return settings.analyticsEnabled.first() && hasAcceptedPrivacyTerms()
    }

    /**
     * Collect anonymous usage statistics (if enabled)
     */
    suspend fun recordUsageEvent(event: String, properties: Map<String, Any> = emptyMap()) {
        if (!shouldCollectAnalytics()) return
        
        // Only collect if user explicitly opted in
        if (settings.anonymousUsageStats.first()) {
            // This would integrate with your analytics provider
            logger.d(TAG, "Usage event recorded: $event")
        }
    }

    /**
     * Set data retention period and clean up old data
     */
    suspend fun setDataRetention(days: Int) {
        settings.setDataRetentionDays(days)
        cleanupOldData(days)
        logger.i(TAG, "Data retention set to $days days")
    }

    /**
     * Get privacy status summary
     */
    suspend fun getPrivacyStatus(): PrivacyStatus {
        return PrivacyStatus(
            telemetryEnabled = settings.telemetryEnabled.first(),
            crashReportingEnabled = settings.crashReportingEnabled.first(),
            analyticsEnabled = settings.analyticsEnabled.first(),
            dataCollectionEnabled = settings.dataCollectionEnabled.first(),
            fileLoggingEnabled = settings.fileLoggingEnabled.first(),
            loggingLevel = settings.loggingLevel.first(),
            hasAcceptedTerms = hasAcceptedPrivacyTerms(),
            dataRetentionDays = settings.dataRetentionDays.first()
        )
    }

    private fun clearCacheDirectories() {
        try {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdirs()
        } catch (e: Exception) {
            logger.e(TAG, "Error clearing cache directories", e)
        }
    }

    private fun clearTemporaryFiles() {
        try {
            val tempDir = File(context.filesDir, "temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error clearing temporary files", e)
        }
    }

    private fun getLogStorageSize(): Long {
        return try {
            val logsDir = File(context.filesDir, "logs")
            if (logsDir.exists()) {
                logsDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            } else {
                0L
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error calculating log storage size", e)
            0L
        }
    }

    private fun getCacheStorageSize(): Long {
        return try {
            context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } catch (e: Exception) {
            logger.e(TAG, "Error calculating cache storage size", e)
            0L
        }
    }

    private fun getModelStorageSize(): Long {
        return try {
            val modelsDir = File(context.filesDir, "models")
            if (modelsDir.exists()) {
                modelsDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            } else {
                0L
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error calculating model storage size", e)
            0L
        }
    }

    private suspend fun cleanupOldData(retentionDays: Int) {
        val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        
        try {
            // Clean up old log files
            val logsDir = File(context.filesDir, "logs")
            if (logsDir.exists()) {
                logsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        file.delete()
                        logger.d(TAG, "Deleted old log file: ${file.name}")
                    }
                }
            }
            
            // Clean up old cache files
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    file.deleteRecursively()
                    logger.d(TAG, "Deleted old cache file: ${file.name}")
                }
            }
            
        } catch (e: Exception) {
            logger.e(TAG, "Error cleaning up old data", e)
        }
    }

    /**
     * Initialize privacy manager - configure based on user settings
     */
    suspend fun initialize() {
        logger.i(TAG, "Initializing privacy manager")
        
        // Configure logging based on user preferences
        configureLogging()
        
        // Set up data retention cleanup
        val retentionDays = settings.dataRetentionDays.first()
        if (retentionDays > 0) {
            cleanupOldData(retentionDays)
        }
        
        // Log privacy status (without revealing sensitive info)
        val isAnyCollectionEnabled = isAnyDataCollectionEnabled()
        logger.i(TAG, "Privacy manager initialized - data collection: $isAnyCollectionEnabled")
    }
}

data class DataStorageSummary(
    val totalSizeMB: Long,
    val logSizeMB: Long,
    val cacheSizeMB: Long,
    val modelSizeMB: Long,
    val lastUpdated: Long
)

data class PrivacyStatus(
    val telemetryEnabled: Boolean,
    val crashReportingEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val dataCollectionEnabled: Boolean,
    val fileLoggingEnabled: Boolean,
    val loggingLevel: String,
    val hasAcceptedTerms: Boolean,
    val dataRetentionDays: Int
)