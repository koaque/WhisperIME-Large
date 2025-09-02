package com.whispertflite.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whispertflite.core.settings.WhisperSettings
import com.whispertflite.core.logging.WhisperLogger
import com.whispertflite.core.privacy.PrivacyManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PrivacyConsentUiState(
    val privacyPolicyAccepted: Boolean = false,
    val termsAccepted: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val loggingLevel: String = "MINIMAL",
    val showPrivacyPolicy: Boolean = false,
    val showTermsOfUse: Boolean = false
)

class PrivacyConsentViewModel(
    private val settings: WhisperSettings,
    private val logger: WhisperLogger,
    private val privacyManager: PrivacyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyConsentUiState())
    val uiState: StateFlow<PrivacyConsentUiState> = _uiState.asStateFlow()

    fun setPrivacyPolicyAccepted(accepted: Boolean) {
        _uiState.value = _uiState.value.copy(privacyPolicyAccepted = accepted)
    }

    fun setTermsAccepted(accepted: Boolean) {
        _uiState.value = _uiState.value.copy(termsAccepted = accepted)
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(analyticsEnabled = enabled)
    }

    fun setCrashReportingEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(crashReportingEnabled = enabled)
    }

    fun setLoggingLevel(level: String) {
        _uiState.value = _uiState.value.copy(loggingLevel = level)
    }

    fun showPrivacyPolicy() {
        _uiState.value = _uiState.value.copy(showPrivacyPolicy = true)
    }

    fun showTermsOfUse() {
        _uiState.value = _uiState.value.copy(showTermsOfUse = true)
    }

    fun hidePrivacyPolicy() {
        _uiState.value = _uiState.value.copy(showPrivacyPolicy = false)
    }

    fun hideTermsOfUse() {
        _uiState.value = _uiState.value.copy(showTermsOfUse = false)
    }

    fun enableMaximumPrivacy() {
        _uiState.value = _uiState.value.copy(
            analyticsEnabled = false,
            crashReportingEnabled = false,
            loggingLevel = "DISABLED"
        )
        logger.i("PrivacyConsentViewModel", "Maximum privacy settings selected")
    }

    fun enableRecommendedSettings() {
        _uiState.value = _uiState.value.copy(
            analyticsEnabled = false, // Still default to no analytics
            crashReportingEnabled = false, // Still default to no crash reporting
            loggingLevel = "MINIMAL" // Minimal for good battery life
        )
        logger.i("PrivacyConsentViewModel", "Recommended privacy settings selected")
    }

    fun canProceed(): Boolean {
        return uiState.value.privacyPolicyAccepted && uiState.value.termsAccepted
    }

    fun acceptTerms() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Save all privacy settings
            settings.setPrivacyPolicyAccepted(state.privacyPolicyAccepted)
            settings.setTermsOfUseAccepted(state.termsAccepted)
            settings.setAnalyticsEnabled(state.analyticsEnabled)
            settings.setCrashReportingEnabled(state.crashReportingEnabled)
            settings.setLoggingLevel(state.loggingLevel)
            
            // Configure logging based on user choice
            privacyManager.configureLogging()
            
            logger.i("PrivacyConsentViewModel", "Privacy consent completed - analytics: ${state.analyticsEnabled}, crashes: ${state.crashReportingEnabled}, logging: ${state.loggingLevel}")
        }
    }
}