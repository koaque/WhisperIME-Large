package com.whispertflite.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whispertflite.core.settings.WhisperSettings
import com.whispertflite.core.logging.WhisperLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isPermissionGranted: Boolean = false,
    val isKeyboardEnabled: Boolean = false,
    val selectedModelId: String = "",
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false
)

class OnboardingViewModel @Inject constructor(
    private val settings: WhisperSettings,
    private val logger: WhisperLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        logger.i("OnboardingViewModel", "Onboarding flow started")
    }

    fun setPermissionGranted() {
        logger.i("OnboardingViewModel", "Microphone permission granted")
        _uiState.value = _uiState.value.copy(isPermissionGranted = true)
    }

    fun setKeyboardEnabled() {
        logger.i("OnboardingViewModel", "Keyboard setup completed")
        _uiState.value = _uiState.value.copy(isKeyboardEnabled = true)
    }

    fun setSelectedModel(modelId: String) {
        logger.i("OnboardingViewModel", "Model selected: $modelId")
        _uiState.value = _uiState.value.copy(selectedModelId = modelId)
        
        // Save selected model to settings
        viewModelScope.launch {
            settings.setSelectedModelId(modelId)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Mark onboarding as completed in settings
                settings.setOnboardingCompleted(true)
                
                logger.i("OnboardingViewModel", "Onboarding completed successfully")
                _uiState.value = _uiState.value.copy(
                    isCompleted = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                logger.e("OnboardingViewModel", "Failed to complete onboarding", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            logger.w("OnboardingViewModel", "Onboarding skipped by user")
            settings.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }
}

// If not using Hilt, create a simple version
class OnboardingViewModelSimple(
    private val settings: WhisperSettings,
    private val logger: WhisperLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        logger.i("OnboardingViewModel", "Onboarding flow started")
    }

    fun setPermissionGranted() {
        logger.i("OnboardingViewModel", "Microphone permission granted")
        _uiState.value = _uiState.value.copy(isPermissionGranted = true)
    }

    fun setKeyboardEnabled() {
        logger.i("OnboardingViewModel", "Keyboard setup completed")
        _uiState.value = _uiState.value.copy(isKeyboardEnabled = true)
    }

    fun setSelectedModel(modelId: String) {
        logger.i("OnboardingViewModel", "Model selected: $modelId")
        _uiState.value = _uiState.value.copy(selectedModelId = modelId)
        
        // Save selected model to settings
        viewModelScope.launch {
            settings.setSelectedModelId(modelId)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Mark onboarding as completed in settings
                settings.setOnboardingCompleted(true)
                
                logger.i("OnboardingViewModel", "Onboarding completed successfully")
                _uiState.value = _uiState.value.copy(
                    isCompleted = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                logger.e("OnboardingViewModel", "Failed to complete onboarding", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            logger.w("OnboardingViewModel", "Onboarding skipped by user")
            settings.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }
}