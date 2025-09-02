package com.whispertflite.ui.onboarding

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import com.whispertflite.ui.theme.*
import com.whispertflite.ui.components.*
import com.whispertflite.ui.model.ModelSelectionScreen

@OptIn(ExperimentalPagerApi::class, ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 5 })
    
    // Permission state
    val microphonePermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // System settings launcher
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Handle result if needed */ }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Top app bar with progress
        OnboardingTopBar(
            currentPage = pagerState.currentPage,
            totalPages = pagerState.pageCount,
            onSkip = { 
                viewModel.skipOnboarding()
                onComplete() 
            }
        )
        
        // Main content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> PermissionsPage(
                    microphonePermission = microphonePermission,
                    onPermissionGranted = { viewModel.setPermissionGranted() }
                )
                2 -> KeyboardSetupPage(
                    onKeyboardEnabled = { viewModel.setKeyboardEnabled() },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        settingsLauncher.launch(intent)
                    }
                )
                3 -> ModelSelectionPage(
                    onModelSelected = { modelId -> 
                        viewModel.setSelectedModel(modelId)
                    }
                )
                4 -> CompletePage(
                    onFinish = { 
                        viewModel.completeOnboarding()
                    }
                )
            }
        }
        
        // Bottom navigation
        OnboardingBottomBar(
            currentPage = pagerState.currentPage,
            totalPages = pagerState.pageCount,
            canProceed = canProceedFromPage(pagerState.currentPage, uiState, microphonePermission),
            onNext = {
                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    // Use coroutine scope for pager animation
                    // pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } else {
                    viewModel.completeOnboarding()
                }
            },
            onBack = {
                if (pagerState.currentPage > 0) {
                    // pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        )
    }
}

@Composable
private fun OnboardingTopBar(
    currentPage: Int,
    totalPages: Int,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WhisperDesignTokens.SpacingM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Progress indicators
        Row {
            repeat(totalPages) { index ->
                val isActive = index <= currentPage
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        .animateContentSize()
                ) {
                    if (index == currentPage) {
                        // Animated current indicator
                        val infiniteTransition = rememberInfiniteTransition(label = "progress")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scale)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    }
                }
                
                if (index < totalPages - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        
        // Skip button
        if (currentPage < totalPages - 1) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPageLayout(
        icon = Icons.Default.Mic,
        title = "Welcome to WhisperIME",
        subtitle = "Transform your voice into text with AI-powered precision",
        description = "Experience seamless voice typing powered by OpenAI Whisper technology. Type with your voice in any app, anywhere on your device.",
        illustration = {
            VoiceWaveAnimation(
                modifier = Modifier.size(200.dp),
                isAnimating = true
            )
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionsPage(
    microphonePermission: PermissionState,
    onPermissionGranted: () -> Unit
) {
    LaunchedEffect(microphonePermission.status) {
        if (microphonePermission.status == PermissionStatus.Granted) {
            onPermissionGranted()
        }
    }
    
    OnboardingPageLayout(
        icon = Icons.Default.MicNone,
        title = "Microphone Access",
        subtitle = "Grant microphone permission to enable voice recognition",
        description = "WhisperIME needs access to your microphone to convert your speech into text. Your voice data is processed entirely on your device - no data is sent to external servers.",
        actionButton = {
            Button(
                onClick = {
                    when (microphonePermission.status) {
                        is PermissionStatus.Denied -> {
                            if (microphonePermission.status.shouldShowRationale) {
                                microphonePermission.launchPermissionRequest()
                            } else {
                                microphonePermission.launchPermissionRequest()
                            }
                        }
                        PermissionStatus.Granted -> {
                            onPermissionGranted()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (microphonePermission.status) {
                            PermissionStatus.Granted -> Icons.Default.CheckCircle
                            else -> Icons.Default.Mic
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (microphonePermission.status) {
                            PermissionStatus.Granted -> "Permission Granted!"
                            else -> "Grant Microphone Permission"
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun KeyboardSetupPage(
    onKeyboardEnabled: () -> Unit,
    onOpenSettings: () -> Unit
) {
    OnboardingPageLayout(
        icon = Icons.Default.Keyboard,
        title = "Enable Voice Keyboard",
        subtitle = "Add WhisperIME to your device keyboards",
        description = "To use voice typing in any app, you need to enable WhisperIME in your device's keyboard settings. Don't worry - we'll guide you through it!",
        actionButton = {
            Column {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Keyboard Settings")
                }
                
                Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingM))
                
                OutlinedButton(
                    onClick = onKeyboardEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I've Enabled the Keyboard")
                }
            }
        }
    )
}

@Composable
private fun ModelSelectionPage(
    onModelSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WhisperDesignTokens.SpacingM)
    ) {
        OnboardingHeader(
            title = "Choose Your Model",
            subtitle = "Select the Whisper model that best fits your device"
        )
        
        Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingL))
        
        // Embed the model selection screen
        ModelSelectionScreen(
            isOnboarding = true,
            onModelSelected = onModelSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompletePage(
    onFinish: () -> Unit
) {
    OnboardingPageLayout(
        icon = Icons.Default.CheckCircle,
        title = "You're All Set!",
        subtitle = "WhisperIME is ready to transform your typing experience",
        description = "Start using voice typing in any app by switching to the WhisperIME keyboard. Long press the microphone for push-to-talk, or tap for continuous recording.",
        actionButton = {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Rocket,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Using WhisperIME")
            }
        }
    )
}

@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    totalPages: Int,
    canProceed: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WhisperDesignTokens.SpacingM),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            if (currentPage > 0) {
                TextButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            // Next/Finish button
            Button(
                onClick = onNext,
                enabled = canProceed
            ) {
                Text(
                    text = if (currentPage == totalPages - 1) "Finish" else "Next"
                )
                if (currentPage < totalPages - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageLayout(
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String,
    illustration: (@Composable () -> Unit)? = null,
    actionButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WhisperDesignTokens.SpacingM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingXL))
        
        // Icon or illustration
        if (illustration != null) {
            illustration()
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingL))
        
        OnboardingHeader(title = title, subtitle = subtitle)
        
        Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingL))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        actionButton?.invoke()
        
        Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingXL))
    }
}

@Composable
private fun OnboardingHeader(
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingS))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun canProceedFromPage(
    currentPage: Int,
    uiState: OnboardingUiState,
    microphonePermission: PermissionState
): Boolean {
    return when (currentPage) {
        0 -> true // Welcome page
        1 -> microphonePermission.status == PermissionStatus.Granted
        2 -> uiState.isKeyboardEnabled
        3 -> uiState.selectedModelId.isNotEmpty()
        4 -> true // Complete page
        else -> false
    }
}