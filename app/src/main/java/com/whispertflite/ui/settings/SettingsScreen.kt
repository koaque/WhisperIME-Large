package com.whispertflite.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whispertflite.ui.theme.*
import com.whispertflite.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WhisperDesignTokens.SpacingM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingS))
                    
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Speech Recognition Section
        item {
            SettingsSection(
                title = "Speech Recognition",
                icon = Icons.Default.Mic
            )
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.Psychology,
                        title = "Speech Engine",
                        subtitle = getEngineDisplayName(uiState.speechEngineType),
                        onClick = { viewModel.toggleEngineSelector() },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    )
                    
                    AnimatedVisibility(visible = uiState.showEngineSelector) {
                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            Divider()
                            
                            SpeechEngineOption(
                                title = "TensorFlow Lite",
                                subtitle = "On-device Whisper models (recommended)",
                                selected = uiState.speechEngineType == "TENSORFLOW",
                                onSelect = { 
                                    viewModel.setSpeechEngineType("TENSORFLOW")
                                    viewModel.toggleEngineSelector()
                                },
                                icon = Icons.Default.Memory
                            )
                            
                            SpeechEngineOption(
                                title = "Fake Engine",
                                subtitle = "Testing and emulator support",
                                selected = uiState.speechEngineType == "FAKE",
                                onSelect = { 
                                    viewModel.setSpeechEngineType("FAKE")
                                    viewModel.toggleEngineSelector()
                                },
                                icon = Icons.Default.BugReport,
                                enabled = true // Always available for testing
                            )
                            
                            SpeechEngineOption(
                                title = "Android System",
                                subtitle = "Device's built-in speech recognition",
                                selected = uiState.speechEngineType == "SYSTEM",
                                onSelect = { 
                                    viewModel.setSpeechEngineType("SYSTEM")
                                    viewModel.toggleEngineSelector()
                                },
                                icon = Icons.Default.Android,
                                warning = "Requires internet connection"
                            )
                        }
                    }
                }
            }
        }
        
        item {
            SettingsCard {
                Column {
                    SwitchListTile(
                        icon = Icons.Default.Android,
                        title = "Enable Android Fallback",
                        subtitle = "Automatically use system speech recognition when models fail",
                        checked = uiState.enableAndroidFallback,
                        onCheckedChange = { viewModel.setEnableAndroidFallback(it) }
                    )
                    
                    if (uiState.enableAndroidFallback) {
                        Divider()
                        ListTile(
                            icon = Icons.Default.Info,
                            title = "Fallback Settings",
                            subtitle = "Configure when to use Android fallback",
                            onClick = { /* Open fallback settings */ }
                        )
                    }
                }
            }
        }
        
        item {
            SettingsCard {
                SwitchListTile(
                    icon = Icons.Default.Translate,
                    title = "Auto-detect Language",
                    subtitle = "Automatically detect spoken language",
                    checked = uiState.autoDetectLanguage,
                    onCheckedChange = { viewModel.setAutoDetectLanguage(it) }
                )
            }
        }
        
        // Output & Routing Section
        item { 
            SettingsSection(
                title = "Output & Routing",
                icon = Icons.Default.Output
            )
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.Route,
                        title = "Output Mode",
                        subtitle = if (uiState.outputMode == "DIRECT") "Direct input" else "Buffer and paste",
                        onClick = { viewModel.toggleOutputModeSelector() },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    )
                    
                    AnimatedVisibility(visible = uiState.showOutputModeSelector) {
                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            Divider()
                            
                            OutputModeOption(
                                title = "Direct Mode",
                                subtitle = "Stream text directly into input fields",
                                selected = uiState.outputMode == "DIRECT",
                                onSelect = { 
                                    viewModel.setOutputMode("DIRECT")
                                    viewModel.toggleOutputModeSelector()
                                },
                                icon = Icons.Default.Input
                            )
                            
                            OutputModeOption(
                                title = "Buffered Mode",
                                subtitle = "Collect text in buffer, paste with Up Arrow",
                                selected = uiState.outputMode == "BUFFERED",
                                onSelect = { 
                                    viewModel.setOutputMode("BUFFERED")
                                    viewModel.toggleOutputModeSelector()
                                },
                                icon = Icons.Default.ViewList
                            )
                        }
                    }
                }
            }
        }
        
        item {
            SettingsCard {
                Column {
                    SwitchListTile(
                        icon = Icons.Default.ContentPaste,
                        title = "Auto-paste (Direct Mode)",
                        subtitle = "Automatically paste final transcription",
                        checked = uiState.autoPasteDirectMode,
                        onCheckedChange = { viewModel.setAutoPasteDirectMode(it) },
                        enabled = uiState.outputMode == "DIRECT"
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.SpaceBar,
                        title = "Insert Space Before Paste",
                        subtitle = "Add space when pasting text",
                        checked = uiState.insertSpaceBeforePaste,
                        onCheckedChange = { viewModel.setInsertSpaceBeforePaste(it) }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.ContentCopy,
                        title = "Auto-copy to Clipboard",
                        subtitle = "Copy transcription results to clipboard",
                        checked = uiState.autoCopyToClipboard,
                        onCheckedChange = { viewModel.setAutoCopyToClipboard(it) }
                    )
                }
            }
        }
        
        // Audio Processing Section
        item {
            SettingsSection(
                title = "Audio Processing",
                icon = Icons.Default.AudioFile
            )
        }
        
        item {
            SettingsCard {
                Column {
                    SwitchListTile(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Voice Activity Detection",
                        subtitle = "Automatically detect when you're speaking",
                        checked = uiState.enableVAD,
                        onCheckedChange = { viewModel.setEnableVAD(it) }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.NoiseAware,
                        title = "Noise Suppression",
                        subtitle = "Reduce background noise (experimental)",
                        checked = uiState.enableNoiseSuppression,
                        onCheckedChange = { viewModel.setEnableNoiseSuppression(it) }
                    )
                }
            }
        }
        
        item {
            SettingsCard {
                SliderListTile(
                    icon = Icons.Default.Tune,
                    title = "Endpointing Sensitivity",
                    subtitle = "How quickly to end voice detection",
                    value = uiState.endpointingSensitivity,
                    onValueChange = { viewModel.setEndpointingSensitivity(it) },
                    valueRange = 0f..1f,
                    steps = 10
                )
            }
        }
        
        // User Interface Section
        item {
            SettingsSection(
                title = "User Interface",
                icon = Icons.Default.Palette
            )
        }
        
        item {
            SettingsCard {
                Column {
                    SwitchListTile(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on recording start/stop",
                        checked = uiState.enableHapticFeedback,
                        onCheckedChange = { viewModel.setEnableHapticFeedback(it) }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.ShowChart,
                        title = "Show VU Meter",
                        subtitle = "Display audio level visualization",
                        checked = uiState.showVUMeter,
                        onCheckedChange = { viewModel.setShowVUMeter(it) }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.TouchApp,
                        title = "Push-to-Talk Mode",
                        subtitle = "Hold microphone button to record",
                        checked = uiState.pushToTalkMode,
                        onCheckedChange = { viewModel.setPushToTalkMode(it) }
                    )
                }
            }
        }
        
        // Privacy & Data Section
        item {
            SettingsSection(
                title = "Privacy & Data",
                icon = Icons.Default.PrivacyTip
            )
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.Security,
                        title = "Maximum Privacy Mode",
                        subtitle = "Disable all data collection and logging",
                        onClick = { 
                            scope.launch {
                                viewModel.enableMaximumPrivacy()
                            }
                        }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.Analytics,
                        title = "Anonymous Usage Analytics",
                        subtitle = "Help improve the app (no voice data)",
                        checked = uiState.analyticsEnabled,
                        onCheckedChange = { viewModel.setAnalyticsEnabled(it) }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.BugReport,
                        title = "Crash Reporting",
                        subtitle = "Send anonymous crash reports",
                        checked = uiState.crashReportingEnabled,
                        onCheckedChange = { viewModel.setCrashReportingEnabled(it) }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.DataUsage,
                        title = "Data Collection",
                        subtitle = "Allow minimal data collection for features",
                        checked = uiState.dataCollectionEnabled,
                        onCheckedChange = { viewModel.setDataCollectionEnabled(it) }
                    )
                }
            }
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        onClick = { viewModel.showPrivacyPolicy() }
                    )
                    
                    Divider()
                    
                    ListTile(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Use",
                        subtitle = "App usage terms and conditions",
                        onClick = { viewModel.showTermsOfUse() }
                    )
                    
                    Divider()
                    
                    ListTile(
                        icon = Icons.Default.Delete,
                        title = "Delete All Data",
                        subtitle = "Permanently remove all app data",
                        onClick = { viewModel.showDeleteAllDataDialog() }
                    )
                }
            }
        }
        
        // Logging Section (Separate from Privacy for Battery Control)
        item {
            SettingsSection(
                title = "Logging & Diagnostics",
                icon = Icons.Default.BugReport
            )
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.Settings,
                        title = "Logging Level",
                        subtitle = getLoggingLevelDescription(uiState.loggingLevel),
                        onClick = { viewModel.toggleLoggingLevelSelector() },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    )
                    
                    AnimatedVisibility(visible = uiState.showLoggingLevelSelector) {
                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            Divider()
                            
                            LoggingLevelOption(
                                title = "Disabled",
                                subtitle = "No logging - Maximum battery life",
                                batteryImpact = "🔋 Best",
                                selected = uiState.loggingLevel == "DISABLED",
                                onSelect = { 
                                    viewModel.setLoggingLevel("DISABLED")
                                    viewModel.toggleLoggingLevelSelector()
                                }
                            )
                            
                            LoggingLevelOption(
                                title = "Errors Only", 
                                subtitle = "Critical errors only - Minimal battery impact",
                                batteryImpact = "🔋 Excellent",
                                selected = uiState.loggingLevel == "ERROR_ONLY",
                                onSelect = { 
                                    viewModel.setLoggingLevel("ERROR_ONLY")
                                    viewModel.toggleLoggingLevelSelector()
                                }
                            )
                            
                            LoggingLevelOption(
                                title = "Minimal",
                                subtitle = "Errors and warnings - Low battery impact",
                                batteryImpact = "🔋 Good",
                                selected = uiState.loggingLevel == "MINIMAL",
                                onSelect = { 
                                    viewModel.setLoggingLevel("MINIMAL")
                                    viewModel.toggleLoggingLevelSelector()
                                }
                            )
                            
                            LoggingLevelOption(
                                title = "Standard",
                                subtitle = "Normal app events - Moderate battery impact",
                                batteryImpact = "🔋 Fair",
                                selected = uiState.loggingLevel == "STANDARD",
                                onSelect = { 
                                    viewModel.setLoggingLevel("STANDARD")
                                    viewModel.toggleLoggingLevelSelector()
                                }
                            )
                            
                            LoggingLevelOption(
                                title = "Extensive",
                                subtitle = "Detailed debugging - High battery impact",
                                batteryImpact = "⚠️ High Usage",
                                selected = uiState.loggingLevel == "EXTENSIVE",
                                onSelect = { 
                                    viewModel.setLoggingLevel("EXTENSIVE")
                                    viewModel.toggleLoggingLevelSelector()
                                }
                            )
                        }
                    }
                }
            }
        }
        
        item {
            SettingsCard {
                Column {
                    SwitchListTile(
                        icon = Icons.Default.Save,
                        title = "Save Logs to Files",
                        subtitle = "Store logs on device (uses storage space)",
                        checked = uiState.fileLoggingEnabled,
                        onCheckedChange = { viewModel.setFileLoggingEnabled(it) },
                        enabled = uiState.loggingLevel != "DISABLED"
                    )
                    
                    if (uiState.fileLoggingEnabled && uiState.loggingLevel != "DISABLED") {
                        Divider()
                        ListTile(
                            icon = Icons.Default.Description,
                            title = "Export Logs",
                            subtitle = "Share logs for debugging",
                            onClick = { 
                                scope.launch {
                                    viewModel.exportLogs(context)
                                }
                            }
                        )
                        
                        Divider()
                        ListTile(
                            icon = Icons.Default.Delete,
                            title = "Clear Logs",
                            subtitle = "Delete all stored log files",
                            onClick = { 
                                scope.launch {
                                    viewModel.clearLogs()
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Advanced Section
        item {
            SettingsSection(
                title = "Advanced",
                icon = Icons.Default.Settings
            )
        }
        
        item {
            SettingsCard {
                ListTile(
                    icon = Icons.Default.RestartAlt,
                    title = "Reset All Settings",
                    subtitle = "Restore default configuration",
                    onClick = { viewModel.showResetConfirmation() }
                )
            }
        }
        
        // Storage Section
        item {
            SettingsSection(
                title = "Storage",
                icon = Icons.Default.Storage
            )
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.CloudDownload,
                        title = "Model Downloads",
                        subtitle = "Manage Whisper models",
                        onClick = { /* Navigate to model manager */ }
                    )
                    
                    Divider()
                    
                    SwitchListTile(
                        icon = Icons.Default.Wifi,
                        title = "WiFi Only Downloads",
                        subtitle = "Only download models on WiFi",
                        checked = uiState.modelDownloadWifiOnly,
                        onCheckedChange = { viewModel.setModelDownloadWifiOnly(it) }
                    )
                    
                    Divider()
                    
                    ListTile(
                        icon = Icons.Default.Info,
                        title = "Storage Usage",
                        subtitle = "${uiState.storageUsedMB} MB used",
                        onClick = { /* Show storage details */ }
                    )
                }
            }
        }
        
        // About Section
        item {
            SettingsSection(
                title = "About",
                icon = Icons.Default.Info
            )
        }
        
        item {
            SettingsCard {
                Column {
                    ListTile(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "1.1.0 (Build 110)"
                    )
                    
                    Divider()
                    
                    ListTile(
                        icon = Icons.Default.Privacy,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        onClick = { /* Show privacy policy */ }
                    )
                    
                    Divider()
                    
                    ListTile(
                        icon = Icons.Default.Code,
                        title = "Open Source Licenses",
                        subtitle = "Third-party library licenses",
                        onClick = { /* Show licenses */ }
                    )
                }
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingXXL))
        }
    }
    
    // Reset confirmation dialog
    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetConfirmation() },
            title = { Text("Reset All Settings?") },
            text = { 
                Text("This will restore all settings to their default values. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.resetAllSettings()
                        }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideResetConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = WhisperDesignTokens.SpacingM,
                end = WhisperDesignTokens.SpacingM,
                top = WhisperDesignTokens.SpacingL,
                bottom = WhisperDesignTokens.SpacingS
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingS))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WhisperDesignTokens.SpacingM, vertical = WhisperDesignTokens.SpacingXS),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
private fun SpeechEngineOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    warning: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
                enabled = enabled
            )
            .padding(WhisperDesignTokens.SpacingM)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled
            )
            
            Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingM))
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            
            Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingS))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
                
                if (warning != null) {
                    Text(
                        text = "⚠️ $warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(WhisperDesignTokens.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingM))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingS))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoggingLevelOption(
    title: String,
    subtitle: String,
    batteryImpact: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(WhisperDesignTokens.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingM))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = batteryImpact,
                style = MaterialTheme.typography.bodySmall,
                color = if (batteryImpact.contains("🔋")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun getLoggingLevelDescription(level: String): String {
    return when (level) {
        "DISABLED" -> "Disabled - Best battery life"
        "ERROR_ONLY" -> "Errors only - Excellent battery"
        "MINIMAL" -> "Minimal - Good battery"
        "STANDARD" -> "Standard - Fair battery"
        "EXTENSIVE" -> "Extensive - High battery usage"
        else -> "Unknown"
    }
}

private fun getEngineDisplayName(engineType: String): String {
    return when (engineType) {
        "TENSORFLOW" -> "TensorFlow Lite"
        "FAKE" -> "Fake Engine (Testing)"
        "SYSTEM" -> "Android System"
        else -> "Unknown"
    }
}