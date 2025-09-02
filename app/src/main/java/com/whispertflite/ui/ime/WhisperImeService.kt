package com.whispertflite.ui.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.*
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.whispertflite.R
import com.whispertflite.core.audio.AudioEngine
import com.whispertflite.core.output.OutputRouter
import com.whispertflite.core.output.OutputMode
import com.whispertflite.core.output.TranscriptionResult
import com.whispertflite.core.settings.WhisperSettings
import com.whispertflite.core.whisper.*
import com.whispertflite.core.logging.WhisperLogger
import com.whispertflite.ui.components.VUMeterView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Modern Material 3 InputMethodService for WhisperIME
 * Features: Voice recording, VU meter, language selection, buffered output
 */
class WhisperImeService : InputMethodService() {
    
    companion object {
        private const val TAG = "WhisperImeService"
    }
    
    // Core components
    private lateinit var audioEngine: AudioEngine
    private lateinit var whisperEngine: WhisperEngine
    private lateinit var outputRouter: OutputRouter
    private lateinit var settings: WhisperSettings
    private lateinit var logger: WhisperLogger
    
    // UI Components
    private lateinit var rootView: View
    private lateinit var micButton: MaterialButton
    private lateinit var languageButton: MaterialButton  
    private lateinit var upArrowButton: MaterialButton
    private lateinit var vuMeter: VUMeterView
    private lateinit var transcriptPreview: TextView
    private lateinit var bufferOverlay: MaterialCardView
    private lateinit var bufferText: TextView
    
    // State management
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isRecording = false
    private var currentOutputMode = OutputMode.DIRECT
    private var selectedLanguage = "en"
    
    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        logger.i(TAG, "WhisperIME service created")
    }
    
    override fun onCreateInputView(): View {
        logger.d(TAG, "Creating input view")
        return createMaterialInputView()
    }
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        logger.d(TAG, "Starting input view, restarting: $restarting")
        
        // Update output router with current input connection
        outputRouter.setInputConnection(currentInputConnection)
        
        // Update UI based on current settings
        updateUIFromSettings()
        
        // Start observing settings changes
        observeSettingsChanges()
    }
    
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        logger.d(TAG, "Finishing input view")
        
        // Stop any active recording
        if (isRecording) {
            stopRecording()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        logger.i(TAG, "WhisperIME service destroyed")
    }
    
    private fun initializeComponents() {
        logger.d(TAG, "Initializing core components")
        
        // Initialize core components
        audioEngine = AudioEngine(this)
        settings = WhisperSettings(this)
        logger = WhisperLogger.getInstance(this)
        outputRouter = OutputRouter(this)
        
        // Initialize whisper engine (will be set based on settings)
        whisperEngine = FakeWhisperEngine() // Default fallback
        
        serviceScope.launch {
            setupWhisperEngine()
        }
    }
    
    private fun createMaterialInputView(): View {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.ime_whisper_keyboard, null)
        
        initializeViews()
        setupClickListeners()
        setupAudioVisualization()
        
        return rootView
    }
    
    private fun initializeViews() {
        micButton = rootView.findViewById(R.id.btn_microphone)
        languageButton = rootView.findViewById(R.id.btn_language)
        upArrowButton = rootView.findViewById(R.id.btn_up_arrow)
        vuMeter = rootView.findViewById(R.id.vu_meter)
        transcriptPreview = rootView.findViewById(R.id.tv_transcript_preview)
        bufferOverlay = rootView.findViewById(R.id.buffer_overlay)
        bufferText = rootView.findViewById(R.id.tv_buffer_text)
    }
    
    private fun setupClickListeners() {
        // Microphone button - supports both tap and long press
        micButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording(false) // Toggle mode
            }
        }
        
        micButton.setOnLongClickListener {
            if (!isRecording) {
                startRecording(true) // Push-to-talk mode
            }
            true
        }
        
        // Language selection button
        languageButton.setOnClickListener {
            showLanguageSelector()
        }
        
        // Up arrow button (paste buffer content)
        upArrowButton.setOnClickListener {
            pasteBufferContent()
        }
        
        // Buffer overlay click - show/hide expanded view
        bufferOverlay.setOnClickListener {
            toggleBufferOverlay()
        }
    }
    
    private fun setupAudioVisualization() {
        serviceScope.launch {
            // Observe audio level for VU meter
            audioEngine.audioLevel
                .flowOn(Dispatchers.Main)
                .collect { level ->
                    vuMeter.setLevel(level)
                }
        }
        
        serviceScope.launch {
            // Observe voice detection
            audioEngine.isVoiceDetected
                .flowOn(Dispatchers.Main)
                .collect { isVoiceDetected ->
                    updateVoiceDetectionUI(isVoiceDetected)
                }
        }
    }
    
    private fun startRecording(isPushToTalk: Boolean) {
        if (isRecording) return
        
        logger.i(TAG, "Starting recording - Push-to-talk: $isPushToTalk")
        
        try {
            audioEngine.startRecording()
            isRecording = true
            
            updateRecordingUI(true)
            
            // Start transcription pipeline
            serviceScope.launch {
                transcribeAudioStream(isPushToTalk)
            }
            
        } catch (e: Exception) {
            logger.e(TAG, "Failed to start recording", e)
            showError("Failed to start recording: ${e.message}")
        }
    }
    
    private fun stopRecording() {
        if (!isRecording) return
        
        logger.i(TAG, "Stopping recording")
        
        try {
            audioEngine.stopRecording()
            isRecording = false
            
            updateRecordingUI(false)
            
        } catch (e: Exception) {
            logger.e(TAG, "Failed to stop recording", e)
        }
    }
    
    private suspend fun transcribeAudioStream(isPushToTalk: Boolean) {
        try {
            whisperEngine.transcribe(audioEngine.audioSamples)
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    withContext(Dispatchers.Main) {
                        handleTranscriptionResult(result)
                    }
                }
        } catch (e: Exception) {
            logger.e(TAG, "Error in transcription pipeline", e)
            withContext(Dispatchers.Main) {
                showError("Transcription failed: ${e.message}")
            }
        }
    }
    
    private fun handleTranscriptionResult(result: WhisperResult) {
        logger.logTranscription(
            text = result.text,
            confidence = result.confidence,
            engine = whisperEngine::class.simpleName ?: "Unknown",
            duration = System.currentTimeMillis() - result.startTime
        )
        
        // Convert to output format
        val transcriptionResult = TranscriptionResult(
            text = result.text,
            isPartial = result.isPartial,
            isFinal = result.isFinal,
            confidence = result.confidence
        )
        
        // Route output based on current mode
        outputRouter.processTranscription(transcriptionResult)
        
        // Update UI
        updateTranscriptPreview(result.text, result.isPartial)
        
        if (currentOutputMode == OutputMode.BUFFERED && result.isFinal) {
            updateBufferDisplay()
        }
    }
    
    private fun updateTranscriptPreview(text: String, isPartial: Boolean) {
        transcriptPreview.text = if (isPartial) "$text..." else text
        transcriptPreview.alpha = if (isPartial) 0.7f else 1.0f
        
        // Auto-hide preview after final result
        if (!isPartial && text.isNotEmpty()) {
            transcriptPreview.postDelayed({
                transcriptPreview.text = ""
            }, 3000)
        }
    }
    
    private fun updateBufferDisplay() {
        serviceScope.launch {
            val bufferContent = outputRouter.getBufferedText()
            bufferText.text = bufferContent
            
            // Show/hide buffer overlay based on content
            bufferOverlay.visibility = if (bufferContent.isNotEmpty()) View.VISIBLE else View.GONE
            
            // Update up arrow button state
            upArrowButton.isEnabled = bufferContent.isNotEmpty()
            upArrowButton.alpha = if (bufferContent.isNotEmpty()) 1.0f else 0.5f
        }
    }
    
    private fun pasteBufferContent() {
        if (currentOutputMode != OutputMode.BUFFERED) return
        
        val success = outputRouter.pasteBufferedContent()
        
        if (success) {
            logger.i(TAG, "Buffer content pasted successfully")
            updateBufferDisplay()
        } else {
            logger.w(TAG, "Failed to paste buffer content - copied to clipboard instead")
            showMessage("Copied to clipboard")
        }
    }
    
    private fun showLanguageSelector() {
        // Create and show language selection popup
        val popupWindow = createLanguagePopup()
        popupWindow.showAsDropDown(languageButton)
    }
    
    private fun createLanguagePopup(): PopupWindow {
        val context = this
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_language_selector, null)
        
        val listView = popupView.findViewById<ListView>(R.id.language_list)
        val languages = getAvailableLanguages()
        
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_single_choice, languages.map { it.displayName })
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        
        // Set current selection
        val currentIndex = languages.indexOfFirst { it.code == selectedLanguage }
        if (currentIndex >= 0) {
            listView.setItemChecked(currentIndex, true)
        }
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedLang = languages[position]
            setSelectedLanguage(selectedLang.code, selectedLang.displayName)
        }
        
        return PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
    }
    
    private fun setSelectedLanguage(code: String, displayName: String) {
        selectedLanguage = code
        languageButton.text = displayName
        
        serviceScope.launch {
            settings.setLanguage(code)
        }
        
        logger.i(TAG, "Language changed to: $displayName ($code)")
    }
    
    private fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption("auto", "Auto"),
            LanguageOption("en", "English"),
            LanguageOption("es", "Spanish"),
            LanguageOption("fr", "French"),
            LanguageOption("de", "German"),
            LanguageOption("zh", "Chinese"),
            LanguageOption("ja", "Japanese"),
            LanguageOption("ko", "Korean"),
            LanguageOption("pt", "Portuguese"),
            LanguageOption("ru", "Russian"),
            LanguageOption("it", "Italian")
        )
    }
    
    private fun updateRecordingUI(recording: Boolean) {
        // Update microphone button
        micButton.text = if (recording) "⏹️" else "🎤"
        micButton.isSelected = recording
        
        // Update VU meter visibility
        vuMeter.visibility = if (recording) View.VISIBLE else View.INVISIBLE
        
        // Update transcript preview
        if (!recording) {
            transcriptPreview.text = ""
        }
    }
    
    private fun updateVoiceDetectionUI(voiceDetected: Boolean) {
        // Visual feedback for voice detection
        micButton.alpha = if (voiceDetected) 1.0f else 0.8f
    }
    
    private fun toggleBufferOverlay() {
        // Toggle between compact and expanded buffer view
        val isExpanded = bufferText.maxLines > 2
        bufferText.maxLines = if (isExpanded) 2 else Int.MAX_VALUE
    }
    
    private fun updateUIFromSettings() {
        serviceScope.launch {
            // Collect settings and update UI
            settings.outputMode.first().let { mode ->
                currentOutputMode = if (mode == "DIRECT") OutputMode.DIRECT else OutputMode.BUFFERED
                outputRouter.setOutputMode(currentOutputMode)
                updateOutputModeUI()
            }
            
            settings.language.first().let { lang ->
                selectedLanguage = lang
                updateLanguageUI()
            }
            
            settings.showVUMeter.first().let { show ->
                vuMeter.visibility = if (show) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun updateOutputModeUI() {
        when (currentOutputMode) {
            OutputMode.DIRECT -> {
                upArrowButton.visibility = View.GONE
                bufferOverlay.visibility = View.GONE
            }
            OutputMode.BUFFERED -> {
                upArrowButton.visibility = View.VISIBLE
                updateBufferDisplay()
            }
        }
    }
    
    private fun updateLanguageUI() {
        val langName = getAvailableLanguages().find { it.code == selectedLanguage }?.displayName ?: selectedLanguage
        languageButton.text = langName
    }
    
    private fun observeSettingsChanges() {
        serviceScope.launch {
            settings.outputMode.collect { mode ->
                currentOutputMode = if (mode == "DIRECT") OutputMode.DIRECT else OutputMode.BUFFERED
                outputRouter.setOutputMode(currentOutputMode)
                updateOutputModeUI()
            }
        }
        
        serviceScope.launch {
            settings.language.collect { lang ->
                if (selectedLanguage != lang) {
                    selectedLanguage = lang
                    updateLanguageUI()
                }
            }
        }
    }
    
    private suspend fun setupWhisperEngine() {
        try {
            settings.speechEngineType.first().let { engineType ->
                whisperEngine = when (engineType) {
                    "FAKE" -> FakeWhisperEngine()
                    "SYSTEM" -> SpeechRecognizerEngine(this@WhisperImeService)
                    "TENSORFLOW" -> TensorFlowWhisperEngine(this@WhisperImeService)
                    else -> FakeWhisperEngine()
                }
                
                // Initialize the engine
                whisperEngine.initialize("", WhisperOptions())
                logger.i(TAG, "Whisper engine initialized: ${whisperEngine::class.simpleName}")
            }
        } catch (e: Exception) {
            logger.e(TAG, "Failed to setup whisper engine", e)
            // Fall back to fake engine
            whisperEngine = FakeWhisperEngine()
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        logger.e(TAG, "Error shown to user: $message")
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun cleanup() {
        serviceScope.cancel()
        
        try {
            if (isRecording) {
                audioEngine.stopRecording()
            }
            audioEngine.release()
            
            serviceScope.launch {
                whisperEngine.release()
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error during cleanup", e)
        }
    }
    
    data class LanguageOption(
        val code: String,
        val displayName: String
    )
}