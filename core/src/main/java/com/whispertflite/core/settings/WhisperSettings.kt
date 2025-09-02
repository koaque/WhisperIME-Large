package com.whispertflite.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Settings management using DataStore
 */
class WhisperSettings(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "whisper_settings")
        
        // Keys for all settings
        private val OUTPUT_MODE = stringPreferencesKey("output_mode")
        private val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        private val SPEECH_ENGINE_TYPE = stringPreferencesKey("speech_engine_type")
        private val LANGUAGE = stringPreferencesKey("language")
        private val AUTO_DETECT_LANGUAGE = booleanPreferencesKey("auto_detect_language")
        private val ENABLE_VAD = booleanPreferencesKey("enable_vad")
        private val ENABLE_NOISE_SUPPRESSION = booleanPreferencesKey("enable_noise_suppression")
        private val ENABLE_PUNCTUATION = booleanPreferencesKey("enable_punctuation")
        private val ENABLE_PROFANITY_MASK = booleanPreferencesKey("enable_profanity_mask")
        private val ENDPOINTING_SENSITIVITY = floatPreferencesKey("endpointing_sensitivity")
        private val AUTO_PASTE_DIRECT_MODE = booleanPreferencesKey("auto_paste_direct_mode")
        private val INSERT_SPACE_BEFORE_PASTE = booleanPreferencesKey("insert_space_before_paste")
        private val AUTO_COPY_TO_CLIPBOARD = booleanPreferencesKey("auto_copy_to_clipboard")
        private val ADD_TIMESTAMPS_ON_COPY = booleanPreferencesKey("add_timestamps_on_copy")
        private val TEXT_PREFIX = stringPreferencesKey("text_prefix")
        private val TEXT_SUFFIX = stringPreferencesKey("text_suffix")
        private val TEXT_CASE_MODE = stringPreferencesKey("text_case_mode")
        private val ENABLE_HAPTIC_FEEDBACK = booleanPreferencesKey("enable_haptic_feedback")
        private val SHOW_VU_METER = booleanPreferencesKey("show_vu_meter")
        private val PUSH_TO_TALK_MODE = booleanPreferencesKey("push_to_talk_mode")
        private val CONTINUOUS_RECORDING_TIMEOUT = intPreferencesKey("continuous_recording_timeout")
        private val USE_FAKE_ENGINE = booleanPreferencesKey("use_fake_engine")
        private val MODEL_DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("model_download_wifi_only")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // Privacy & Data Settings
        private val TELEMETRY_ENABLED = booleanPreferencesKey("telemetry_enabled")
        private val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
        private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        private val DATA_COLLECTION_ENABLED = booleanPreferencesKey("data_collection_enabled")
        private val LOGGING_LEVEL = stringPreferencesKey("logging_level")
        private val FILE_LOGGING_ENABLED = booleanPreferencesKey("file_logging_enabled")
        private val PRIVACY_POLICY_ACCEPTED = booleanPreferencesKey("privacy_policy_accepted")
        private val TERMS_OF_USE_ACCEPTED = booleanPreferencesKey("terms_of_use_accepted")
        private val DATA_RETENTION_DAYS = intPreferencesKey("data_retention_days")
        private val ANONYMOUS_USAGE_STATS = booleanPreferencesKey("anonymous_usage_stats")
    }
    
    // Output & Routing Settings
    val outputMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[OUTPUT_MODE] ?: "DIRECT"
    }
    
    val autoPasteDirectMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_PASTE_DIRECT_MODE] ?: true
    }
    
    val insertSpaceBeforePaste: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[INSERT_SPACE_BEFORE_PASTE] ?: true
    }
    
    val autoCopyToClipboard: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_COPY_TO_CLIPBOARD] ?: false
    }
    
    val addTimestampsOnCopy: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ADD_TIMESTAMPS_ON_COPY] ?: false
    }
    
    val textPrefix: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TEXT_PREFIX] ?: ""
    }
    
    val textSuffix: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TEXT_SUFFIX] ?: ""
    }
    
    val textCaseMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TEXT_CASE_MODE] ?: "NORMAL"
    }
    
    // Model & Engine Settings
    val selectedModelId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL_ID] ?: "small"
    }
    
    val speechEngineType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SPEECH_ENGINE_TYPE] ?: "TENSORFLOW"
    }
    
    val useFakeEngine: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_FAKE_ENGINE] ?: false
    }
    
    // Language Settings
    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "en"
    }
    
    val autoDetectLanguage: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DETECT_LANGUAGE] ?: true
    }
    
    // Audio Processing Settings
    val enableVAD: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_VAD] ?: true
    }
    
    val enableNoiseSuppression: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_NOISE_SUPPRESSION] ?: false
    }
    
    val endpointingSensitivity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[ENDPOINTING_SENSITIVITY] ?: 0.5f
    }
    
    // Transcription Settings
    val enablePunctuation: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_PUNCTUATION] ?: true
    }
    
    val enableProfanityMask: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_PROFANITY_MASK] ?: false
    }
    
    // UI & UX Settings
    val enableHapticFeedback: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_HAPTIC_FEEDBACK] ?: true
    }
    
    val showVUMeter: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_VU_METER] ?: true
    }
    
    val pushToTalkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PUSH_TO_TALK_MODE] ?: false
    }
    
    val continuousRecordingTimeout: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CONTINUOUS_RECORDING_TIMEOUT] ?: 30 // seconds
    }
    
    // Download Settings
    val modelDownloadWifiOnly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MODEL_DOWNLOAD_WIFI_ONLY] ?: true
    }
    
    // Onboarding
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }
    
    // Privacy & Data Settings
    val telemetryEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TELEMETRY_ENABLED] ?: false // Default: NO telemetry
    }
    
    val crashReportingEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CRASH_REPORTING_ENABLED] ?: false // Default: NO crash reporting
    }
    
    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ANALYTICS_ENABLED] ?: false // Default: NO analytics
    }
    
    val dataCollectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DATA_COLLECTION_ENABLED] ?: false // Default: NO data collection
    }
    
    val loggingLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LOGGING_LEVEL] ?: "MINIMAL" // Default: minimal logging for battery
    }
    
    val fileLoggingEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FILE_LOGGING_ENABLED] ?: false // Default: NO file logging for battery
    }
    
    val privacyPolicyAccepted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PRIVACY_POLICY_ACCEPTED] ?: false
    }
    
    val termsOfUseAccepted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TERMS_OF_USE_ACCEPTED] ?: false
    }
    
    val dataRetentionDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DATA_RETENTION_DAYS] ?: 7 // Default: 7 days retention
    }
    
    val anonymousUsageStats: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ANONYMOUS_USAGE_STATS] ?: false // Default: NO usage stats
    }
    
    // Update methods
    suspend fun setOutputMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[OUTPUT_MODE] = mode
        }
    }
    
    suspend fun setSelectedModelId(modelId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_ID] = modelId
        }
    }
    
    suspend fun setSpeechEngineType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[SPEECH_ENGINE_TYPE] = type
        }
    }
    
    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }
    
    suspend fun setAutoDetectLanguage(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DETECT_LANGUAGE] = enabled
        }
    }
    
    suspend fun setEnableVAD(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_VAD] = enabled
        }
    }
    
    suspend fun setEnableNoiseSuppression(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_NOISE_SUPPRESSION] = enabled
        }
    }
    
    suspend fun setEnablePunctuation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_PUNCTUATION] = enabled
        }
    }
    
    suspend fun setEnableProfanityMask(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_PROFANITY_MASK] = enabled
        }
    }
    
    suspend fun setEndpointingSensitivity(sensitivity: Float) {
        context.dataStore.edit { preferences ->
            preferences[ENDPOINTING_SENSITIVITY] = sensitivity.coerceIn(0f, 1f)
        }
    }
    
    suspend fun setAutoPasteDirectMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_PASTE_DIRECT_MODE] = enabled
        }
    }
    
    suspend fun setInsertSpaceBeforePaste(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[INSERT_SPACE_BEFORE_PASTE] = enabled
        }
    }
    
    suspend fun setAutoCopyToClipboard(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_COPY_TO_CLIPBOARD] = enabled
        }
    }
    
    suspend fun setAddTimestampsOnCopy(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ADD_TIMESTAMPS_ON_COPY] = enabled
        }
    }
    
    suspend fun setTextPrefix(prefix: String) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_PREFIX] = prefix
        }
    }
    
    suspend fun setTextSuffix(suffix: String) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SUFFIX] = suffix
        }
    }
    
    suspend fun setTextCaseMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_CASE_MODE] = mode
        }
    }
    
    suspend fun setEnableHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_HAPTIC_FEEDBACK] = enabled
        }
    }
    
    suspend fun setShowVUMeter(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_VU_METER] = enabled
        }
    }
    
    suspend fun setPushToTalkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PUSH_TO_TALK_MODE] = enabled
        }
    }
    
    suspend fun setContinuousRecordingTimeout(timeoutSeconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[CONTINUOUS_RECORDING_TIMEOUT] = timeoutSeconds.coerceIn(5, 300)
        }
    }
    
    suspend fun setUseFakeEngine(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_FAKE_ENGINE] = enabled
        }
    }
    
    suspend fun setModelDownloadWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_DOWNLOAD_WIFI_ONLY] = wifiOnly
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    // Privacy & Data Update Methods
    suspend fun setTelemetryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TELEMETRY_ENABLED] = enabled
        }
    }
    
    suspend fun setCrashReportingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CRASH_REPORTING_ENABLED] = enabled
        }
    }
    
    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANALYTICS_ENABLED] = enabled
        }
    }
    
    suspend fun setDataCollectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DATA_COLLECTION_ENABLED] = enabled
        }
    }
    
    suspend fun setLoggingLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGING_LEVEL] = level
        }
    }
    
    suspend fun setFileLoggingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FILE_LOGGING_ENABLED] = enabled
        }
    }
    
    suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRIVACY_POLICY_ACCEPTED] = accepted
        }
    }
    
    suspend fun setTermsOfUseAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TERMS_OF_USE_ACCEPTED] = accepted
        }
    }
    
    suspend fun setDataRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[DATA_RETENTION_DAYS] = days.coerceIn(1, 365)
        }
    }
    
    suspend fun setAnonymousUsageStats(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANONYMOUS_USAGE_STATS] = enabled
        }
    }
    
    /**
     * Disable all data collection and telemetry for maximum privacy
     */
    suspend fun enableMaximumPrivacy() {
        context.dataStore.edit { preferences ->
            preferences[TELEMETRY_ENABLED] = false
            preferences[CRASH_REPORTING_ENABLED] = false
            preferences[ANALYTICS_ENABLED] = false
            preferences[DATA_COLLECTION_ENABLED] = false
            preferences[FILE_LOGGING_ENABLED] = false
            preferences[ANONYMOUS_USAGE_STATS] = false
            preferences[LOGGING_LEVEL] = "ERROR_ONLY"
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}